"use client";

import { useState, useTransition } from "react";
import { createMissedCall, type MissedCallState } from "@/app/actions/requests";

export function MissedCallForm() {
  const [open, setOpen] = useState(false);
  const [state, setState] = useState<MissedCallState>({});
  const [isPending, startTransition] = useTransition();

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const formData = new FormData(form);
    startTransition(async () => {
      const result = await createMissedCall({}, formData);
      setState(result);
      if (result.success) {
        form.reset();
      }
    });
  }

  if (!open) {
    return (
      <button
        onClick={() => {
          setState({});
          setOpen(true);
        }}
        className="w-full rounded-xl bg-red-600 px-4 py-4 text-center text-base font-semibold text-white shadow-sm transition-colors hover:bg-red-700"
      >
        📵 Appel manqué — envoyer le lien au client
      </button>
    );
  }

  return (
    <div className="rounded-xl border border-zinc-200 bg-white p-5 shadow-sm">
      <h2 className="text-sm font-semibold text-zinc-900">Numéro du client</h2>
      <p className="mt-1 text-xs text-zinc-500">
        Il recevra un SMS avec un lien pour décrire ses travaux et envoyer des photos.
      </p>

      <form onSubmit={handleSubmit} className="mt-3 flex flex-col gap-3">
        <input
          name="clientPhone"
          type="tel"
          required
          placeholder="06 12 34 56 78"
          autoFocus
          className="rounded-lg border border-zinc-300 px-3 py-2 text-sm outline-none focus:border-zinc-900"
        />

        {state.error && (
          <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{state.error}</p>
        )}

        {state.success && (
          <div className="rounded-lg bg-green-50 px-3 py-2 text-sm text-green-800">
            <p>SMS envoyé au client.</p>
            {state.simulated && (
              <p className="mt-1 break-all text-xs text-green-700">
                (Mode démo — aucun fournisseur SMS configuré. Lien : {state.link})
              </p>
            )}
          </div>
        )}

        <div className="flex gap-2">
          <button
            type="submit"
            disabled={isPending}
            className="rounded-lg bg-brand px-4 py-2 text-sm font-medium text-white hover:bg-brand-dark disabled:opacity-50"
          >
            {isPending ? "Envoi..." : "Envoyer le SMS"}
          </button>
          <button
            type="button"
            onClick={() => setOpen(false)}
            className="rounded-lg px-4 py-2 text-sm font-medium text-zinc-500 hover:text-zinc-900"
          >
            Annuler
          </button>
        </div>
      </form>
    </div>
  );
}
