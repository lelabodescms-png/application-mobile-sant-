import { RequestStatus, Urgency } from "@/generated/prisma/enums";

export const STATUS_LABELS: Record<RequestStatus, string> = {
  LIEN_ENVOYE: "Lien envoyé",
  A_TRAITER: "À traiter",
  DEVIS_ENVOYE: "Devis envoyé",
  ACCEPTE: "Accepté",
  REFUSE: "Refusé",
};

export const STATUS_COLORS: Record<RequestStatus, string> = {
  LIEN_ENVOYE: "bg-zinc-100 text-zinc-600",
  A_TRAITER: "bg-amber-100 text-amber-800",
  DEVIS_ENVOYE: "bg-blue-100 text-blue-800",
  ACCEPTE: "bg-green-100 text-green-800",
  REFUSE: "bg-red-100 text-red-800",
};

export const URGENCY_LABELS: Record<Urgency, string> = {
  FAIBLE: "Faible",
  NORMALE: "Normale",
  URGENTE: "Urgente",
};

export const URGENCY_COLORS: Record<Urgency, string> = {
  FAIBLE: "bg-zinc-100 text-zinc-600",
  NORMALE: "bg-blue-100 text-blue-800",
  URGENTE: "bg-red-100 text-red-800",
};

export const NEXT_STATUSES: RequestStatus[] = [
  RequestStatus.A_TRAITER,
  RequestStatus.DEVIS_ENVOYE,
  RequestStatus.ACCEPTE,
  RequestStatus.REFUSE,
];
