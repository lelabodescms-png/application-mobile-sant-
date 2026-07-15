"""
Configuration centrale de l'application.

Charge les variables d'environnement depuis le fichier .env (voir .env.example)
et expose des constantes utilisées par les autres modules.
"""
import os
from pathlib import Path

from dotenv import load_dotenv

# Racine du projet (dossier contenant ce fichier)
BASE_DIR = Path(__file__).resolve().parent

# Charge le fichier .env s'il existe
load_dotenv(BASE_DIR / ".env")

# --- Telegram ---
TELEGRAM_BOT_TOKEN = os.getenv("TELEGRAM_BOT_TOKEN", "")
TELEGRAM_CHAT_ID = os.getenv("TELEGRAM_CHAT_ID", "")

# --- Gmail ---
GMAIL_QUERY = os.getenv(
    "GMAIL_QUERY",
    "from:(jobalerts-noreply@indeed.com OR alerte@indeed.com OR welcometothejungle.com) newer_than:2d",
)
GMAIL_CREDENTIALS_PATH = BASE_DIR / "gmail_credentials.json"
GMAIL_TOKEN_PATH = BASE_DIR / "gmail_token.json"

# --- Fichiers de données ---
DB_PATH = BASE_DIR / "missions.db"
KEYWORDS_PATH = BASE_DIR / "keywords.yaml"

# --- Logs ---
LOG_DIR = BASE_DIR / "logs"
LOG_DIR.mkdir(exist_ok=True)
LOG_PATH = LOG_DIR / "veille.log"
LOG_LEVEL = os.getenv("LOG_LEVEL", "INFO")

# --- Réseau ---
# User-Agent identifiable, avec contact, conformément aux bonnes pratiques.
USER_AGENT = os.getenv(
    "USER_AGENT",
    f"VeilleFreelanceBot/1.0 (+contact: {os.getenv('CONTACT_EMAIL', 'contact@example.com')}; "
    "veille personnelle de missions freelance)",
)
REQUEST_TIMEOUT = 15  # secondes

# --- Scoring ---
BONUS_TITRE = 2       # bonus par mot-clé trouvé dans le titre
BONUS_FREELANCE = 1   # bonus si "freelance"/"mission" apparaît
