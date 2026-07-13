"use server";

import { z } from "zod";
import { redirect } from "next/navigation";
import { db } from "@/lib/db";
import { createSession, destroySession, hashPassword, verifyPassword } from "@/lib/auth";

export type AuthState = { error?: string };

const signupSchema = z.object({
  companyName: z.string().trim().min(2, "Le nom de l'entreprise est trop court."),
  username: z
    .string()
    .trim()
    .toLowerCase()
    .min(3, "L'identifiant doit contenir au moins 3 caractères.")
    .regex(/^[a-z0-9._-]+$/, "L'identifiant ne peut contenir que des lettres, chiffres, points, tirets."),
  password: z.string().min(8, "Le mot de passe doit contenir au moins 8 caractères."),
});

export async function signup(_prevState: AuthState, formData: FormData): Promise<AuthState> {
  const parsed = signupSchema.safeParse({
    companyName: formData.get("companyName"),
    username: formData.get("username"),
    password: formData.get("password"),
  });

  if (!parsed.success) {
    return { error: parsed.error.issues[0]?.message ?? "Champs invalides." };
  }

  const { companyName, username, password } = parsed.data;

  const existing = await db.roofer.findUnique({ where: { username } });
  if (existing) {
    return { error: "Cet identifiant est déjà utilisé." };
  }

  const passwordHash = await hashPassword(password);
  const roofer = await db.roofer.create({
    data: { companyName, username, passwordHash },
  });

  await createSession(roofer.id);
  redirect("/dashboard");
}

const loginSchema = z.object({
  username: z.string().trim().toLowerCase().min(1, "Identifiant requis."),
  password: z.string().min(1, "Mot de passe requis."),
});

const MAX_LOGIN_ATTEMPTS = 5;
const LOCKOUT_DURATION_MS = 15 * 60 * 1000; // 15 minutes

export async function login(_prevState: AuthState, formData: FormData): Promise<AuthState> {
  const parsed = loginSchema.safeParse({
    username: formData.get("username"),
    password: formData.get("password"),
  });

  if (!parsed.success) {
    return { error: "Identifiant et mot de passe requis." };
  }

  const { username, password } = parsed.data;

  const roofer = await db.roofer.findUnique({ where: { username } });

  if (roofer?.lockedUntil && roofer.lockedUntil > new Date()) {
    const minutesLeft = Math.ceil((roofer.lockedUntil.getTime() - Date.now()) / 60000);
    return {
      error: `Trop de tentatives échouées. Réessayez dans ${minutesLeft} minute(s).`,
    };
  }

  const isValid = roofer && (await verifyPassword(password, roofer.passwordHash));

  if (!isValid) {
    if (roofer) {
      const attempts = roofer.failedLoginAttempts + 1;
      await db.roofer.update({
        where: { id: roofer.id },
        data: {
          failedLoginAttempts: attempts,
          lockedUntil: attempts >= MAX_LOGIN_ATTEMPTS ? new Date(Date.now() + LOCKOUT_DURATION_MS) : null,
        },
      });
    }
    return { error: "Identifiant ou mot de passe incorrect." };
  }

  await db.roofer.update({
    where: { id: roofer.id },
    data: { failedLoginAttempts: 0, lockedUntil: null },
  });

  await createSession(roofer.id);
  redirect("/dashboard");
}

export async function logout() {
  await destroySession();
  redirect("/connexion");
}
