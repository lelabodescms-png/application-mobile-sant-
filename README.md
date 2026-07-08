# Handoff : Application de suivi santé personnel (Android)

## Overview
Application mobile Android de suivi santé personnel : suivi du poids, hydratation, activité physique, jeûne intermittent et journal alimentaire. 4 écrans + navigation par barre du bas.

## About the Design Files
Les fichiers de ce dossier sont des **références de design créées en HTML** (prototype interactif) — elles montrent le look final et le comportement attendu, ce ne sont **pas des fichiers à copier tels quels** dans l'app finale. La tâche consiste à **recréer ces designs HTML dans l'environnement natif Android du projet** (Kotlin/Jetpack Compose recommandé, ou Java/XML si le projet existant l'utilise déjà), en respectant les patterns et librairies déjà en place dans le codebase. Si aucun environnement n'existe encore, Jetpack Compose est le choix recommandé pour une app Android moderne.

## Fidelity
**Haute fidélité (hifi)** : le prototype fourni est pixel-perfect — couleurs, typographie, espacements et interactions sont définitifs. Le développeur doit recréer l'UI fidèlement avec les composants natifs Android (Compose `Canvas`/`Modifier` pour les anneaux de progression et graphiques, pas de WebView).

## Design Tokens

### Couleurs
- Fond app (noir bleuté) : `#08090C`
- Cartes (anthracite) : `#141519`
- Feuilles modales (bottom sheets) : `#1A1B20`
- Bordures de carte (très subtiles) : `rgba(255,255,255,0.07)`
- Texte principal : `#F5F5F7`
- Texte secondaire (dim) : `rgba(245,245,247,0.5)`
- Piste des anneaux (fond de la progress ring) : `#25262B`
- Piste douce (boutons -/+ neutres) : `#1F2024`
- **Accent or/doré (marque, poids)** : `#EAB63F` — texte sur fond doré : `#191307` (quasi-noir)
- **Bleu (eau)** : `#3B9EFF` / fond doux `rgba(59,158,255,0.16)`
- **Orange (activité)** : `#FF9448` / fond doux `rgba(255,148,72,0.16)`
- **Sarcelle (jeûne)** : `#2DD9A3` / fond doux `rgba(45,217,163,0.16)`
- **Violet (repas)** : `#A78BFA` / fond doux `rgba(167,139,250,0.16)`
- Vert positif (perte de poids, delta négatif) : `#4ADE80`
- Rouge (delta positif / prise de poids) : `#FF8080`

### Typographie
- Police : **Manrope** (Google Fonts)
- Titres d'écran : 700-800, 24-26px, letter-spacing -0.3px
- Valeur poids principale : 800, 44px
- Labels de carte : 700, 14-15px
- Corps / texte secondaire : 500-600, 12-14px
- Boutons pilule : 800, 16px

### Spacing & Forme
- Cartes : border-radius **20px**, padding 18-20px
- Petites cartes (historique, stats) : border-radius 16px
- Boutons pilule (pleine largeur) : border-radius **999px** (fully rounded), hauteur ~48-56px (padding 16px vertical)
- Feuilles modales (bottom sheets) : border-radius **24px 24px 0 0**, glissent du bas, overlay `rgba(0,0,0,0.55)`
- Anneaux de progression : rayon 47px, stroke-width 10px, stroke-linecap round, démarrent en haut (rotation -90deg), sens horaire
- Grille 2x2 : gap 14px
- Placeholder photo : motif rayé diagonal `repeating-linear-gradient(135deg, #232428, #232428 6px, #2b2c31 6px, #2b2c31 12px)` + texte "photo" centré, gris `rgba(245,245,247,0.4)`

## Screens / Views

### 1. Dashboard (Accueil)
**Purpose** : vue d'ensemble quotidienne — poids, hydratation, activité, jeûne, repas.

**Layout** :
- Header : "Bonjour" (26px/800) + date longue en dessous (14px/600, gris) ; icône cloche à droite dans un cercle 42px avec pastille dorée (notification)
- Carte Poids (pleine largeur, 20px radius) :
  - Ligne titre "Poids" + badge pilule doré "Objectif 70 kg"
  - Valeur actuelle (44px/800) + unité "kg" + à droite : delta texte vert ("+X kg à faire" ou "Objectif atteint !")
  - Sparkline SVG (courbe + zone remplie dorée translucide) sur 7 derniers points
  - Barre de progression horizontale (piste grise, remplissage doré) + texte "% de l'objectif atteint"
  - Bouton pilule pleine largeur doré "+ Peser aujourd'hui"
- Grille 2x2 (cartes 20px radius) :
  - **Eau** : anneau bleu, valeur "X.XX / objectif L" au centre, boutons − / + (fond neutre / fond bleu doux)
  - **Activité** : anneau orange, valeur "min / objectif" au centre, bouton pilule "+ Ajouter" (ouvre modal)
  - **Jeûne** : anneau sarcelle, valeur "Xh / objectif h" au centre, boutons − / +
  - **Repas** : gros cercle violet avec le nombre de repas du jour, label "Repas du jour", clic → navigue vers Journal

### 2. Écran Poids
**Layout** :
- Titre "Suivi du poids" (24px/800)
- 3 stat-cards côte à côte (Départ / Actuel / Objectif), 16px radius
- Carte graphique annuel : courbe + zone remplie dorée, ligne pointillée horizontale = objectif (`stroke-dasharray: 5,5`, couleur `rgba(245,245,247,0.35)`), labels mois en dessous (Jan/Mar/Mai/Jul)
- Bouton pilule doré "+ Ajouter une pesée"
- Liste "Historique" : chaque ligne = date (ex "6 jul.") + poids (16px/800) + delta coloré (vert si négatif/perte, rouge sinon), séparateur bordure subtile

### 3. Écran Journal
**Layout** :
- Titre "Journal des repas"
- Sélecteur de date horizontal scrollable : 7 derniers jours, chaque item = jour abrégé (jeu/ven/sam...) + numéro, item sélectionné = fond doré translucide + texte doré + bordure dorée
- Label date complète sélectionnée (ex "Mercredi 8 juillet 2026")
- Liste des repas du jour : carte (18px radius) = photo placeholder rayée 56x56 (14px radius) + nom du repas + heure + badge pilule violet = type de repas (Petit-déjeuner/Déjeuner/Dîner/Collation)
- État vide : "Aucun repas enregistré pour ce jour." centré, texte gris
- Bouton flottant "+" (cercle doré 58px, ombre dorée) en bas à droite → ouvre modal ajout de repas

### 4. Écran Réglages
**Layout** :
- Titre "Réglages"
- Section "OBJECTIF" (label majuscule, 12.5px/800, letter-spacing 1px, gris) : carte à 2 colonnes Départ / Objectif
- Section "NOTIFICATIONS" : carte avec 3 lignes, chacune = titre + sous-texte (fréquence) + interrupteur on/off (pill 48x28px, thumb blanc 22px qui glisse, couleur ON = teinte propre à chaque rappel : doré pour pesée, bleu pour hydratation, orange pour activité ; couleur OFF = gris piste)

### Navigation
Barre du bas fixe, 4 onglets (icônes outline + labels 11.5px/700) : Accueil, Poids, Journal, Réglages. Onglet actif = couleur or, inactifs = gris texte secondaire.

### Modales (bottom sheets)
Toutes glissent du bas, radius 24px en haut, overlay sombre cliquable pour fermer, poignée grise 40x4px centrée en haut.
- **Ajouter une pesée** : champ numérique (26px/800) + unité "kg", bouton "Enregistrer"
- **Ajouter de l'activité** : 3 boutons presets (+10/+20/+30 min, fond orange doux) + champ numérique manuel + unité "minutes", bouton "Ajouter"
- **Ajouter un repas** : sélecteur de type (pills, actif = violet), champ texte "Nom du repas", zone photo placeholder rayée 120px, bouton "Ajouter le repas"

## Interactions & Behavior
- Tap "+ Peser aujourd'hui" / "+ Ajouter une pesée" → ouvre modal poids. Si une pesée existe déjà aujourd'hui, elle est **remplacée** (pas de doublon) ; sinon nouvelle entrée ajoutée à l'historique. Le poids "Actuel" = toujours la dernière entrée de l'historique.
- Boutons − / + eau : incrémente/décrémente de 0.25 L, plancher à 0.
- Boutons − / + jeûne : incrémente/décrémente de 1h, plancher à 0.
- "+ Ajouter" activité → modal avec presets rapides ou saisie manuelle, **s'ajoute** à la valeur du jour (ne remplace pas).
- Sélecteur de date Journal : tap sur un jour → change le jour affiché, la liste de repas se met à jour, chaque jour a son propre state.
- Bouton flottant "+" Journal → ajoute un repas au jour actuellement sélectionné (pas forcément aujourd'hui), avec l'heure courante.
- Interrupteurs Réglages : toggle simple on/off, état persistant en mémoire.
- Anneaux de progression : `stroke-dashoffset` calculé dynamiquement = `circumference * (1 - min(1, valeur/objectif))`, capés à 100% visuellement même si la valeur réelle dépasse l'objectif (ex: activité peut dépasser 45 min sans casser l'anneau).

## State Management
- `activeTab`: 'accueil' | 'poids' | 'journal' | 'reglages'
- `weightHistory`: liste ordonnée chronologiquement `{date, weight}` — voir dataset ci-dessous
- `weightGoal`: 70
- `water` (L, courant), `waterGoal` (2.0)
- `activityMin` (courant), `activityGoal` (45)
- `fastingHr` (courant), `fastingGoal` (14)
- `meals`: dictionnaire `{ "YYYY-MM-DD": [{id, type, name, time}] }`
- `journalSelectedDate`
- `settings`: `{ weighIn: bool, hydration: bool, activity: bool }`
- État des modales (type ouvert + champs de saisie en cours)

### Dataset d'exemple (historique de poids, Jan → Jul 2026)
83.5, 82.9, 82.3, 81.7, 81.1, 80.5, 79.9, 79.3, 78.5, 77.7, 77.1, 76.6, 76.0, 75.6 kg (points bimensuels, du 5 janvier au 6 juillet 2026). Poids actuel = dernier point (75.6 kg), objectif = 70 kg.

### Dataset d'exemple (repas du jour initial)
- 07:30 — Yaourt grec & granola — Petit-déjeuner
- 12:45 — Salade poulet grillé & quinoa — Déjeuner

## Assets
- **Logo de l'app** : `assets/logo-forge.svg` / `assets/logo-forge.png` (512x512). Icône haltère stylisée en or (`#EAB63F`) sur badge arrondi anthracite/noir, dans l'esprit "salle de sport premium, masculin". Utilisé comme app icon ET affiché en badge (38-42px, radius 12px) dans le header de chaque écran (Accueil, Poids, Journal, Réglages), à gauche du titre — voir captures d'écran. À exporter aux résolutions Android standard (mipmap-hdpi/xhdpi/xxhdpi/xxxhdpi/xxxhdpi, adaptive icon foreground/background) pour l'icône de lanceur.
- Le reste des visuels est généré en code (SVG pour graphiques/anneaux, CSS pattern rayé pour les placeholders photo, icônes SVG dessinées à la main pour la nav et les réglages). Les vraies photos de repas seront à intégrer via un composant d'upload/galerie natif à la place des placeholders rayés.

## Files
- `Suivi Santé.dc.html` — prototype HTML complet et interactif (source de vérité pour le comportement et le visuel)
- `assets/logo-forge.svg`, `assets/logo-forge.png` — logo de l'app
- `screenshots/` — captures des 4 écrans (si demandées)
