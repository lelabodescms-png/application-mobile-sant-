"""
Source : Remotive (offres remote internationales).

Toutes les offres Remotive sont 100% remote par nature : le filtre remote
de filters.py est donc court-circuité pour cette source (IS_REMOTE_ONLY = True).

Flux par défaut : catégorie marketing. Remotive propose d'autres flux par
catégorie (ex: /feed/design, /feed/product, /feed/software-dev) : ajoute-les
à FEED_URLS si tu veux élargir la veille.
"""
import logging

from sources.base import entry_to_mission, fetch_feed

logger = logging.getLogger(__name__)

SOURCE_NAME = "Remotive"
IS_REMOTE_ONLY = True

FEED_URLS = [
    "https://remotive.com/remote-jobs/feed/marketing",
]


def fetch() -> list[dict]:
    """Récupère les offres Remotive. Retourne [] si le flux est indisponible."""
    missions = []
    for feed_url in FEED_URLS:
        feed = fetch_feed(feed_url, SOURCE_NAME)
        if feed is None:
            continue
        for entry in feed.entries:
            missions.append(entry_to_mission(entry, SOURCE_NAME))
        logger.info("[%s] %d entrées récupérées depuis %s", SOURCE_NAME, len(feed.entries), feed_url)
    return missions
