"use server";

import { z } from "zod";
import { revalidatePath } from "next/cache";
import { db } from "@/lib/db";
import { getSession } from "@/lib/auth";
import { generateToken } from "@/lib/token";
import { sendSms } from "@/lib/sms";
import { toE164 } from "@/lib/phone";
import { toSenderId } from "@/lib/sender-id";
import { RequestStatus } from "@/generated/prisma/enums";
import type { Roofer } from "@/generated/prisma/client";

export type MissedCallState = {
  error?: string;
  success?: boolean;
  link?: string;
  simulated?: boolean;
};

const missedCallSchema = z.object({
  clientPhone: z.string().trim().min(6, "Numéro de téléphone invalide."),
});

function buildMessage(roofer: Roofer, link: string) {
  return `Bonjour, vous avez essayé de joindre ${roofer.companyName}. Merci de décrire votre besoin ici (2 min) : ${link}`;
}

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

  const clientPhone = toE164(parsed.data.clientPhone);
  if (!clientPhone) {
    return {
      error: "Numéro de téléphone invalide. Utilisez un numéro français à 10 chiffres (ex : 06 12 34 56 78).",
    };
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
      clientPhone,
      status: RequestStatus.LIEN_ENVOYE,
    },
  });

  const appUrl = process.env.APP_URL ?? "http://localhost:3000";
  const link = `${appUrl}/demande/${token}`;

  let simulated = false;
  try {
    const result = await sendSms(clientPhone, buildMessage(roofer, link), toSenderId(roofer.companyName));
    simulated = result.simulated;
  } catch {
    return {
      error:
        "La demande a été créée mais l'envoi du SMS a échoué. Vous pouvez copier le lien manuellement depuis la fiche de la demande, ou réessayer l'envoi.",
    };
  }

  revalidatePath("/dashboard");
  return { success: true, link, simulated };
}

export async function resendMissedCallSms(requestId: string) {
  const session = await getSession();
  if (!session) {
    throw new Error("Vous devez être connecté.");
  }

  const request = await db.request.findFirst({
    where: { id: requestId, rooferId: session.rooferId },
    include: { roofer: true },
  });
  if (!request) {
    throw new Error("Demande introuvable.");
  }
  if (request.submittedAt) {
    throw new Error("Cette demande a déjà été remplie par le client.");
  }

  const appUrl = process.env.APP_URL ?? "http://localhost:3000";
  const link = `${appUrl}/demande/${request.token}`;

  await sendSms(request.clientPhone, buildMessage(request.roofer, link), toSenderId(request.roofer.companyName));

  revalidatePath(`/dashboard/demandes/${requestId}`);
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
