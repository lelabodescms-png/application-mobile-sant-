# Guide de déploiement — pour un solo founder sans équipe technique

Ce guide suppose que vous ne codez pas vous-même. Chaque étape se fait via
l'interface web d'un service (pas de terminal), sauf la toute dernière
(copier des variables d'environnement).

Stack recommandée — tout en offre gratuite ou quasi-gratuite pour démarrer,
sans serveur à gérer :

| Besoin | Service recommandé | Pourquoi |
|---|---|---|
| Hébergement de l'application | [Vercel](https://vercel.com) | Fait par les créateurs de Next.js, déploiement en un clic depuis GitHub, HTTPS automatique, gratuit pour ce volume. |
| Base de données | [Neon](https://neon.tech) | PostgreSQL gratuit, compatible serverless, connexion en 2 minutes. |
| Stockage des photos | [Cloudflare R2](https://developers.cloudflare.com/r2/) | 10 Go gratuits, compatible S3, pas de frais de sortie de données. |
| Envoi de SMS | [Twilio](https://www.twilio.com) | Standard du marché, facturation à l'usage, pas de numéro à acheter grâce à l'Alphanumeric Sender ID. |

## 1. Créer la base de données (Neon)

1. Créer un compte sur neon.tech (gratuit).
2. Créer un projet → copier la "Connection string" (commence par `postgresql://`).
3. Garder cette valeur de côté, elle ira dans `DATABASE_URL`.

## 2. Créer le stockage des photos (Cloudflare R2)

1. Créer un compte Cloudflare, aller dans R2 → créer un bucket (ex : `couvreur-app-photos`).
2. Rendre le bucket public (Settings → Public access) ou associer un domaine
   personnalisé — c'est cette URL qui ira dans `S3_PUBLIC_URL_BASE`.
3. Créer un jeton API R2 (Manage R2 API Tokens) → récupérer `Access Key ID` et
   `Secret Access Key`.
4. L'endpoint est de la forme `https://<account-id>.r2.cloudflarestorage.com`.

Si vous préférez repousser cette étape, l'application fonctionne quand même
sans stockage S3 configuré (les photos restent sur le disque local), mais
elles seront perdues à chaque redéploiement sur Vercel — à ne garder que
pour un premier test, pas pour de vrais clients.

## 3. Créer le compte SMS (Twilio)

1. Créer un compte sur twilio.com.
2. Récupérer `Account SID` et `Auth Token` depuis le tableau de bord principal.

Pas besoin d'acheter de numéro de téléphone : le SMS est envoyé avec le nom
de l'entreprise de chaque couvreur comme expéditeur (Alphanumeric Sender
ID — ex : un SMS de "CouvDupont" plutôt que d'un numéro), généré
automatiquement par l'application à partir du nom saisi à l'inscription.
Ce mode ne nécessite aucune inscription préalable pour envoyer vers la
France (contrairement à d'autres pays où Twilio l'exige).

Comme pour le stockage, vous pouvez déployer sans Twilio configuré pour
tester d'abord : le lien s'affichera à l'écran au lieu d'être envoyé par SMS.

## 4. Déployer l'application (Vercel)

1. Le code doit être sur GitHub (déjà le cas dans ce dépôt).
2. Sur vercel.com → "Add New Project" → sélectionner ce dépôt GitHub.
3. Dans "Root Directory", indiquer `couvreur-app` (l'application n'est pas
   à la racine du dépôt).
4. Renseigner les variables d'environnement (Settings → Environment Variables) :

   | Variable | Valeur |
   |---|---|
   | `DATABASE_URL` | La chaîne de connexion Neon de l'étape 1 |
   | `AUTH_SECRET` | Une valeur aléatoire longue — la générer sur [1password.com/password-generator](https://1password.com/password-generator/) (64 caractères) ou demander à votre développeur de la générer |
   | `APP_URL` | L'URL Vercel de votre app une fois déployée (ex : `https://couvreur-app.vercel.app`) |
   | `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN` | Depuis l'étape 3 |
   | `S3_ENDPOINT`, `S3_BUCKET`, `S3_ACCESS_KEY_ID`, `S3_SECRET_ACCESS_KEY`, `S3_PUBLIC_URL_BASE` | Depuis l'étape 2 |

5. Cliquer "Deploy". Vercel construit et met en ligne l'application.
6. Une fois en ligne, appliquer le schéma de base de données une seule fois
   (nécessite qu'un développeur exécute `npx prisma migrate deploy` avec le
   `DATABASE_URL` de production — c'est la seule étape qui demande un
   terminal).

## Après le déploiement

- Testez vous-même le parcours complet avec votre propre numéro avant
  d'inviter un couvreur : inscription, "Appel manqué", réception du SMS,
  remplissage du formulaire avec une vraie photo, apparition dans le
  dashboard, changement de statut.
- Chaque redéploiement (nouveau code poussé sur GitHub) est automatique sur
  Vercel — aucune action de votre part.
