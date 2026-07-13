import { randomBytes } from "crypto";

export function generateToken() {
  return randomBytes(16).toString("hex");
}
