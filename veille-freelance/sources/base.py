"""
Utilitaires mutualisés pour toutes les sources : récupération HTTP avec
User-Agent identifiable, respect du robots.txt, parsing RSS générique,
et nettoyage du HTML des descriptions.

Chaque source doit rester tolérante aux pannes : en cas d'erreur (flux mort,
timeout, structure changée...), on logue et on retourne une liste vide,
sans jamais faire planter le scan global.
"""
from __future__ import annotations

import logging
import re
from urllib.parse import urlparse
from urllib.robotparser import RobotFileParser

import feedparser
import requests
from bs4 import BeautifulSoup

import config

logger = logging.getLogger(__name__)

_robots_cache: dict[str, RobotFileParser] = {}

HTML_TAG_RE = re.compile(r"<[^>]+>")
WHITESPACE_RE = re.compile(r"\s+")


def can_fetch(url: str) -> bool:
    """Vérifie le robots.txt du domaine avant de récupérer une URL."""
    parsed = urlparse(url)
    domain = f"{parsed.scheme}://{parsed.netloc}"

    if domain not in _robots_cache:
        rp = RobotFileParser()
        rp.set_url(f"{domain}/robots.txt")
        try:
            # On récupère nous-mêmes le robots.txt avec notre User-Agent (via `requests`),
            # au lieu de rp.read() qui utilise urllib sans en-têtes : certains sites
            # (ex: Remotive) renvoient une 403 aux requêtes sans User-Agent identifiable,
            # ce que RobotFileParser interprète alors par erreur comme "tout est interdit".
            response = requests.get(
                f"{domain}/robots.txt",
                headers={"User-Agent": config.USER_AGENT},
                timeout=config.REQUEST_TIMEOUT,
            )
            if response.status_code >= 400:
                raise requests.RequestException(f"HTTP {response.status_code}")
            rp.parse(response.text.splitlines())
        except requests.RequestException as e:
            # Si le robots.txt est inaccessible, on n'entrave pas la source pour autant.
            logger.warning("robots.txt illisible pour %s (%s) : on continue par défaut (autorisé)", domain, e)
            rp.parse([])  # aucune règle chargée = tout autorisé par défaut
        _robots_cache[domain] = rp

    return _robots_cache[domain].can_fetch(config.USER_AGENT, url)


def clean_html(raw_html: str, max_chars: int = 220) -> str:
    """Retire les balises HTML d'une description et la tronque proprement."""
    if not raw_html:
        return ""
    text = HTML_TAG_RE.sub(" ", raw_html)
    text = WHITESPACE_RE.sub(" ", text).strip()
    if len(text) > max_chars:
        text = text[:max_chars].rsplit(" ", 1)[0] + "…"
    return text


def fetch_feed(url: str, source_name: str) -> feedparser.FeedParserDict | None:
    """
    Récupère et parse un flux RSS/Atom.
    Retourne None (et logue l'erreur) en cas d'échec, sans lever d'exception.
    """
    if not can_fetch(url):
        logger.warning("[%s] robots.txt interdit l'accès à %s — source ignorée", source_name, url)
        return None

    headers = {"User-Agent": config.USER_AGENT}
    try:
        response = requests.get(url, headers=headers, timeout=config.REQUEST_TIMEOUT)
        response.raise_for_status()
    except requests.RequestException as e:
        logger.error("[%s] échec de récupération du flux %s : %s", source_name, url, e)
        return None

    feed = feedparser.parse(response.content)
    if feed.bozo and not feed.entries:
        logger.error(
            "[%s] flux invalide ou vide à %s (%s)", source_name, url, feed.get("bozo_exception")
        )
        return None

    return feed


def fetch_html(url: str, source_name: str) -> BeautifulSoup | None:
    """
    Récupère et parse une page HTML publique (pour les sources sans flux RSS).
    Retourne None (et logue l'erreur) en cas d'échec, sans lever d'exception.
    """
    if not can_fetch(url):
        logger.warning("[%s] robots.txt interdit l'accès à %s — source ignorée", source_name, url)
        return None

    headers = {"User-Agent": config.USER_AGENT}
    try:
        response = requests.get(url, headers=headers, timeout=config.REQUEST_TIMEOUT)
        response.raise_for_status()
    except requests.RequestException as e:
        logger.error("[%s] échec de récupération de la page %s : %s", source_name, url, e)
        return None

    return BeautifulSoup(response.text, "html.parser")


def entry_to_mission(entry, source_name: str) -> dict:
    """Normalise une entrée feedparser en dict mission standard."""
    title = getattr(entry, "title", "").strip()
    url = getattr(entry, "link", "").strip()
    description = clean_html(getattr(entry, "summary", "") or getattr(entry, "description", ""))
    date_posted = getattr(entry, "published", "") or getattr(entry, "updated", "")
    return {
        "title": title,
        "url": url,
        "description": description,
        "source": source_name,
        "date_posted": date_posted,
    }
