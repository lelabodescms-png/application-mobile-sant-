-- CreateEnum
CREATE TYPE "RequestStatus" AS ENUM ('LIEN_ENVOYE', 'A_TRAITER', 'DEVIS_ENVOYE', 'ACCEPTE', 'REFUSE');

-- CreateEnum
CREATE TYPE "Urgency" AS ENUM ('FAIBLE', 'NORMALE', 'URGENTE');

-- CreateTable
CREATE TABLE "Roofer" (
    "id" TEXT NOT NULL,
    "username" TEXT NOT NULL,
    "passwordHash" TEXT NOT NULL,
    "companyName" TEXT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "Roofer_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Request" (
    "id" TEXT NOT NULL,
    "token" TEXT NOT NULL,
    "status" "RequestStatus" NOT NULL DEFAULT 'LIEN_ENVOYE',
    "rooferId" TEXT NOT NULL,
    "clientPhone" TEXT NOT NULL,
    "description" TEXT,
    "roofType" TEXT,
    "address" TEXT,
    "contactName" TEXT,
    "contactPhone" TEXT,
    "urgency" "Urgency",
    "submittedAt" TIMESTAMP(3),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "Request_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Photo" (
    "id" TEXT NOT NULL,
    "requestId" TEXT NOT NULL,
    "path" TEXT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "Photo_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "Roofer_username_key" ON "Roofer"("username");

-- CreateIndex
CREATE UNIQUE INDEX "Request_token_key" ON "Request"("token");

-- AddForeignKey
ALTER TABLE "Request" ADD CONSTRAINT "Request_rooferId_fkey" FOREIGN KEY ("rooferId") REFERENCES "Roofer"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Photo" ADD CONSTRAINT "Photo_requestId_fkey" FOREIGN KEY ("requestId") REFERENCES "Request"("id") ON DELETE CASCADE ON UPDATE CASCADE;
