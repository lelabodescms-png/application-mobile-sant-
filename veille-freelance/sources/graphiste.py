"""
Source : Graphiste.com (même groupe que Codeur.com, projets orientés design).

Flux RSS public : https://www.graphiste.com/projects?format=rss
"""
import logging

from sources.base import entry_to_mission, fetch_feed

logger = logging.getLogger(__name__)

SOURCE_NAME = "Graphiste.com"
IS_REMOTE_ONLY = False

FEED_URLS = [
    "https://www.graphiste.com/projects?format=rss",
]


def fetch() -> list[dict]:
    """Récupère les projets Graphiste.com. Retourne [] si le flux est indisponible."""
    missions = []
    for feed_url in FEED_URLS:
        feed = fetch_feed(feed_url, SOURCE_NAME)
        if feed is None:
            continue
        for entry in feed.entries:
            missions.append(entry_to_mission(entry, SOURCE_NAME))
        logger.info("[%s] %d entrées récupérées depuis %s", SOURCE_NAME, len(feed.entries), feed_url)
    return missions
