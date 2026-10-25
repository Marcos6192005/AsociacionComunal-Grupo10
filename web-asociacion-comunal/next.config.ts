import type { NextConfig } from "next"
import path from "node:path"
import { fileURLToPath } from "node:url"

const nextConfig: NextConfig = {
  // A package-lock.json in the user home made Turbopack watch that
  // directory and the dev compile never finished.
  turbopack: {
    root: path.dirname(fileURLToPath(import.meta.url)),
  },
}

export default nextConfig
