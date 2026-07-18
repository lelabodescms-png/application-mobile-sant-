/**
 * Envoi de SMS abstrait derrière Twilio. Sans identifiants Twilio configurés
 * (mode développement / pilote), le message est simplement journalisé au
 * lieu d'être envoyé, pour ne pas bloquer le reste du parcours.
 */
export async function sendSms(to: string, message: string, from: string) {
  const { TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN } = process.env;

  if (!TWILIO_ACCOUNT_SID || !TWILIO_AUTH_TOKEN) {
    console.log(`[SMS simulé] De: ${from} À: ${to}\nMessage: ${message}`);
    return { simulated: true as const };
  }

  const credentials = Buffer.from(`${TWILIO_ACCOUNT_SID}:${TWILIO_AUTH_TOKEN}`).toString("base64");

  const response = await fetch(
    `https://api.twilio.com/2010-04-01/Accounts/${TWILIO_ACCOUNT_SID}/Messages.json`,
    {
      method: "POST",
      headers: {
        Authorization: `Basic ${credentials}`,
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: new URLSearchParams({
        To: to,
        From: from,
        Body: message,
      }),
    }
  );

  if (!response.ok) {
    const errorBody = await response.text();
    throw new Error(`Échec de l'envoi du SMS (${response.status}): ${errorBody}`);
  }

  return { simulated: false as const };
}

/**
 * Envoie un SMS en essayant d'abord le Sender ID alphanumérique (nom de
 * l'entreprise) — accepté sans démarche en France, mais refusé par de
 * nombreux autres pays (ex : Maroc). En cas d'échec, on retente avec un
 * numéro de téléphone Twilio classique (TWILIO_FALLBACK_FROM_NUMBER),
 * accepté plus largement à l'international.
 */
export async function sendSmsWithFallback(to: string, message: string, senderId: string) {
  try {
    return await sendSms(to, message, senderId);
  } catch (error) {
    const fallbackNumber = process.env.TWILIO_FALLBACK_FROM_NUMBER;
    if (!fallbackNumber) {
      throw error;
    }
    console.error(`Sender ID alphanumérique refusé pour ${to}, tentative avec le numéro de secours.`, error);
    return sendSms(to, message, fallbackNumber);
  }
}
