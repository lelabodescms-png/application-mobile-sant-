"""
Point d'entrée de l'application de veille freelance.

Usage :
    python main.py                      # scan seul (fetch + filtre + stockage), pas de notif
    python main.py --digest             # envoie le digest Telegram des offres non notifiées
    python main.py --now                # scan + digest immédiat (test manuel complet)
    python main.py --test-source codeur # fetch brut d'une seule source, sans écrire en base

Sources disponibles pour --test-source : codeur, graphiste, remotive, freework,
mission_freelances, gmail
"""
import argparse
import logging
import logging.handlers

import config
import database
import filters
import notifier
from sources import codeur, freework, gmail_parser, graphiste, mission_freelances, remotive

# Registre des sources : nom court -> module. C'est ICI qu'on ajoute une nouvelle
# source (voir le README, section "Ajouter une source RSS").
SOURCES = {
    "codeur": codeur,
    "graphiste": graphiste,
    "remotive": remotive,
    "freework": freework,
    "mission_freelances": mission_freelances,
    "gmail": gmail_parser,
}


def setup_logging() -> None:
    """Configure le logging : console + fichier tournant (5 Mo x 3)."""
    level = getattr(logging, config.LOG_LEVEL.upper(), logging.INFO)
    formatter = logging.Formatter("%(asctime)s [%(levelname)s] %(name)s: %(message)s")

    file_handler = logging.handlers.RotatingFileHandler(
        config.LOG_PATH, maxBytes=5 * 1024 * 1024, backupCount=3, encoding="utf-8"
    )
    file_handler.setFormatter(formatter)

    console_handler = logging.StreamHandler()
    console_handler.setFormatter(formatter)

    root = logging.getLogger()
    root.setLevel(level)
    root.addHandler(file_handler)
    root.addHandler(console_handler)


logger = logging.getLogger(__name__)


def fetch_all_missions() -> list[dict]:
    """Interroge toutes les sources. Une source en échec est loguée et ignorée, sans bloquer les autres."""
    all_missions = []
    for name, module in SOURCES.items():
        try:
            missions = module.fetch()
            all_missions.extend(missions)
        except Exception as e:
            # Filet de sécurité ultime : même une erreur non prévue dans une source
            # ne doit jamais interrompre le scan global.
            logger.error("Source '%s' en échec inattendu : %s", name, e, exc_info=True)
            continue
    return all_missions


def run_scan() -> int:
    """Récupère toutes les sources, filtre, déduplique et stocke les nouvelles offres."""
    logger.info("=== Démarrage du scan ===")
    database.init_db()

    raw_missions = fetch_all_missions()
    logger.info("%d offres brutes récupérées (toutes sources confondues)", len(raw_missions))

    new_count = 0
    for mission in raw_missions:
        if not mission.get("title") or not mission.get("url"):
            continue

        source_module = SOURCES.get(_source_key_from_name(mission["source"]))
        is_remote_only = getattr(source_module, "IS_REMOTE_ONLY", False) if source_module else False

        accepted, score, matched = filters.passes_filters(
            mission["title"], mission["description"], source_is_remote_only=is_remote_only
        )
        if not accepted:
            continue

        if database.mission_exists(mission["url"], mission["title"]):
            continue

        database.insert_mission(
            url=mission["url"],
            title=mission["title"],
            description=mission["description"],
            source=mission["source"],
            date_posted=mission["date_posted"],
            score=score,
        )
        new_count += 1

    logger.info("=== Scan terminé : %d nouvelle(s) offre(s) ajoutée(s) ===", new_count)
    return new_count


def _source_key_from_name(source_name: str) -> str | None:
    """Retrouve la clé du registre SOURCES à partir du nom affiché (mission['source'])."""
    for key, module in SOURCES.items():
        if getattr(module, "SOURCE_NAME", None) == source_name:
            return key
        # Gmail expose deux noms de source (Indeed / WTTJ) pour un seul module.
        if key == "gmail" and "Gmail" in source_name:
            return key
    return None


def run_digest() -> None:
    """Envoie le digest Telegram des offres pas encore notifiées."""
    logger.info("=== Envoi du digest ===")
    database.init_db()
    missions = database.get_unnotified_missions()

    if not missions:
        logger.info("Aucune nouvelle offre à notifier.")
        return

    success = notifier.send_digest(missions)
    if success:
        database.mark_notified([m["id"] for m in missions])
        logger.info("Digest envoyé et %d offre(s) marquée(s) comme notifiée(s).", len(missions))
    else:
        logger.error("Échec de l'envoi du digest — les offres restent marquées comme non notifiées.")


def test_single_source(name: str) -> None:
    """Fetch brut d'une seule source (sans filtrage ni écriture en base), pour débugger."""
    module = SOURCES.get(name)
    if module is None:
        print(f"Source inconnue : {name}. Sources disponibles : {', '.join(SOURCES)}")
        return

    print(f"--- Test de la source '{name}' ---")
    try:
        missions = module.fetch()
    except Exception as e:
        print(f"ERREUR : {e}")
        return

    print(f"{len(missions)} offre(s) brute(s) récupérée(s) :\n")
    for m in missions[:20]:
        accepted, score, matched = filters.passes_filters(
            m["title"], m["description"], source_is_remote_only=getattr(module, "IS_REMOTE_ONLY", False)
        )
        marker = "✅" if accepted else "❌"
        print(f"{marker} [{score if accepted else '-'}] {m['title']}")
        print(f"    {m['url']}")
        if accepted:
            print(f"    mots-clés : {', '.join(matched)}")
        print()

    if len(missions) > 20:
        print(f"... et {len(missions) - 20} de plus (affichage limité à 20).")


def main() -> None:
    parser = argparse.ArgumentParser(description="Veille automatisée de missions freelance.")
    parser.add_argument("--digest", action="store_true", help="Envoie le digest Telegram immédiatement.")
    parser.add_argument("--now", action="store_true", help="Lance un scan puis un digest immédiatement.")
    parser.add_argument(
        "--test-source",
        metavar="NOM",
        help=f"Teste une seule source sans écrire en base. Sources : {', '.join(SOURCES)}",
    )
    args = parser.parse_args()

    setup_logging()

    if args.test_source:
        test_single_source(args.test_source)
    elif args.now:
        run_scan()
        run_digest()
    elif args.digest:
        run_digest()
    else:
        run_scan()


if __name__ == "__main__":
    main()
