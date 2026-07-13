import { redirect } from "next/navigation";
import { getSession, destroySession } from "@/lib/auth";
import { db } from "@/lib/db";

export default async function DashboardLayout({ children }: { children: React.ReactNode }) {
  const session = await getSession();
  if (!session) {
    redirect("/connexion");
  }

  const roofer = await db.roofer.findUnique({ where: { id: session.rooferId } });
  if (!roofer) {
    redirect("/connexion");
  }

  async function logoutAction() {
    "use server";
    await destroySession();
    redirect("/connexion");
  }

  return (
    <div className="flex min-h-full flex-1 flex-col">
      <header className="border-b border-zinc-200 bg-white">
        <div className="mx-auto flex max-w-4xl items-center justify-between px-4 py-4">
          <div>
            <p className="text-sm font-semibold text-zinc-900">{roofer.companyName}</p>
            <p className="text-xs text-zinc-400">@{roofer.username}</p>
          </div>
          <form action={logoutAction}>
            <button type="submit" className="text-sm text-zinc-500 underline hover:text-zinc-900">
              Déconnexion
            </button>
          </form>
        </div>
      </header>
      <main className="mx-auto w-full max-w-4xl flex-1 px-4 py-8">{children}</main>
    </div>
  );
}
