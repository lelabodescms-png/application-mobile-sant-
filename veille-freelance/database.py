"""
Couche d'accès à la base SQLite locale (missions.db).

Chaque offre détectée est stockée avec son URL (unique), un hash du titre
(pour la déduplication même si l'URL change légèrement), sa source, son
score de pertinence et son statut (nouvelle / vue / postulée).
"""
import hashlib
import logging
import sqlite3
from contextlib import contextmanager
from datetime import datetime, timezone

import config

logger = logging.getLogger(__name__)

SCHEMA = """
CREATE TABLE IF NOT EXISTS missions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    url TEXT NOT NULL UNIQUE,
    title_hash TEXT NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    source TEXT NOT NULL,
    date_posted TEXT,
    date_found TEXT NOT NULL,
    score INTEGER NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'nouvelle',
    notified INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_missions_title_hash ON missions(title_hash);
CREATE INDEX IF NOT EXISTS idx_missions_notified ON missions(notified);
"""


def title_hash(title: str) -> str:
    """Hash normalisé du titre, utilisé pour la déduplication."""
    normalized = title.strip().lower()
    return hashlib.sha256(normalized.encode("utf-8")).hexdigest()


@contextmanager
def get_connection():
    conn = sqlite3.connect(config.DB_PATH)
    conn.row_factory = sqlite3.Row
    try:
        yield conn
        conn.commit()
    finally:
        conn.close()


def init_db() -> None:
    """Crée la base et la table si elles n'existent pas encore."""
    with get_connection() as conn:
        conn.executescript(SCHEMA)
    logger.debug("Base de données initialisée (%s)", config.DB_PATH)


def mission_exists(url: str, title: str) -> bool:
    """Vérifie si une offre est déjà connue, par URL ou par hash de titre."""
    h = title_hash(title)
    with get_connection() as conn:
        row = conn.execute(
            "SELECT 1 FROM missions WHERE url = ? OR title_hash = ? LIMIT 1",
            (url, h),
        ).fetchone()
    return row is not None


def insert_mission(
    url: str,
    title: str,
    description: str,
    source: str,
    date_posted: str,
    score: int,
) -> None:
    """Insère une nouvelle offre en base (statut initial : nouvelle)."""
    with get_connection() as conn:
        try:
            conn.execute(
                """
                INSERT INTO missions
                    (url, title_hash, title, description, source, date_posted, date_found, score, status, notified)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'nouvelle', 0)
                """,
                (
                    url,
                    title_hash(title),
                    title,
                    description,
                    source,
                    date_posted,
                    datetime.now(timezone.utc).isoformat(),
                    score,
                ),
            )
        except sqlite3.IntegrityError:
            # Course possible entre deux sources qui référencent la même URL.
            logger.debug("Offre déjà présente (URL en doublon) : %s", url)


def get_unnotified_missions() -> list[sqlite3.Row]:
    """Retourne les offres pas encore envoyées dans un digest, triées par score."""
    with get_connection() as conn:
        rows = conn.execute(
            "SELECT * FROM missions WHERE notified = 0 ORDER BY score DESC, date_found DESC"
        ).fetchall()
    return rows


def mark_notified(mission_ids: list[int]) -> None:
    """Marque des offres comme notifiées, pour ne jamais les renvoyer dans un digest."""
    if not mission_ids:
        return
    with get_connection() as conn:
        conn.executemany(
            "UPDATE missions SET notified = 1 WHERE id = ?",
            [(mid,) for mid in mission_ids],
        )


def set_status(url: str, status: str) -> bool:
    """Change le statut d'une offre (nouvelle / vue / postulée). Retourne False si introuvable."""
    if status not in {"nouvelle", "vue", "postulée"}:
        raise ValueError(f"Statut invalide : {status}")
    with get_connection() as conn:
        cur = conn.execute("UPDATE missions SET status = ? WHERE url = ?", (status, url))
    return cur.rowcount > 0
