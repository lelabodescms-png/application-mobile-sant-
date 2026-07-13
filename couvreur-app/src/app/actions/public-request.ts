"use server";

import { z } from "zod";
import path from "path";
import { redirect } from "next/navigation";
import { db } from "@/lib/db";
import { savePhoto } from "@/lib/storage";
import { RequestStatus, Urgency } from "@/generated/prisma/enums";

export type ClientFormState = { error?: string };

const MAX_PHOTOS = 6;
const MAX_PHOTO_SIZE = 8 * 1024 * 1024; // 8 Mo

const clientFormSchema = z.object({
  description: z.string().trim().min(5, "Merci de décrire brièvement les travaux."),
  roofType: z.string().trim().min(1, "Merci d'indiquer le type de toiture."),
  address: z.string().trim().min(5, "Merci d'indiquer l'adresse du chantier."),
  contactName: z.string().trim().min(2, "Merci d'indiquer votre nom."),
  contactPhone: z
    .string()
    .trim()
    .min(6, "Numéro de téléphone invalide.")
    .regex(/^[0-9+ .-]+$/, "Numéro de téléphone invalide."),
  urgency: z.enum([Urgency.FAIBLE, Urgency.NORMALE, Urgency.URGENTE]),
});

export async function submitClientForm(
  token: string,
  _prevState: ClientFormState,
  formData: FormData
): Promise<ClientFormState> {
  const request = await db.request.findUnique({ where: { token } });
  if (!request) {
    return { error: "Cette demande n'existe pas ou a expiré." };
  }
  if (request.submittedAt) {
    return { error: "Cette demande a déjà été envoyée." };
  }

  const parsed = clientFormSchema.safeParse({
    description: formData.get("description"),
    roofType: formData.get("roofType"),
    address: formData.get("address"),
    contactName: formData.get("contactName"),
    contactPhone: formData.get("contactPhone"),
    urgency: formData.get("urgency"),
  });

  if (!parsed.success) {
    return { error: parsed.error.issues[0]?.message ?? "Formulaire invalide." };
  }

  const photoFiles = formData
    .getAll("photos")
    .filter((entry): entry is File => entry instanceof File && entry.size > 0);

  if (photoFiles.length > MAX_PHOTOS) {
    return { error: `Vous pouvez envoyer au maximum ${MAX_PHOTOS} photos.` };
  }
  for (const file of photoFiles) {
    if (file.size > MAX_PHOTO_SIZE) {
      return { error: "Une des photos dépasse 8 Mo." };
    }
  }

  const savedPaths: string[] = [];
  for (const [index, file] of photoFiles.entries()) {
    const extension = path.extname(file.name) || ".jpg";
    const filename = `${index}-${Date.now()}${extension}`;
    const buffer = Buffer.from(await file.arrayBuffer());
    const url = await savePhoto(buffer, {
      token,
      filename,
      contentType: file.type || "image/jpeg",
    });
    savedPaths.push(url);
  }

  await db.request.update({
    where: { id: request.id },
    data: {
      description: parsed.data.description,
      roofType: parsed.data.roofType,
      address: parsed.data.address,
      contactName: parsed.data.contactName,
      contactPhone: parsed.data.contactPhone,
      urgency: parsed.data.urgency,
      status: RequestStatus.A_TRAITER,
      submittedAt: new Date(),
      photos: {
        create: savedPaths.map((p) => ({ path: p })),
      },
    },
  });

  redirect(`/demande/${token}/merci`);
}
