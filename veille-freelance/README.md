# Veille Freelance

Application Python de veille automatisée pour détecter des missions freelance
100% à distance (marché francophone + remote international), matchées contre
tes services : community management, création de sites web, design graphique,
marketing digital, Ads (Meta/TikTok/LinkedIn/Google), création d'applications,
UGC, et bonus (Klaviyo, CRM, email marketing, SEO).

Sources agrégées en V1 :
- **Codeur.com** (RSS)
- **Graphiste.com** (RSS)
- **Remotive** (RSS, remote international)
- **FreeWork** (désactivée par défaut — voir section 3)
- **Mission Freelances** (best-effort — idem)
- **Gmail** (alertes Indeed / Welcome to the Jungle, via API OAuth)

Malt et LinkedIn ne sont volontairement PAS scrapés (violation de leurs CGU).

---

## 1. Installation

```bash
cd veille-freelance
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

Copie ensuite le fichier d'exemple et renseigne tes clés :

```bash
cp .env.example .env
```

Édite `.env` :

| Variable | Description |
|---|---|
| `CONTACT_EMAIL` | Ton email, affiché dans le User-Agent des requêtes HTTP |
| `GMAIL_QUERY` | Requête Gmail pour filtrer les emails d'alerte (par défaut : Indeed + WTTJ) |
| `LOG_LEVEL` | `INFO` par défaut, passe à `DEBUG` pour plus de détails |

Le digest quotidien est envoyé **par email** (sur ta propre adresse Gmail),
et Gmail sert aussi de source pour lire tes alertes Indeed/WTTJ — les deux
utilisent la même autorisation OAuth, configurée en section 2 ci-dessous.

Note : `notifier.py` (envoi via un bot Telegram) existe toujours dans le
code si tu changes d'avis plus tard, mais n'est plus utilisé par défaut.

---

## 2. Configurer Gmail (OAuth) — lecture des alertes ET envoi du digest

Voir aussi les commentaires en tête de `sources/gmail_parser.py`.

1. Va sur [console.cloud.google.com](https://console.cloud.google.com/), crée
   un projet.
2. **API et services > Bibliothèque** → active **Gmail API**.
3. **API et services > Écran de consentement OAuth** :
   - Type d'utilisateur : *Externe*
   - Nom de l'app : "Veille Freelance", ton email en contact
   - Ajoute ton propre compte Gmail comme *utilisateur test*
4. **API et services > Identifiants > Créer des identifiants > ID client OAuth** :
   - Type d'application : **Application de bureau**
5. Télécharge le JSON, renomme-le `gmail_credentials.json`, place-le à la
   racine de `veille-freelance/` (à côté de `main.py`).
6. Configure des **alertes email** sur Indeed et Welcome to the Jungle pour
   tes recherches habituelles (fréquence quotidienne recommandée).
7. Lance, **depuis un terminal interactif** (pas via launchd) :
   ```bash
   python main.py --test-source gmail
   ```
   Un navigateur s'ouvre pour autoriser l'application (deux permissions
   demandées ensemble : lire tes emails et envoyer un email en ton nom —
   la seconde sert au digest quotidien). Une fois validé, un fichier
   `gmail_token.json` est créé — il sera réutilisé et rafraîchi
   automatiquement par les scans suivants, y compris en tâche de fond.
8. Teste l'envoi du digest :
   ```bash
   python main.py --digest
   ```
   (si la base est vide, tu verras juste "Aucune nouvelle offre à notifier" dans les logs — c'est normal tant qu'un scan n'a rien trouvé).

⚠️ Les templates HTML des emails d'alerte évoluent de temps en temps. Si
`--test-source gmail` ne remonte plus d'offres alors que tu reçois bien des
alertes, ouvre un des emails, inspecte les liens des offres, et ajuste les
motifs de détection dans `_extract_jobs_from_html()`
(`sources/gmail_parser.py`).

---

## 3. Tester chaque source individuellement

```bash
python main.py --test-source codeur
python main.py --test-source graphiste
python main.py --test-source remotive
python main.py --test-source mission_freelances
python main.py --test-source gmail
```

Chaque commande affiche les offres brutes récupérées, avec ✅/❌ selon si
elles passent les filtres (remote + mots-clés + exclusions) et leur score.
Rien n'est écrit en base — c'est un mode 100% sans effet de bord, idéal pour
valider qu'une source fonctionne avant de l'automatiser.

**Mission Freelances** n'a pas de flux RSS public confirmé : le module fait
un scraping HTML minimal et se désactive proprement (log + liste vide) si
la structure de la page a changé. Si `--test-source mission_freelances` ne
renvoie rien, regarde les logs (`logs/veille.log`) : soit robots.txt bloque
l'accès, soit la page a changé de structure (ajuste alors les sélecteurs
dans `sources/mission_freelances.py`).

**FreeWork** (`sources/freework.py`) est désactivée dans `main.py` : le site
charge ses offres en JavaScript, un scraping HTML simple n'y trouve que des
liens de filtres/catégories, jamais de vraies missions. Le fichier reste
dans le dépôt (testable isolément en le réimportant dans `main.py`) au cas
où une vraie API/RSS deviendrait disponible plus tard.

---

## 4. Lancer un scan complet manuellement

```bash
python main.py --now
```

Ceci enchaîne : scan de toutes les sources → filtrage → dédup → stockage →
envoi immédiat du digest par email des nouvelles offres.

Pour un usage normal en deux temps (scan seul / digest seul), voir section 7.

---

## 5. Ajouter une nouvelle source RSS

1. Crée `sources/ma_source.py` sur le modèle de `sources/codeur.py` :
   ```python
   from sources.base import entry_to_mission, fetch_feed

   SOURCE_NAME = "Ma Source"
   IS_REMOTE_ONLY = False  # True si la source ne liste que des offres 100% remote
   FEED_URLS = ["https://exemple.com/rss"]

   def fetch() -> list[dict]:
       missions = []
       for feed_url in FEED_URLS:
           feed = fetch_feed(feed_url, SOURCE_NAME)
           if feed is None:
               continue
           missions.extend(entry_to_mission(e, SOURCE_NAME) for e in feed.entries)
       return missions
   ```
2. Enregistre-la dans `main.py`, dans le dict `SOURCES` :
   ```python
   from sources import ma_source
   SOURCES = {
       ...,
       "ma_source": ma_source,
   }
   ```
3. Teste avec `python main.py --test-source ma_source`.

---

## 6. Éditer les mots-clés de matching

Tout se passe dans **`keywords.yaml`**, sans toucher au code :

- `remote.inclusion` / `remote.exclusion` / `remote.exception_si_present` :
  règles de détection du télétravail.
- `services.<catégorie>` : mots-clés FR/EN par service (ajoute/enlève des
  lignes librement — pas besoin des accents, la comparaison les ignore).
- `exclusions` : mots-clés qui rejettent systématiquement une offre (stage,
  alternance, CDI uniquement...).

Le fichier est rechargé à chaque lancement de `main.py` (pas besoin de
redémarrer un process qui tournerait en continu, puisque le scan est
relancé périodiquement par launchd).

---

## 7. Exécution automatique (launchd, macOS)

Deux tâches séparées :
- **scan** : toutes les 4 heures → fetch + filtre + stockage (pas de notif)
- **digest** : tous les jours à 9h00 → envoi du digest par email

### Installation

1. Édite les deux fichiers dans `launchd/` et remplace
   `/Users/TON_UTILISATEUR/veille-freelance` par le chemin réel vers ce
   dossier (tape `pwd` dans le dossier `veille-freelance/` pour l'obtenir).
2. Copie-les dans `~/Library/LaunchAgents/` :
   ```bash
   cp launchd/com.labodescms.veillefreelance.scan.plist ~/Library/LaunchAgents/
   cp launchd/com.labodescms.veillefreelance.digest.plist ~/Library/LaunchAgents/
   ```
3. Charge les deux tâches :
   ```bash
   launchctl load ~/Library/LaunchAgents/com.labodescms.veillefreelance.scan.plist
   launchctl load ~/Library/LaunchAgents/com.labodescms.veillefreelance.digest.plist
   ```
4. Vérifie qu'elles sont bien chargées :
   ```bash
   launchctl list | grep veillefreelance
   ```
5. Les logs d'exécution sont dans `logs/` (`launchd-scan.out.log`,
   `launchd-digest.out.log`, etc.) et dans `logs/veille.log` (log applicatif
   détaillé).

### Désinstaller / mettre en pause

```bash
launchctl unload ~/Library/LaunchAgents/com.labodescms.veillefreelance.scan.plist
launchctl unload ~/Library/LaunchAgents/com.labodescms.veillefreelance.digest.plist
```

### Notes

- launchd ne tourne que pendant les sessions actives (le Mac doit être
  allumé ; s'il est en veille au moment prévu, macOS rattrape le job au
  réveil dans la plupart des cas mais ce n'est pas garanti à 100%).
- La tâche Gmail (lecture des alertes ET envoi du digest) nécessite d'avoir
  déjà autorisé l'app une première fois en interactif (section 2) : launchd
  ne peut pas ouvrir de navigateur.

---

## 8. Structure du projet

```
veille-freelance/
├── README.md
├── requirements.txt
├── .env.example / .env
├── keywords.yaml            # mots-clés + règles remote (éditable)
├── config.py                # constantes, chargement .env
├── database.py              # SQLite (missions.db)
├── filters.py                # remote / mots-clés / score / exclusions
├── email_notifier.py         # digest quotidien par email (API Gmail)
├── notifier.py                # digest Telegram (non utilisé par défaut, gardé au cas où)
├── main.py                   # CLI : scan / digest / --now / --test-source
├── sources/
│   ├── base.py                # utilitaires HTTP/RSS/HTML mutualisés
│   ├── codeur.py
│   ├── graphiste.py
│   ├── remotive.py
│   ├── freework.py             # désactivée par défaut (voir section 3)
│   ├── mission_freelances.py
│   └── gmail_parser.py         # lecture des alertes + identifiants OAuth partagés
└── launchd/
    ├── com.labodescms.veillefreelance.scan.plist
    └── com.labodescms.veillefreelance.digest.plist
```

---

## 9. Base de données

`missions.db` (SQLite, créée automatiquement) — table `missions` :

| Colonne | Description |
|---|---|
| `url` | URL de l'offre (unique) |
| `title_hash` | hash du titre normalisé (dédup même si l'URL change) |
| `title`, `description`, `source`, `date_posted` | métadonnées de l'offre |
| `score` | score de pertinence (mots-clés matchés + bonus titre + bonus freelance/mission) |
| `status` | `nouvelle` / `vue` / `postulée` |
| `notified` | 1 si déjà envoyée dans un digest (jamais renvoyée) |

---

## À venir (pas dans cette V1)

- Dashboard web local (Flask) pour parcourir les offres et marquer "postulée"
- Module de pré-rédaction de brouillon de candidature (positionnement Le
  Labo des CMs)
- Déploiement Railway/Render pour tourner même Mac fermé

Demande-moi ces évolutions quand tu es prêt·e.
