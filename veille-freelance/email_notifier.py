"""
Envoi du digest quotidien par email, via l'API Gmail (sur ta propre adresse).

Réutilise les identifiants OAuth de sources/gmail_parser.py (mêmes scopes :
lecture ET envoi demandés ensemble dès la première autorisation).
"""
from __future__ import annotations

import base64
import html
import logging
import sqlite3
from email.mime.text import MIMEText

from sources.gmail_parser import get_gmail_credentials

logger = logging.getLogger(__name__)


def _format_mission_html(mission: sqlite3.Row) -> str:
    """Formate une offre en bloc HTML : titre cliquable, source, score, description, date."""
    title = html.escape(mission["title"] or "(sans titre)")
    url = html.escape(mission["url"] or "", quote=True)
    description = html.escape(mission["description"] or "")
    date_posted = html.escape(str(mission["date_posted"] or "date inconnue"))
    source = html.escape(mission["source"])
    score = mission["score"]

    return f"""
    <div style="margin-bottom:20px;padding-bottom:16px;border-bottom:1px solid #e5e5e5;">
      <a href="{url}" style="font-size:16px;font-weight:600;color:#1a73e8;text-decoration:none;">{title}</a>
      <div style="color:#5f6368;font-size:13px;margin-top:4px;">
        Source : {source} &nbsp;|&nbsp; Score : {score} &nbsp;|&nbsp; {date_posted}
      </div>
      <div style="color:#3c4043;font-size:14px;margin-top:6px;">{description}</div>
    </div>
    """


def build_digest_html(missions: list[sqlite3.Row]) -> str:
    """Construit le corps HTML complet du digest."""
    blocks = "".join(_format_mission_html(m) for m in missions)
    return f"""
    <div style="font-family:Arial,Helvetica,sans-serif;max-width:640px;margin:0 auto;">
      <h2 style="color:#202124;">📋 Veille freelance — {len(missions)} nouvelle(s) offre(s)</h2>
      {blocks}
    </div>
    """


def send_digest(missions: list[sqlite3.Row]) -> bool:
    """Envoie le digest par email. Retourne True en cas de succès (ou s'il n'y a rien à envoyer)."""
    if not missions:
        logger.info("Aucune nouvelle offre à notifier, digest non envoyé.")
        return True

    creds = get_gmail_credentials()
    if creds is None:
        logger.error(
            "Gmail non configuré (gmail_credentials.json manquant) — impossible d'envoyer le digest par email. "
            "Voir le README pour la procédure OAuth Gmail."
        )
        return False

    from googleapiclient.discovery import build
    from googleapiclient.errors import HttpError

    try:
        service = build("gmail", "v1", credentials=creds)
        my_email = service.users().getProfile(userId="me").execute()["emailAddress"]

        message = MIMEText(build_digest_html(missions), "html", "utf-8")
        message["to"] = my_email
        message["from"] = my_email
        message["subject"] = f"Veille freelance — {len(missions)} nouvelle(s) offre(s)"

        raw = base64.urlsafe_b64encode(message.as_bytes()).decode("ascii")
        service.users().messages().send(userId="me", body={"raw": raw}).execute()
        logger.info("Digest envoyé par email à %s", my_email)
        return True
    except HttpError as e:
        logger.error("Échec de l'envoi de l'email de digest : %s", e)
        return False
