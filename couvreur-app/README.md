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

```bash
npm install
npx prisma migrate dev   # crée la base SQLite locale (déjà fait si dev.db existe)
npm run dev
```

L'application est disponible sur http://localhost:3000.

## Variables d'environnement (fichier `.env`)

| Variable | Description |
|---|---|
| `DATABASE_URL` | Base SQLite locale (`file:./dev.db`). À remplacer par une vraie base (Postgres) en production. |
| `AUTH_SECRET` | Secret pour signer les cookies de session. **À changer avant toute mise en ligne réelle.** |
| `APP_URL` | URL publique de l'application, utilisée dans le lien envoyé par SMS. |
| `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_FROM_NUMBER` | Identifiants Twilio pour l'envoi réel de SMS. **Laissés vides**, les SMS sont alors simplement affichés dans les logs du serveur (mode démo), avec le lien affiché aussi à l'écran pour pouvoir tester sans compte Twilio. |

Pour activer l'envoi réel de SMS : créer un compte sur [twilio.com](https://www.twilio.com),
acheter un numéro (idéalement un numéro géographique français), et renseigner
les trois variables Twilio ci-dessus.

## Ce qui manque avant une mise en production réelle

- **Hébergement des photos** : actuellement stockées sur le disque du
  serveur (`public/uploads/`). À remplacer par un stockage objet (S3,
  Cloudflare R2, OVH Object Storage) dès qu'il y a plusieurs couvreurs,
  car le disque local ne survit pas à un redéploiement sur la plupart des
  hébergeurs.
- **Base de données** : SQLite convient au développement, mais une vraie
  mise en ligne avec plusieurs couvreurs simultanés nécessite PostgreSQL
  (Prisma migre facilement vers ce provider).
- **`AUTH_SECRET`** : générer une vraie valeur aléatoire longue (ex. `openssl rand -base64 32`)
  avant toute mise en ligne, et ne jamais commiter le `.env`.
- **Détection automatique de l'appel manqué** : la V2, via un numéro
  virtuel (Twilio Voice ou équivalent) qui déclenche l'envoi du SMS sans
  action du couvreur.

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
