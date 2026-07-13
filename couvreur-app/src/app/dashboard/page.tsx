import Link from "next/link";
import { getSession } from "@/lib/auth";
import { db } from "@/lib/db";
import { MissedCallForm } from "./missed-call-form";
import { STATUS_LABELS, STATUS_COLORS } from "@/lib/status";

export default async function DashboardPage() {
  const session = await getSession();
  const requests = await db.request.findMany({
    where: { rooferId: session!.rooferId },
    orderBy: { createdAt: "desc" },
    include: { photos: true },
  });

  const enAttenteDeReponse = requests.filter((r) => r.status === "A_TRAITER").length;

  return (
    <div className="flex flex-col gap-8">
      <MissedCallForm />

      <div>
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-sm font-semibold text-zinc-900">
            Demandes ({requests.length})
          </h2>
          {enAttenteDeReponse > 0 && (
            <span className="rounded-full bg-amber-100 px-3 py-1 text-xs font-medium text-amber-800">
              {enAttenteDeReponse} à traiter
            </span>
          )}
        </div>

        {requests.length === 0 ? (
          <p className="rounded-xl border border-dashed border-zinc-300 px-4 py-10 text-center text-sm text-zinc-400">
            Aucune demande pour le moment. Utilisez le bouton ci-dessus dès votre prochain appel
            manqué.
          </p>
        ) : (
          <ul className="flex flex-col gap-2">
            {requests.map((request) => (
              <li key={request.id}>
                <Link
                  href={`/dashboard/demandes/${request.id}`}
                  className="flex items-center justify-between rounded-xl border border-zinc-200 bg-white px-4 py-3 shadow-sm transition-colors hover:border-zinc-400"
                >
                  <div>
                    <p className="text-sm font-medium text-zinc-900">
                      {request.contactName || request.clientPhone}
                    </p>
                    <p className="text-xs text-zinc-500">
                      {request.address || "Formulaire non encore rempli"}
                      {request.photos.length > 0 && ` · ${request.photos.length} photo(s)`}
                    </p>
                  </div>
                  <span
                    className={`shrink-0 rounded-full px-3 py-1 text-xs font-medium ${STATUS_COLORS[request.status]}`}
                  >
                    {STATUS_LABELS[request.status]}
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
