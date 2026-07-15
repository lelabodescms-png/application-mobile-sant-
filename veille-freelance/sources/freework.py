"""
Source : FreeWork (ex-Freelance-info.fr / Carriere-info.fr, aujourd'hui
free-work.com).

⚠️ Statut V1 : AUCUN flux RSS public fiable n'a été identifié pour ce site
(l'ancien flux Feedly de freelance-info.fr est mort depuis le rebranding).
Ce module tente donc un scraping HTML minimal et respectueux (robots.txt +
User-Agent identifiable) de la page de recherche publique. Si la structure
de la page ne correspond plus aux sélecteurs ci-dessous (site qui change
souvent), la source se désactive proprement : elle logue un avertissement
et retourne une liste vide, sans jamais faire planter le scan.

Si tu retrouves un flux RSS qui fonctionne vraiment, remplace directement
la fonction fetch() par un appel à fetch_feed() comme dans sources/codeur.py.
"""
import logging
from urllib.parse import urljoin

from sources.base import clean_html, fetch_html

logger = logging.getLogger(__name__)

SOURCE_NAME = "FreeWork"
IS_REMOTE_ONLY = False

SEARCH_URL = "https://www.free-work.com/fr/tech-it/jobs"

# Longueur minimale d'un intitulé pour être considéré comme une offre
# (permet d'écarter les liens de navigation/menu qui n'ont rien à voir).
MIN_TITLE_LENGTH = 15


def fetch() -> list[dict]:
    """Tente de récupérer les offres FreeWork. Retourne [] si la page est illisible."""
    soup = fetch_html(SEARCH_URL, SOURCE_NAME)
    if soup is None:
        return []

    missions = []
    seen_urls = set()

    for link in soup.find_all("a", href=True):
        href = link["href"]
        if "/jobs/" not in href and "/missions/" not in href:
            continue

        title = link.get_text(separator=" ", strip=True)
        title = " ".join(title.split())
        if len(title) < MIN_TITLE_LENGTH:
            continue

        url = urljoin(SEARCH_URL, href)
        if url in seen_urls:
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
