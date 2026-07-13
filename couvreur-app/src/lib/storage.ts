import { mkdir, writeFile } from "fs/promises";
import path from "path";
import { S3Client, PutObjectCommand } from "@aws-sdk/client-s3";

/**
 * Stockage des photos envoyées par les clients, abstrait derrière deux
 * implémentations :
 *
 * - Stockage S3-compatible (Cloudflare R2, AWS S3, OVH Object Storage...)
 *   si les variables S3_* sont configurées. Indispensable sur un hébergeur
 *   serverless où le disque local ne persiste pas entre deux requêtes.
 * - Disque local (public/uploads/) sinon — pratique en développement, ou
 *   suffisant pour un déploiement sur un serveur unique avec disque
 *   persistant.
 */

function getS3Config() {
  const {
    S3_ENDPOINT,
    S3_BUCKET,
    S3_ACCESS_KEY_ID,
    S3_SECRET_ACCESS_KEY,
    S3_REGION,
    S3_PUBLIC_URL_BASE,
  } = process.env;

  if (!S3_ENDPOINT || !S3_BUCKET || !S3_ACCESS_KEY_ID || !S3_SECRET_ACCESS_KEY || !S3_PUBLIC_URL_BASE) {
    return null;
  }

  return {
    endpoint: S3_ENDPOINT,
    bucket: S3_BUCKET,
    accessKeyId: S3_ACCESS_KEY_ID,
    secretAccessKey: S3_SECRET_ACCESS_KEY,
    region: S3_REGION || "auto",
    publicUrlBase: S3_PUBLIC_URL_BASE.replace(/\/$/, ""),
  };
}

export async function savePhoto(
  buffer: Buffer,
  { token, filename, contentType }: { token: string; filename: string; contentType: string }
): Promise<string> {
  const s3Config = getS3Config();
  const key = `uploads/${token}/${filename}`;

  if (s3Config) {
    const client = new S3Client({
      endpoint: s3Config.endpoint,
      region: s3Config.region,
      credentials: {
        accessKeyId: s3Config.accessKeyId,
        secretAccessKey: s3Config.secretAccessKey,
      },
    });

    await client.send(
      new PutObjectCommand({
        Bucket: s3Config.bucket,
        Key: key,
        Body: buffer,
        ContentType: contentType,
      })
    );

    return `${s3Config.publicUrlBase}/${key}`;
  }

  const uploadDir = path.join(process.cwd(), "public", "uploads", token);
  await mkdir(uploadDir, { recursive: true });
  await writeFile(path.join(uploadDir, filename), buffer);
  return `/uploads/${token}/${filename}`;
}
