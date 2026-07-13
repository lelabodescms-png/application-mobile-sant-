/**
 * Envoi de SMS abstrait derrière Twilio. Sans identifiants Twilio configurés
 * (mode développement / pilote), le message est simplement journalisé au
 * lieu d'être envoyé, pour ne pas bloquer le reste du parcours.
 */
export async function sendSms(to: string, message: string) {
  const { TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, TWILIO_FROM_NUMBER } = process.env;

  if (!TWILIO_ACCOUNT_SID || !TWILIO_AUTH_TOKEN || !TWILIO_FROM_NUMBER) {
    console.log(`[SMS simulé] À: ${to}\nMessage: ${message}`);
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
        From: TWILIO_FROM_NUMBER,
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
