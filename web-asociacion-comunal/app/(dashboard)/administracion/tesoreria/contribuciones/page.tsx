import { listarUsuariosAction } from "@/actions/usuario-action"
import {
    crearCuotaAction,
    generarCuotasAction,
    getUserCargoTesoreria,
    listarCuotasAdminAction,
    marcarCuotaPagadaAction,
} from "@/actions/tesoreria-action"
import { ActionForm } from "@/components/forms/action-form"
import { PageHeader } from "@/components/dashboard/page-header"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Field, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { puedeEscribirTesoreria } from "@/lib/cargo"

export default async function Page() {
    const [cuotasResult, cargo, usuarios] = await Promise.all([
        listarCuotasAdminAction(),
        getUserCargoTesoreria(),
        listarUsuariosAction().catch(() => []),
    ])
    const cuotas = cuotasResult.data ?? []
    const puedeEscribir = puedeEscribirTesoreria(cargo)
    const vecinos = usuarios.filter((usuario) => usuario.rol === "VECINO")
    const hoy = new Date()
    const periodoActual = `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, "0")}`

    return (
        <div>
            <PageHeader
                title="Contribuciones mensuales"
                description="El tesorero asigna cuotas y registra el pago cuando el vecino aporta."
            />

            {cuotasResult.error && (
                <p className="mb-4 text-sm text-red-500">{cuotasResult.error}</p>
            )}

            {puedeEscribir ? (
                <Card className="mb-6">
                    <CardHeader>
                        <CardTitle>Nueva cuota</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <ActionForm action={crearCuotaAction} className="grid gap-3">
                            <Field>
                                <FieldLabel htmlFor="correoVecino">Vecino</FieldLabel>
                                <select
                                    id="correoVecino"
                                    name="correoVecino"
                                    required
                                    className="h-8 rounded-2xl border border-border bg-background px-3 text-sm"
                                >
                                    {vecinos.map((vecino) => (
                                        <option key={vecino.id} value={vecino.correo}>
                                            {vecino.nombre} ({vecino.correo})
                                        </option>
                                    ))}
                                </select>
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="periodo">Periodo (YYYY-MM)</FieldLabel>
                                <Input id="periodo" name="periodo" placeholder="2026-03" required />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="monto">Monto</FieldLabel>
                                <Input id="monto" name="monto" type="number" step="0.01" min="0.01" required />
                            </Field>
                            <Button type="submit">Crear cuota</Button>
                        </ActionForm>
                    </CardContent>
                </Card>
            ) : null}

            {puedeEscribir ? (
                <Card className="mb-6">
                    <CardHeader>
                        <CardTitle>Cuotas del mes</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <ActionForm action={generarCuotasAction} className="grid gap-3">
                            <Field>
                                <FieldLabel htmlFor="periodoMes">Periodo</FieldLabel>
                                <Input
                                    id="periodoMes"
                                    name="periodo"
                                    defaultValue={periodoActual}
                                    placeholder="2026-10"
                                    required
                                />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="montoMes">Monto</FieldLabel>
                                <Input
                                    id="montoMes"
                                    name="monto"
                                    type="number"
                                    step="0.01"
                                    min="0.01"
                                    defaultValue="8"
                                    required
                                />
                            </Field>
                            <Button type="submit">Generar cuotas del mes</Button>
                        </ActionForm>
                    </CardContent>
                </Card>
            ) : null}

            {!puedeEscribir ? (
                <p className="mb-4 text-sm text-muted-foreground">
                    Tu cargo solo permite lectura. Escritura: Presidente o Tesorero.
                </p>
            ) : null}

            {cuotas.length === 0 ? (
                <p className="text-sm text-muted-foreground">Todavía no hay cuotas.</p>
            ) : (
                <div className="grid gap-4">
                    {cuotas.map((cuota) => (
                        <Card key={cuota.id}>
                            <CardHeader>
                                <CardTitle>
                                    {cuota.nombreVecino} · {cuota.periodo}
                                </CardTitle>
                            </CardHeader>
                            <CardContent className="grid gap-2">
                                <p className="text-sm text-muted-foreground">
                                    {cuota.numeroCasa || "Sin casa"} · ${cuota.monto.toFixed(2)} ·{" "}
                                    {cuota.estado}
                                </p>
                                {cuota.fechaPago && (
                                    <p className="text-sm">Pagada el {cuota.fechaPago}</p>
                                )}
                                {puedeEscribir && cuota.estado === "PENDIENTE" && (
                                    <ActionForm
                                        action={marcarCuotaPagadaAction}
                                        confirmMessage="Marcar esta cuota como pagada? Se registra un ingreso."
                                    >
                                        <input type="hidden" name="id" value={cuota.id} />
                                        <Button type="submit" size="sm" variant="outline">
                                            Marcar como pagada
                                        </Button>
                                    </ActionForm>
                                )}
                            </CardContent>
                        </Card>
                    ))}
                </div>
            )}
        </div>
    )
}
