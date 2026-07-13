/**
 * Dérive un "Alphanumeric Sender ID" (nom d'expéditeur SMS, ex : "CouvDupont")
 * à partir du nom de l'entreprise du couvreur, pour que chaque client voie le
 * nom de l'entreprise qu'il a appelée plutôt qu'un numéro ou une marque
 * générique. Limité à 11 caractères alphanumériques (contrainte des
 * opérateurs), sans accents ni espaces.
 */
const DIACRITICS_REGEX = /[̀-ͯ]/g;

export function toSenderId(companyName: string): string {
  const normalized = companyName
    .normalize("NFD")
    .replace(DIACRITICS_REGEX, "")
    .replace(/[^a-zA-Z0-9]/g, "")
    .slice(0, 11);

  return normalized.length > 0 && /[a-zA-Z]/.test(normalized) ? normalized : "Couvreur";
}
