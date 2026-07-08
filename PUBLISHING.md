# Publier Suivi Santé sur le Google Play Store

Ce document est le fil conducteur pour passer du code à l'app publiée.
Les étapes 1 à 4 sont déjà faites côté technique ; les étapes marquées
**[VOUS]** nécessitent votre action directe (identité, paiement, upload).

## 1. Signature de l'app — ✅ fait

- Un keystore de release a été généré et vous a été transmis séparément
  (fichier `.jks` + `credentials.txt`) — **ne le committez jamais**.
- `app/build.gradle.kts` lit la config de signature depuis des variables
  d'environnement (`ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_PASSWORD`,
  `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`) — jamais en dur dans le code.
- Un workflow GitHub Actions (`.github/workflows/android-release.yml`,
  déclenchement manuel) construit un `.aab` signé à partir de 4 secrets repo.

### **[VOUS]** Ajouter les 4 secrets GitHub

Dans le repo → **Settings → Secrets and variables → Actions → New repository
secret**, ajoutez (valeurs dans le fichier `credentials.txt` que vous avez
reçu) :

- `ANDROID_KEYSTORE_BASE64` (contenu du fichier `.jks.b64`)
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Puis lancez le workflow : **Actions → Android Release Build → Run workflow**.
Une fois terminé, téléchargez l'artefact `app-release-aab` — c'est le fichier
à uploader sur Play Console.

## 2. Contenu de la fiche Store — ✅ fait

Voir `store-listing/description-fr.md` (titre, descriptions, catégorie,
visuels à préparer) et `store-listing/data-safety.md` (réponses au
formulaire "Sécurité des données").

## 3. Politique de confidentialité — ✅ fait

`docs/privacy-policy.html` est prêt. **[VOUS]** Activez GitHub Pages :
Settings → Pages → Source = branche `main`, dossier `/docs`. L'URL sera du
type `https://lelabodescms-png.github.io/application-mobile-sant-/privacy-policy.html` —
à coller dans Play Console.

## 4. Vérifications techniques avant soumission

- `targetSdk` est actuellement 34 — **[VOUS]** vérifiez au moment de la
  soumission l'exigence Play Console en vigueur (elle évolue chaque année) et
  augmentez-la si nécessaire dans `app/build.gradle.kts`.
- `versionCode`/`versionName` sont à incrémenter à chaque nouvelle
  soumission (`app/build.gradle.kts`, bloc `defaultConfig`).

## 5. Étapes que seul vous pouvez faire

1. **[VOUS]** Créer un compte développeur Google Play : 25 $ (paiement),
   vérification d'identité — https://play.google.com/console/signup
2. **[VOUS]** Créer l'app dans Play Console, remplir la fiche avec le contenu
   de `store-listing/`, uploader icône/captures d'écran, coller l'URL de la
   politique de confidentialité.
3. **[VOUS]** Uploader le `.aab` signé (récupéré à l'étape 1) sur une piste
   de test interne d'abord, puis en production une fois validé.
4. **[VOUS]** Répondre au questionnaire de classification du contenu et au
   formulaire "Sécurité des données" (réponses prêtes dans
   `store-listing/data-safety.md`).
5. **[VOUS]** Soumettre pour review (généralement quelques heures à quelques
   jours pour une première publication).
