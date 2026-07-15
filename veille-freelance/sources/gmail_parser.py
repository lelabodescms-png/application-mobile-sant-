"""
Source : parsing des alertes email Gmail (Indeed, Welcome to the Jungle).

Lit la boîte Gmail via l'API Gmail (OAuth 2.0), repère les emails d'alerte
job configurés par l'utilisateur, et en extrait les offres (titre + lien).

--------------------------------------------------------------------------
CONFIGURATION OAuth — à faire une seule fois (voir aussi le README) :
--------------------------------------------------------------------------
1. Va sur https://console.cloud.google.com/ et crée un projet (ou réutilise
   un projet existant).
2. Dans "API et services" > "Bibliothèque", active l'API "Gmail API".
3. Dans "API et services" > "Écran de consentement OAuth" :
   - Type d'utilisateur : Externe
   - Renseigne un nom d'application (ex: "Veille Freelance") et ton email
   - Ajoute ton propre email Gmail comme "utilisateur test"
4. Dans "API et services" > "Identifiants" > "Créer des identifiants" >
   "ID client OAuth" :
   - Type d'application : "Application de bureau" (Desktop app)
5. Télécharge le fichier JSON généré, renomme-le en `gmail_credentials.json`
   et place-le à la racine du projet veille-freelance/ (à côté de main.py).
6. Lance une première fois `python main.py --test-source gmail` DEPUIS UN
   TERMINAL INTERACTIF (pas via launchd) : un navigateur s'ouvre pour te
   demander d'autoriser l'application. Une fois autorisé, un fichier
   `gmail_token.json` est créé automatiquement : il sera réutilisé (et
   rafraîchi tout seul) par les prochains scans, y compris via launchd.

Ce même jeton OAuth sert aussi à l'envoi du digest quotidien par email
(voir email_notifier.py) — c'est pour ça que les deux scopes ci-dessous
(lecture ET envoi) sont demandés ensemble dès la première autorisation.
--------------------------------------------------------------------------

⚠️ Les templates HTML des emails d'alerte Indeed / WTTJ changent parfois.
L'extraction ci-dessous est volontairement générique (recherche de liens
vers des pages d'offres) ; si elle ne détecte plus rien après un changement
de template, ajuste les motifs dans _extract_jobs_from_html().
"""
import base64
import logging
from email.utils import parsedate_to_datetime

from bs4 import BeautifulSoup

import config

logger = logging.getLogger(__name__)

SOURCE_NAME_INDEED = "Indeed (Gmail)"
SOURCE_NAME_WTTJ = "Welcome to the Jungle (Gmail)"
IS_REMOTE_ONLY = False

SCOPES = [
    "https://www.googleapis.com/auth/gmail.readonly",
    "https://www.googleapis.com/auth/gmail.send",
]


def get_gmail_credentials():
    """Charge/rafraîchit les identifiants OAuth Gmail. Retourne None si non configuré."""
    if not config.GMAIL_CREDENTIALS_PATH.exists():
        logger.info(
            "[Gmail] gmail_credentials.json introuvable — source Gmail désactivée "
            "(voir la documentation en tête de sources/gmail_parser.py pour la configurer)"
        )
        return None

    # Imports différés : ces dépendances ne sont nécessaires que si Gmail est utilisé.
    from google.auth.exceptions import RefreshError
    from google.auth.transport.requests import Request
    from google.oauth2.credentials import Credentials
    from google_auth_oauthlib.flow import InstalledAppFlow

    creds = None
    if config.GMAIL_TOKEN_PATH.exists():
        creds = Credentials.from_authorized_user_file(str(config.GMAIL_TOKEN_PATH), SCOPES)

    if creds and creds.valid:
        return creds

    if creds and creds.expired and creds.refresh_token:
        try:
            creds.refresh(Request())
            config.GMAIL_TOKEN_PATH.write_text(creds.to_json())
            return creds
        except RefreshError as e:
            logger.warning("[Gmail] jeton expiré et non rafraîchissable (%s) — réautorisation nécessaire", e)

    # Pas de jeton valide : lance le flow interactif (nécessite un terminal avec navigateur).
    logger.info("[Gmail] autorisation OAuth requise — ouverture du navigateur")
    flow = InstalledAppFlow.from_client_secrets_file(str(config.GMAIL_CREDENTIALS_PATH), SCOPES)
    creds = flow.run_local_server(port=0)
    config.GMAIL_TOKEN_PATH.write_text(creds.to_json())
    return creds


def _decode_body(payload: dict) -> str:
    """Extrait le corps HTML (ou texte) d'un message Gmail, y compris multipart."""
    if payload.get("mimeType") == "text/html" and payload.get("body", {}).get("data"):
        return base64.urlsafe_b64decode(payload["body"]["data"]).decode("utf-8", errors="ignore")

    for part in payload.get("parts", []) or []:
        html = _decode_body(part)
        if html:
            return html

    if payload.get("body", {}).get("data"):
        return base64.urlsafe_b64decode(payload["body"]["data"]).decode("utf-8", errors="ignore")

    return ""


def _extract_jobs_from_html(html: str, source_name: str, date_posted: str) -> list[dict]:
    """Extrait les offres (titre + lien) d'un email d'alerte, à partir de son HTML."""
    soup = BeautifulSoup(html, "html.parser")
    missions = []
    seen_urls = set()

    for link in soup.find_all("a", href=True):
        href = link["href"]

        is_job_link = (
            ("indeed.com" in href and ("/rc/clk" in href or "/viewjob" in href or "/pagead/clk" in href))
            or ("welcometothejungle.com" in href and "/jobs/" in href)
        )
        if not is_job_link:
            continue

        title = link.get_text(strip=True)
        if not title or len(title) < 5 or href in seen_urls:
            continue
        seen_urls.add(href)

        missions.append(
            {
                "title": title,
                "url": href,
                "description": "",
                "source": source_name,
                "date_posted": date_posted,
            }
        )

    return missions


def fetch() -> list[dict]:
    """Récupère et parse les emails d'alerte Indeed/WTTJ. Retourne [] si Gmail n'est pas configuré."""
    creds = get_gmail_credentials()
    if creds is None:
        return []

    from googleapiclient.discovery import build
    from googleapiclient.errors import HttpError

    try:
        service = build("gmail", "v1", credentials=creds)
        results = service.users().messages().list(userId="me", q=config.GMAIL_QUERY).execute()
        message_refs = results.get("messages", [])
    except HttpError as e:
        logger.error("[Gmail] erreur API lors de la recherche des emails : %s", e)
        return []
    except Exception as e:
        logger.error("[Gmail] erreur inattendue : %s", e)
        return []

    missions = []
    for ref in message_refs:
        try:
            message = service.users().messages().get(userId="me", id=ref["id"], format="full").execute()
            headers = {h["name"]: h["value"] for h in message["payload"].get("headers", [])}
            sender = headers.get("From", "")
            date_header = headers.get("Date", "")
            try:
                date_posted = parsedate_to_datetime(date_header).isoformat()
            except (TypeError, ValueError):
                date_posted = ""

            source_name = SOURCE_NAME_WTTJ if "welcometothejungle" in sender.lower() else SOURCE_NAME_INDEED
            html = _decode_body(message["payload"])
            if not html:
                continue

            missions.extend(_extract_jobs_from_html(html, source_name, date_posted))
        except HttpError as e:
            logger.error("[Gmail] erreur en lisant le message %s : %s", ref.get("id"), e)
            continue

    logger.info("[Gmail] %d offres extraites depuis %d emails", len(missions), len(message_refs))
    return missions
