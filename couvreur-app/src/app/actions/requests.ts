"use server";

import { z } from "zod";
import { revalidatePath } from "next/cache";
import { db } from "@/lib/db";
import { getSession } from "@/lib/auth";
import { generateToken } from "@/lib/token";
import { sendSms } from "@/lib/sms";
import { RequestStatus } from "@/generated/prisma/enums";

export type MissedCallState = {
  error?: string;
  success?: boolean;
  link?: string;
  simulated?: boolean;
};

const missedCallSchema = z.object({
  clientPhone: z
    .string()
    .trim()
    .min(6, "Numéro de téléphone invalide.")
    .regex(/^[0-9+ .-]+$/, "Numéro de téléphone invalide."),
});

export async function createMissedCall(
  _prevState: MissedCallState,
  formData: FormData
): Promise<MissedCallState> {
  const session = await getSession();
  if (!session) {
    return { error: "Vous devez être connecté." };
  }

  const parsed = missedCallSchema.safeParse({ clientPhone: formData.get("clientPhone") });
  if (!parsed.success) {
    return { error: parsed.error.issues[0]?.message ?? "Numéro invalide." };
  }

  const roofer = await db.roofer.findUnique({ where: { id: session.rooferId } });
  if (!roofer) {
    return { error: "Compte introuvable." };
  }

  const token = generateToken();
  await db.request.create({
    data: {
      token,
      rooferId: roofer.id,
      clientPhone: parsed.data.clientPhone,
      status: RequestStatus.LIEN_ENVOYE,
    },
  });

  const appUrl = process.env.APP_URL ?? "http://localhost:3000";
  const link = `${appUrl}/demande/${token}`;

  const message = `Bonjour, vous avez essayé de joindre ${roofer.companyName}. Merci de décrire votre besoin ici (2 min) : ${link}`;

  let simulated = false;
  try {
    const result = await sendSms(parsed.data.clientPhone, message);
    simulated = result.simulated;
  } catch {
    return { error: "Le lien a été créé mais l'envoi du SMS a échoué. Vous pouvez copier le lien manuellement." };
  }

  revalidatePath("/dashboard");
  return { success: true, link, simulated };
}

const statusValues = Object.values(RequestStatus) as [string, ...string[]];
const statusSchema = z.enum(statusValues);

export async function updateRequestStatus(requestId: string, status: string) {
  const session = await getSession();
  if (!session) {
    throw new Error("Vous devez être connecté.");
  }

  const parsedStatus = statusSchema.safeParse(status);
  if (!parsedStatus.success) {
    throw new Error("Statut invalide.");
  }

  const request = await db.request.findFirst({
    where: { id: requestId, rooferId: session.rooferId },
  });
  if (!request) {
    throw new Error("Demande introuvable.");
  }

  await db.request.update({
    where: { id: requestId },
    data: { status: parsedStatus.data as RequestStatus },
  });

  revalidatePath("/dashboard");
  revalidatePath(`/dashboard/demandes/${requestId}`);
}
