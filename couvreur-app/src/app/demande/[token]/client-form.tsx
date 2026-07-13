"use client";

import { useState, useTransition } from "react";
import { submitClientForm } from "@/app/actions/public-request";

const ROOF_TYPES = [
  "Tuiles",
  "Ardoises",
  "Zinc",
  "Bac acier",
  "Toiture terrasse",
  "Autre",
];

export function ClientForm({ token, companyName }: { token: string; companyName: string }) {
  const [error, setError] = useState<string | undefined>();
  const [isPending, startTransition] = useTransition();

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);
    startTransition(async () => {
      const result = await submitClientForm(token, {}, formData);
      if (result?.error) {
        setError(result.error);
      }
    });
  }

  return (
    <div className="mx-auto w-full max-w-lg px-4 py-10">
      <h1 className="text-xl font-semibold text-zinc-900">
        Décrivez vos travaux à {companyName}
      </h1>
      <p className="mt-1 text-sm text-zinc-500">
        2 minutes suffisent. Vos informations et vos photos seront transmises directement au
        couvreur.
      </p>

      <form onSubmit={handleSubmit} className="mt-6 flex flex-col gap-4">
        <Field label="Votre nom">
          <input
            name="contactName"
            type="text"
            required
            className="input"
          />
        </Field>

        <Field label="Votre numéro de téléphone">
          <input name="contactPhone" type="tel" required className="input" />
        </Field>

        <Field label="Adresse du chantier">
          <input name="address" type="text" required className="input" />
        </Field>

        <Field label="Type de toiture">
          <select name="roofType" required defaultValue="" className="input">
            <option value="" disabled>
              Sélectionnez...
            </option>
            {ROOF_TYPES.map((type) => (
              <option key={type} value={type}>
                {type}
              </option>
            ))}
          </select>
        </Field>

        <Field label="Décrivez les travaux souhaités">
          <textarea
            name="description"
            required
            rows={4}
            placeholder="Ex : fuite au niveau de la cheminée, quelques tuiles cassées après la tempête..."
            className="input"
          />
        </Field>

        <fieldset>
          <legend className="mb-2 text-sm font-medium text-zinc-700">Niveau d&apos;urgence</legend>
          <div className="flex gap-4">
            {[
              { value: "FAIBLE", label: "Faible" },
              { value: "NORMALE", label: "Normale" },
              { value: "URGENTE", label: "Urgente" },
            ].map((option) => (
              <label key={option.value} className="flex items-center gap-1.5 text-sm text-zinc-700">
                <input type="radio" name="urgency" value={option.value} required />
                {option.label}
              </label>
            ))}
          </div>
        </fieldset>

        <Field label="Photos de la toiture (jusqu'à 6)">
          <input
            name="photos"
            type="file"
            accept="image/*"
            multiple
            capture="environment"
            className="text-sm"
          />
        </Field>

        {error && (
          <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>
        )}

        <button
          type="submit"
          disabled={isPending}
          className="mt-2 rounded-lg bg-brand px-4 py-3 text-sm font-semibold text-white transition-colors hover:bg-brand-dark disabled:opacity-50"
        >
          {isPending ? "Envoi en cours..." : "Envoyer ma demande"}
        </button>
      </form>
    </div>
  );
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <label className="flex flex-col gap-1">
      <span className="text-sm font-medium text-zinc-700">{label}</span>
      {children}
    </label>
  );
}
