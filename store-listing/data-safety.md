# Google Play — Formulaire "Sécurité des données" (Data safety)

Basé sur l'architecture réelle de l'app : Room (base SQLite locale) +
DataStore (préférences locales), aucun appel réseau, aucun SDK tiers,
aucune télémétrie.

## Question : "Votre application collecte-t-elle ou partage-t-elle des types
de données utilisateur requis ?"

**Réponse : Non.**

Toutes les données (historique de poids, repas, réglages, notifications)
sont stockées exclusivement en local sur l'appareil via Room et DataStore.
Aucune donnée n'est transmise à un serveur, ni à Google, ni à un tiers.

## Détail par catégorie (si Play Console demande une confirmation explicite)

| Catégorie                     | Collectée ? | Partagée ? | Note                                   |
|--------------------------------|:-----------:|:----------:|-----------------------------------------|
| Informations de santé/forme    | Non         | Non        | Stockée localement uniquement           |
| Identifiants                   | Non         | Non        | Pas de compte, pas d'e-mail             |
| Localisation                    | Non         | Non        | Non utilisée par l'app                  |
| Données personnelles           | Non         | Non        | —                                        |
| Activité de l'app / diagnostics| Non         | Non        | Aucun SDK analytics/crash-reporting      |
| Publicité                      | Non         | Non        | Aucune publicité, aucun SDK pub          |

## Pratiques de sécurité

- Chiffrement des données en transit : sans objet (aucune donnée envoyée)
- Suppression des données possible : oui — désinstaller l'app supprime toutes
  les données locales (base Room + DataStore)
- Révision indépendante : non applicable (pas de collecte)

## Lien vers la politique de confidentialité

Voir `docs/privacy-policy.html` dans ce dépôt — à héberger via GitHub Pages
(Settings → Pages → Source: branche `main`, dossier `/docs`) ou sur votre
propre domaine, puis à renseigner l'URL dans Play Console.
