import { db } from "@/lib/db";
import { ClientForm } from "./client-form";

export default async function DemandePage({
  params,
}: {
  params: Promise<{ token: string }>;
}) {
  const { token } = await params;
  const request = await db.request.findUnique({
    where: { token },
    include: { roofer: true },
  });

  if (!request) {
    return (
      <Message title="Lien invalide">
        Ce lien n&apos;existe pas ou a expiré. Contactez directement l&apos;entreprise pour
        obtenir un nouveau lien.
      </Message>
    );
  }

  if (request.submittedAt) {
    return (
      <Message title="Demande déjà envoyée">
        Vous avez déjà transmis votre demande à {request.roofer.companyName}. L&apos;entreprise
        vous recontactera rapidement.
      </Message>
    );
  }

  return <ClientForm token={token} companyName={request.roofer.companyName} />;
}

function Message({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="mx-auto flex w-full max-w-lg flex-1 flex-col items-center justify-center px-4 py-10 text-center">
      <h1 className="text-lg font-semibold text-zinc-900">{title}</h1>
      <p className="mt-2 text-sm text-zinc-500">{children}</p>
    </div>
  );
}
