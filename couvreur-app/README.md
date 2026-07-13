# Appel Manqué — MVP pour entreprises de couverture

Application web (Next.js) qui transforme un appel manqué en devis qualifié :
le couvreur clique sur "Appel manqué", saisit le numéro du client, un SMS
part avec un lien vers un formulaire (description des travaux, type de
toiture, adresse, coordonnées, urgence, photos). Le couvreur reçoit la
demande dans son espace et peut suivre son statut (À traiter, Devis envoyé,
Accepté, Refusé).

C'est une V1 volontairement honnête : rien n'est simulé côté client (SMS
réel si configuré, vrai formulaire, vraies photos, vrai suivi). Le seul
raccourci est que le déclenchement est **manuel** (le couvreur appuie sur le
bouton) plutôt qu'automatique via un standard téléphonique — cette
automatisation (Twilio + numéro virtuel) est la prochaine étape, une fois
que des couvreurs payants justifient cet investissement.

## Démarrer en local

Nécessite un serveur PostgreSQL accessible (local ou distant — voir
`DEPLOY.md` pour un fournisseur gratuit comme Neon).

```bash
npm install
cp .env.example .env      # puis renseigner DATABASE_URL au minimum
npx prisma migrate dev
npm run dev
```

L'application est disponible sur http://localhost:3000.

## Variables d'environnement (fichier `.env`)

Voir `.env.example` pour la liste complète et commentée. Les seules
obligatoires pour démarrer sont `DATABASE_URL` et `AUTH_SECRET` — Twilio et
le stockage S3 sont optionnels en développement (SMS journalisés dans la
console, photos stockées sur disque local).

| Variable | Description |
|---|---|
| `DATABASE_URL` | Connexion PostgreSQL. |
| `AUTH_SECRET` | Secret pour signer les cookies de session. **À changer avant toute mise en ligne réelle.** |
| `APP_URL` | URL publique de l'application, utilisée dans le lien envoyé par SMS. |
| `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN` | Identifiants Twilio pour l'envoi réel de SMS. Laissés vides, les SMS sont affichés dans les logs du serveur (mode démo), avec le lien affiché aussi à l'écran pour pouvoir tester sans compte Twilio. Pas besoin d'acheter de numéro de téléphone : le SMS est envoyé avec le nom de l'entreprise du couvreur comme expéditeur (Alphanumeric Sender ID), qui ne nécessite aucune inscription préalable pour envoyer vers la France. |
| `S3_ENDPOINT`, `S3_BUCKET`, `S3_ACCESS_KEY_ID`, `S3_SECRET_ACCESS_KEY`, `S3_PUBLIC_URL_BASE` | Stockage des photos sur un service S3-compatible (Cloudflare R2, AWS S3, OVH Object Storage). Laissés vides, les photos sont stockées sur le disque local — **à ne garder que pour un premier test**, car un disque local ne survit pas à un redéploiement sur un hébergeur serverless comme Vercel. |

## Déploiement en production

Voir `DEPLOY.md` — guide pas-à-pas pensé pour un solo founder sans
compétences techniques (Vercel + Neon + Cloudflare R2 + Twilio).

## Sécurité et fiabilité déjà en place

- Mots de passe hashés (bcrypt), sessions signées (JWT en cookie httpOnly).
- Verrouillage de compte après 5 tentatives de connexion échouées (15 min).
- Numéros de téléphone normalisés au format E.164 avant l'envoi SMS (sinon
  rejetés par la plupart des fournisseurs).
- Renvoi manuel du SMS et copie du lien si l'envoi échoue ou si le client
  l'a perdu.
- En-têtes de sécurité de base (anti-clickjacking, anti-sniffing MIME).
- Isolation stricte par couvreur (`tenant_id`) sur toutes les requêtes.
- Tous les formulaires (connexion, inscription, appel manqué, formulaire
  client) restent utilisables sans recharger la page après une erreur de
  saisie — un bug de ce type a été identifié et corrigé pendant le
  durcissement de cette V1 (voir historique Git), car il aurait bloqué
  silencieusement un client après une simple faute de frappe.

## Ce qui reste pour la V2 (hors scope de ce MVP)

- **Détection automatique de l'appel manqué** via un numéro virtuel (Twilio
  Voice ou équivalent) qui déclenche l'envoi du SMS sans action du
  couvreur — voir l'analyse de faisabilité discutée en amont de ce projet.

## Structure du projet

- `src/app/(inscription|connexion)` — création de compte / connexion couvreur
- `src/app/dashboard` — espace couvreur (bouton "Appel manqué", liste des
  demandes, détail + statut)
- `src/app/demande/[token]` — formulaire public rempli par le client (lien
  reçu par SMS), sans authentification
- `src/app/actions` — logique métier (Server Actions) : auth, création de
  demande, envoi SMS, mise à jour de statut
- `src/lib` — utilitaires partagés (base de données, session, SMS, tokens)
- `prisma/schema.prisma` — modèle de données
