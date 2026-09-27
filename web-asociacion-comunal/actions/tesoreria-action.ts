"use server"

import { cookies } from "next/headers"
import { revalidatePath } from "next/cache"

export type Movimiento = {
  id: string
  tipo: string
  concepto: string
  monto: number
  fecha: string
  proyectoId?: string | null
  correoRegistro: string
  nombreRegistro: string
  cuotaId?: string | null
}

export type Cuota = {
  id: string
  correoVecino: string
  nombreVecino: string
  numeroCasa?: string | null
  periodo: string
  monto: number
  estado: string
  fechaPago?: string | null
}

export type Balance = {
  totalIngresos: number
  totalEgresos: number
  saldo: number
  cantidadMovimientos: number
}

const ADMIN_URL = "http://localhost:8081/api/admin/tesoreria"
const COMUNIDAD_URL = "http://localhost:8081/api/comunidad/tesoreria"

async function getToken() {
  const cookiesHandler = await cookies()
  return cookiesHandler.get("jwt_token")?.value || ""
}

export async function getUserCargoTesoreria(): Promise<string> {
  const cookiesHandler = await cookies()
  return cookiesHandler.get("user_cargo")?.value || ""
}

function authHeaders(token: string) {
  return {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  }
}

export async function listarMovimientosAction(): Promise<{
  data?: Movimiento[]
  error?: string
}> {
  const token = await getToken()
  if (!token) return { error: "No autenticado. Inicia sesión." }

  try {
    const response = await fetch(`${ADMIN_URL}/movimientos`, {
      headers: authHeaders(token),
      cache: "no-store",
    })
    if (response.status === 403) return { error: "Sin permiso para ver movimientos." }
    if (!response.ok) return { error: "No se pudieron cargar los movimientos." }
    return { data: await response.json() }
  } catch {
    return { error: "Error de conexion con el servidor." }
  }
}

export async function registrarMovimientoAction(formData: FormData): Promise<void> {
  const token = await getToken()
  const tipo = formData.get("tipo")?.toString()
  const concepto = formData.get("concepto")?.toString()
  const monto = Number(formData.get("monto"))
  const fecha = formData.get("fecha")?.toString()
  const proyectoId = formData.get("proyectoId")?.toString()

  if (!token || !tipo || !concepto || !monto) {
    return
  }

  try {
    const response = await fetch(`${ADMIN_URL}/movimientos`, {
      method: "POST",
      headers: authHeaders(token),
      body: JSON.stringify({
        tipo,
        concepto,
        monto,
        fecha: fecha || null,
        proyectoId: proyectoId || null,
      }),
      cache: "no-store",
    })

    if (!response.ok) {
      return
    }

    revalidatePath("/administracion/tesoreria/gastos")
    revalidatePath("/administracion/tesoreria/balance")
    revalidatePath("/comunidad/tesoreria")
  } catch {
    return
  }
}

export async function getBalanceAdminAction(): Promise<{
  data?: Balance
  error?: string
}> {
  const token = await getToken()
  if (!token) return { error: "No autenticado. Inicia sesión." }

  try {
    const response = await fetch(`${ADMIN_URL}/balance`, {
      headers: authHeaders(token),
      cache: "no-store",
    })
    if (!response.ok) return { error: "No se pudo calcular el balance." }
    return { data: await response.json() }
  } catch {
    return { error: "Error de conexion con el servidor." }
  }
}

export async function listarCuotasAdminAction(): Promise<{
  data?: Cuota[]
  error?: string
}> {
  const token = await getToken()
  if (!token) return { error: "No autenticado. Inicia sesión." }

  try {
    const response = await fetch(`${ADMIN_URL}/cuotas`, {
      headers: authHeaders(token),
      cache: "no-store",
    })
    if (response.status === 403) return { error: "Sin permiso para ver cuotas." }
    if (!response.ok) return { error: "No se pudieron cargar las cuotas." }
    return { data: await response.json() }
  } catch {
    return { error: "Error de conexion con el servidor." }
  }
}

export async function crearCuotaAction(formData: FormData): Promise<void> {
  const token = await getToken()
  const correoVecino = formData.get("correoVecino")?.toString()
  const periodo = formData.get("periodo")?.toString()
  const monto = Number(formData.get("monto"))

  if (!token || !correoVecino || !periodo || !monto) {
    return
  }

  try {
    const response = await fetch(`${ADMIN_URL}/cuotas`, {
      method: "POST",
      headers: authHeaders(token),
      body: JSON.stringify({ correoVecino, periodo, monto }),
      cache: "no-store",
    })

    if (!response.ok) {
      return
    }

    revalidatePath("/administracion/tesoreria/contribuciones")
  } catch {
    return
  }
}

export async function marcarCuotaPagadaAction(formData: FormData): Promise<void> {
  const token = await getToken()
  const id = formData.get("id")?.toString()

  if (!token || !id) {
    return
  }

  try {
    const response = await fetch(`${ADMIN_URL}/cuotas/${id}/pagar`, {
      method: "PUT",
      headers: authHeaders(token),
      cache: "no-store",
    })

    if (!response.ok) {
      return
    }

    revalidatePath("/administracion/tesoreria/contribuciones")
    revalidatePath("/administracion/tesoreria/balance")
    revalidatePath("/administracion/tesoreria/gastos")
    revalidatePath("/comunidad/tesoreria")
  } catch {
    return
  }
}

export async function getMisCuotasAction(): Promise<{
  data?: Cuota[]
  error?: string
}> {
  const token = await getToken()
  if (!token) return { error: "No autenticado. Inicia sesión." }

  try {
    const response = await fetch(`${COMUNIDAD_URL}/mis-cuotas`, {
      headers: authHeaders(token),
      cache: "no-store",
    })
    if (!response.ok) return { error: "No se pudieron cargar tus cuotas." }
    return { data: await response.json() }
  } catch {
    return { error: "Error de conexion con el servidor." }
  }
}

export async function getBalanceComunidadAction(): Promise<{
  data?: Balance
  error?: string
}> {
  const token = await getToken()
  if (!token) return { error: "No autenticado. Inicia sesión." }

  try {
    const response = await fetch(`${COMUNIDAD_URL}/balance`, {
      headers: authHeaders(token),
      cache: "no-store",
    })
    if (!response.ok) return { error: "No se pudo cargar el balance publico." }
    return { data: await response.json() }
  } catch {
    return { error: "Error de conexion con el servidor." }
  }
}
