"use client";

import { useState, useTransition } from "react";
import { resendMissedCallSms } from "@/app/actions/requests";

export function ResendButton({ requestId, link }: { requestId: string; link: string }) {
  const [isPending, startTransition] = useTransition();
  const [feedback, setFeedback] = useState<string | null>(null);

  return (
    <div className="mt-6 rounded-lg bg-zinc-50 px-4 py-3">
      <p className="text-sm text-zinc-500">
        Le client n&apos;a pas encore rempli le formulaire envoyé par SMS.
      </p>
      <div className="mt-3 flex flex-wrap items-center gap-2">
        <button
          disabled={isPending}
          onClick={() =>
            startTransition(async () => {
              try {
                await resendMissedCallSms(requestId);
                setFeedback("SMS renvoyé.");
              } catch {
                setFeedback("Échec de l'envoi. Vous pouvez copier le lien manuellement.");
              }
            })
          }
          className="rounded-lg bg-zinc-900 px-3 py-1.5 text-sm font-medium text-white hover:bg-zinc-700 disabled:opacity-50"
        >
          {isPending ? "Envoi..." : "Renvoyer le SMS"}
        </button>
        <button
          onClick={() => {
            navigator.clipboard.writeText(link);
            setFeedback("Lien copié.");
          }}
          className="rounded-lg bg-zinc-100 px-3 py-1.5 text-sm font-medium text-zinc-700 hover:bg-zinc-200"
        >
          Copier le lien
        </button>
        {feedback && <span className="text-sm text-green-700">{feedback}</span>}
      </div>
    </div>
  );
}
