export type FormState = {
  mensaje: string | null
  ok: boolean
}

export const formStateInicial: FormState = { mensaje: null, ok: false }

export const MENSAJE_SIN_SERVIDOR =
  "No se pudo contactar el backend. Revisa que este corriendo en el puerto 8081."

export async function mensajeDeRespuesta(response: Response, fallback: string): Promise<string> {
  try {
    const body = (await response.json()) as { mensaje?: unknown }
    if (typeof body?.mensaje === "string" && body.mensaje.trim()) {
      return body.mensaje
    }
  } catch {
    // The body was not JSON.
  }
  return fallback
}
