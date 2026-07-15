"""
Source : Codeur.com (projets freelance francophones).

Flux RSS public : https://www.codeur.com/projects?format=rss

Pour restreindre aux catégories qui t'intéressent (webmarketing, graphisme,
développement...), va sur https://www.codeur.com/projects, filtre par
catégorie dans l'interface, puis récupère le lien RSS correspondant
(généralement affiché en bas de page ou dans l'URL avec un paramètre
`category=`). Colle l'URL obtenue dans FEED_URLS ci-dessous.
"""
import logging

from sources.base import entry_to_mission, fetch_feed

logger = logging.getLogger(__name__)

SOURCE_NAME = "Codeur.com"
IS_REMOTE_ONLY = False

# Un ou plusieurs flux RSS Codeur.com. Par défaut : tous les projets
# (le filtrage remote + mots-clés se charge ensuite de ne garder que le pertinent).
FEED_URLS = [
    "https://www.codeur.com/projects?format=rss",
]


def fetch() -> list[dict]:
    """Récupère les projets Codeur.com. Retourne [] si le flux est indisponible."""
    missions = []
    for feed_url in FEED_URLS:
        feed = fetch_feed(feed_url, SOURCE_NAME)
        if feed is None:
            continue
        for entry in feed.entries:
            missions.append(entry_to_mission(entry, SOURCE_NAME))
        logger.info("[%s] %d entrées récupérées depuis %s", SOURCE_NAME, len(feed.entries), feed_url)
    return missions
