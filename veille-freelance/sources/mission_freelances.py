"""
Source : Mission Freelances (mission-freelances.fr), agrégateur de missions
freelance (LinkedIn, Indeed, WTTJ, HelloWork...) couvrant marketing, design,
dev, rédaction.

⚠️ Statut V1 : aucun flux RSS public n'a été trouvé pour ce site. Ce module
fait donc du scraping HTML minimal et respectueux (robots.txt + User-Agent
identifiable) de la page publique des missions. Comme pour freework.py, si
la structure de la page change, la source se désactive proprement (log +
liste vide) sans bloquer le reste du scan.
"""
import logging
from urllib.parse import urljoin

from sources.base import clean_html, fetch_html

logger = logging.getLogger(__name__)

SOURCE_NAME = "Mission Freelances"
IS_REMOTE_ONLY = False

SEARCH_URL = "https://www.mission-freelances.fr/missions/"

MIN_TITLE_LENGTH = 15


def fetch() -> list[dict]:
    """Tente de récupérer les offres Mission Freelances. Retourne [] si la page est illisible."""
    soup = fetch_html(SEARCH_URL, SOURCE_NAME)
    if soup is None:
        return []

    missions = []
    seen_urls = set()

    for link in soup.find_all("a", href=True):
        href = link["href"]
        if "/missions/" not in href and "/mission/" not in href:
            continue

        title = link.get_text(separator=" ", strip=True)
        title = " ".join(title.split())
        if len(title) < MIN_TITLE_LENGTH:
            continue

        url = urljoin(SEARCH_URL, href)
        if url == SEARCH_URL or url in seen_urls:
            continue
        seen_urls.add(url)

        missions.append(
            {
                "title": title,
                "url": url,
                "description": clean_html(link.get("title", "")),
                "source": SOURCE_NAME,
                "date_posted": "",
            }
        )

    if not missions:
        logger.warning(
            "[%s] aucune offre détectée sur %s — la structure HTML du site a probablement changé, "
            "source ignorée pour ce scan (voir commentaire en tête de fichier)",
            SOURCE_NAME,
            SEARCH_URL,
        )
    else:
        logger.info("[%s] %d offres détectées (scraping best-effort)", SOURCE_NAME, len(missions))

    return missions
