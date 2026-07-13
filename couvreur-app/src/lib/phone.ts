/**
 * Normalise un numéro de téléphone français saisi sous forme locale
 * ("06 12 34 56 78", "06.12.34.56.78", "0612345678") vers le format E.164
 * ("+33612345678") requis par les fournisseurs SMS (Twilio et autres).
 *
 * Retourne null si le numéro ne peut pas être normalisé de façon fiable.
 */
export function toE164(rawPhone: string): string | null {
  const digitsOnly = rawPhone.replace(/[^\d+]/g, "");

  if (digitsOnly.startsWith("+")) {
    // Déjà au format international : on valide juste la longueur plausible.
    return /^\+\d{8,15}$/.test(digitsOnly) ? digitsOnly : null;
  }

  // Numéro français local à 10 chiffres commençant par 0 (ex: 0612345678)
  if (/^0\d{9}$/.test(digitsOnly)) {
    return `+33${digitsOnly.slice(1)}`;
  }

  return null;
}
