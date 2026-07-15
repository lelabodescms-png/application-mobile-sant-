"""
Logique de filtrage des offres : détection du remote, matching des mots-clés
de service, scoring de pertinence, exclusions.

Toute la configuration éditable vit dans keywords.yaml — ce module ne fait
que l'appliquer.
"""
import logging
import unicodedata
from functools import lru_cache

import yaml

import config

logger = logging.getLogger(__name__)


def normalize(text: str) -> str:
    """Minuscules + suppression des accents, pour un matching robuste."""
    if not text:
        return ""
    text = text.lower()
    text = unicodedata.normalize("NFKD", text)
    text = "".join(c for c in text if not unicodedata.combining(c))
    return text


@lru_cache(maxsize=1)
def load_keywords() -> dict:
    """Charge keywords.yaml (mis en cache ; supprimez le cache pour recharger à chaud)."""
    with open(config.KEYWORDS_PATH, encoding="utf-8") as f:
        data = yaml.safe_load(f)
    return data


def reload_keywords() -> dict:
    """Force le rechargement de keywords.yaml (utile après une édition à chaud)."""
    load_keywords.cache_clear()
    return load_keywords()


def is_remote(title: str, description: str, source_is_remote_only: bool = False) -> bool:
    """
    Détermine si une offre est éligible "remote", selon les règles suivantes :
    - toujours vrai si la source est 100% remote (ex: Remotive) ;
    - sinon, vrai si un mot-clé d'inclusion est présent ET aucun mot-clé
      d'exclusion n'est présent (sauf si une exception explicite est présente).
    """
    if source_is_remote_only:
        return True

    text = normalize(f"{title} {description}")
    rules = load_keywords()["remote"]

    inclusion_ok = any(normalize(kw) in text for kw in rules["inclusion"])
    if not inclusion_ok:
        return False

    exclusion_hit = any(normalize(kw) in text for kw in rules["exclusion"])
    if not exclusion_hit:
        return True

    # Une exclusion est présente (ex: "hybride") : on regarde si une exception
    # explicite ("télétravail possible") sauve quand même l'offre.
    exception_ok = any(normalize(kw) in text for kw in rules["exception_si_present"])
    return exception_ok


def matched_keywords(title: str, description: str) -> list[str]:
    """Retourne la liste des mots-clés de service trouvés dans le titre + la description."""
    text = normalize(f"{title} {description}")
    services = load_keywords()["services"]

    found = []
    for category_keywords in services.values():
        for kw in category_keywords:
            if normalize(kw) in text:
                found.append(kw)
    return found


def is_excluded(title: str, description: str) -> bool:
    """Vrai si un mot-clé d'exclusion (stage, alternance, CDI uniquement...) est présent."""
    text = normalize(f"{title} {description}")
    exclusions = load_keywords()["exclusions"]
    return any(normalize(kw) in text for kw in exclusions)


def score_mission(title: str, description: str) -> tuple[int, list[str]]:
    """
    Calcule un score de pertinence simple :
    - +1 par mot-clé de service matché (titre + description)
    - +BONUS_TITRE en plus si ce mot-clé apparaît dans le titre
    - +BONUS_FREELANCE si "freelance" ou "mission" apparaît dans le texte
    Retourne (score, mots-clés matchés).
    """
    title_norm = normalize(title)
    description_norm = normalize(description)
    keywords = matched_keywords(title, description)

    score = len(keywords)
    for kw in keywords:
        if normalize(kw) in title_norm:
            score += config.BONUS_TITRE

    full_text = f"{title_norm} {description_norm}"
    for bonus_word in ("freelance", "mission", "freelancer"):
        if bonus_word in full_text:
            score += config.BONUS_FREELANCE

    return score, keywords


def passes_filters(
    title: str,
    description: str,
    source_is_remote_only: bool = False,
) -> tuple[bool, int, list[str]]:
    """
    Applique la chaîne de filtres complète à une offre.
    Retourne (accepté, score, mots-clés matchés).
    """
    if is_excluded(title, description):
        return False, 0, []

    if not is_remote(title, description, source_is_remote_only):
        return False, 0, []

    score, keywords = score_mission(title, description)
    if not keywords:
        # Aucun mot-clé de service : l'offre ne correspond à aucun de nos services.
        return False, 0, []

    return True, score, keywords
