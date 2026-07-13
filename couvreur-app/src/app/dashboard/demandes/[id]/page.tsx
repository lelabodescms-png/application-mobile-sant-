import Link from "next/link";
import Image from "next/image";
import { notFound } from "next/navigation";
import { getSession } from "@/lib/auth";
import { db } from "@/lib/db";
import { StatusButtons } from "./status-buttons";
import { STATUS_LABELS, STATUS_COLORS, URGENCY_LABELS, URGENCY_COLORS } from "@/lib/status";

export default async function RequestDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const session = await getSession();

  const request = await db.request.findFirst({
    where: { id, rooferId: session!.rooferId },
    include: { photos: true },
  });

  if (!request) {
    notFound();
  }

  return (
    <div className="flex flex-col gap-6">
      <Link href="/dashboard" className="text-sm text-zinc-500 hover:text-zinc-900">
        ← Retour aux demandes
      </Link>

      <div className="rounded-xl border border-zinc-200 bg-white p-6 shadow-sm">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h1 className="text-lg font-semibold text-zinc-900">
              {request.contactName || "Client (formulaire non rempli)"}
            </h1>
            <p className="text-sm text-zinc-500">
              {request.contactPhone || request.clientPhone}
            </p>
          </div>
          <span
            className={`shrink-0 rounded-full px-3 py-1 text-xs font-medium ${STATUS_COLORS[request.status]}`}
          >
            {STATUS_LABELS[request.status]}
          </span>
        </div>

        {!request.submittedAt ? (
          <p className="mt-6 rounded-lg bg-zinc-50 px-4 py-3 text-sm text-zinc-500">
            Le client n&apos;a pas encore rempli le formulaire envoyé par SMS.
          </p>
        ) : (
          <div className="mt-6 flex flex-col gap-4">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <Field label="Type de toiture" value={request.roofType} />
              <Field
                label="Urgence"
                value={
                  request.urgency && (
                    <span
                      className={`rounded-full px-2 py-0.5 text-xs font-medium ${URGENCY_COLORS[request.urgency]}`}
                    >
                      {URGENCY_LABELS[request.urgency]}
                    </span>
                  )
                }
              />
              <Field label="Adresse du chantier" value={request.address} className="sm:col-span-2" />
              <Field label="Description des travaux" value={request.description} className="sm:col-span-2" />
            </div>

            {request.photos.length > 0 && (
              <div>
                <p className="mb-2 text-xs font-medium uppercase text-zinc-400">Photos</p>
                <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
                  {request.photos.map((photo) => (
                    <a key={photo.id} href={photo.path} target="_blank" rel="noreferrer">
                      <Image
                        src={photo.path}
                        alt="Photo de la toiture"
                        width={200}
                        height={200}
                        className="aspect-square w-full rounded-lg object-cover"
                      />
                    </a>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </div>

      {request.submittedAt && (
        <div className="rounded-xl border border-zinc-200 bg-white p-6 shadow-sm">
          <p className="mb-3 text-sm font-semibold text-zinc-900">Statut de la demande</p>
          <StatusButtons requestId={request.id} currentStatus={request.status} />
        </div>
      )}
    </div>
  );
}

function Field({
  label,
  value,
  className,
}: {
  label: string;
  value: React.ReactNode;
  className?: string;
}) {
  return (
    <div className={className}>
      <p className="text-xs font-medium uppercase text-zinc-400">{label}</p>
      <p className="mt-0.5 text-sm text-zinc-800">{value || "—"}</p>
    </div>
  );
}
