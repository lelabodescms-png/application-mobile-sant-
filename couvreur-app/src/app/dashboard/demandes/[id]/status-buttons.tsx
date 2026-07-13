"use client";

import { useTransition } from "react";
import { updateRequestStatus } from "@/app/actions/requests";
import { RequestStatus } from "@/generated/prisma/enums";
import { NEXT_STATUSES, STATUS_LABELS } from "@/lib/status";

export function StatusButtons({
  requestId,
  currentStatus,
}: {
  requestId: string;
  currentStatus: RequestStatus;
}) {
  const [isPending, startTransition] = useTransition();

  return (
    <div className="flex flex-wrap gap-2">
      {NEXT_STATUSES.map((status) => (
        <button
          key={status}
          disabled={isPending || status === currentStatus}
          onClick={() => startTransition(() => updateRequestStatus(requestId, status))}
          className={`rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
            status === currentStatus
              ? "bg-brand text-white"
              : "bg-zinc-100 text-zinc-700 hover:bg-zinc-200"
          } disabled:opacity-50`}
        >
          {STATUS_LABELS[status]}
        </button>
      ))}
    </div>
  );
}
