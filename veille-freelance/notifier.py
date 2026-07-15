"""
Envoi du digest quotidien sur Telegram, via un bot créé avec BotFather.

Voir le README pour la procédure complète de création du bot et de
récupération du token + chat_id.
"""
import html
import logging
import sqlite3

import requests

import config

logger = logging.getLogger(__name__)

TELEGRAM_API_URL = "https://api.telegram.org/bot{token}/sendMessage"
TELEGRAM_MAX_LENGTH = 4096


def _format_mission(mission: sqlite3.Row) -> str:
    """Formate une offre pour l'affichage Telegram (HTML) : titre cliquable, source, score, description, date."""
    title = html.escape(mission["title"] or "(sans titre)")
    url = html.escape(mission["url"] or "", quote=True)
    description = html.escape(mission["description"] or "")
    date_posted = mission["date_posted"] or "date inconnue"

    return (
        f'🔹 <a href="{url}">{title}</a>\n'
        f"Source : {html.escape(mission['source'])} | Score : {mission['score']}\n"
        f"{description}\n"
        f"Date : {html.escape(str(date_posted))}"
    )


def build_digest_messages(missions: list[sqlite3.Row]) -> list[str]:
    """
    Construit le texte du digest, découpé en plusieurs messages si nécessaire
    (limite Telegram : 4096 caractères par message).
    """
    if not missions:
        return []

    header = f"📋 <b>Veille freelance — {len(missions)} nouvelle(s) offre(s)</b>\n\n"
    blocks = [_format_mission(m) for m in missions]

    messages = []
    current = header
    for block in blocks:
        addition = block + "\n\n"
        if len(current) + len(addition) > TELEGRAM_MAX_LENGTH:
            messages.append(current.rstrip())
            current = addition
        else:
            current += addition
    if current.strip():
        messages.append(current.rstrip())

    return messages


def send_telegram_message(text: str) -> bool:
    """Envoie un message Telegram. Retourne True en cas de succès."""
    if not config.TELEGRAM_BOT_TOKEN or not config.TELEGRAM_CHAT_ID:
        logger.error(
            "TELEGRAM_BOT_TOKEN ou TELEGRAM_CHAT_ID manquant dans .env — impossible d'envoyer le digest "
            "(voir le README pour la procédure BotFather)"
        )
        return False

    url = TELEGRAM_API_URL.format(token=config.TELEGRAM_BOT_TOKEN)
    payload = {
        "chat_id": config.TELEGRAM_CHAT_ID,
        "text": text,
        "parse_mode": "HTML",
        "disable_web_page_preview": False,
    }
    try:
        response = requests.post(url, data=payload, timeout=config.REQUEST_TIMEOUT)
        response.raise_for_status()
        return True
    except requests.RequestException as e:
        logger.error("Échec de l'envoi Telegram : %s", e)
        return False


def send_digest(missions: list[sqlite3.Row]) -> bool:
    """Envoie le digest complet (potentiellement découpé en plusieurs messages)."""
    messages = build_digest_messages(missions)
    if not messages:
        logger.info("Aucune offre à notifier, digest non envoyé.")
        return True

    all_ok = True
    for msg in messages:
        ok = send_telegram_message(msg)
        all_ok = all_ok and ok
    return all_ok
