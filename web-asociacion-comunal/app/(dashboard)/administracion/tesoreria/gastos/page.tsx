import {
    getUserCargoTesoreria,
    listarMovimientosAction,
    registrarMovimientoAction,
} from "@/actions/tesoreria-action"
import { ActionForm } from "@/components/forms/action-form"
import { PageHeader } from "@/components/dashboard/page-header"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Field, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { puedeEscribirTesoreria } from "@/lib/cargo"

export default async function Page() {
    const [result, cargo] = await Promise.all([
        listarMovimientosAction(),
        getUserCargoTesoreria(),
    ])
    const movimientos = result.data ?? []
    const puedeEscribir = puedeEscribirTesoreria(cargo)

    return (
        <div>
            <PageHeader
                title="Gastos y movimientos"
                description="Registra ingresos y egresos del libro de caja comunal."
            />

            {result.error && (
                <p className="mb-4 text-sm text-red-500">{result.error}</p>
            )}

            {puedeEscribir ? (
                <Card className="mb-6">
                    <CardHeader>
                        <CardTitle>Nuevo movimiento</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <ActionForm action={registrarMovimientoAction} className="grid gap-3">
                            <Field>
                                <FieldLabel htmlFor="tipo">Tipo</FieldLabel>
                                <select
                                    id="tipo"
                                    name="tipo"
                                    required
                                    className="h-8 rounded-2xl border border-border bg-background px-3 text-sm"
                                    defaultValue="EGRESO"
                                >
                                    <option value="INGRESO">Ingreso</option>
                                    <option value="EGRESO">Egreso</option>
                                </select>
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="concepto">Concepto</FieldLabel>
                                <Input id="concepto" name="concepto" required />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="monto">Monto</FieldLabel>
                                <Input id="monto" name="monto" type="number" step="0.01" min="0.01" required />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="fecha">Fecha</FieldLabel>
                                <Input id="fecha" name="fecha" type="date" />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="proyectoId">Proyecto (id opcional)</FieldLabel>
                                <Input id="proyectoId" name="proyectoId" />
                            </Field>
                            <Button type="submit">Registrar</Button>
                        </ActionForm>
                    </CardContent>
                </Card>
            ) : (
                <p className="mb-4 text-sm text-muted-foreground">
                    Tu cargo solo permite lectura. Escritura: Presidente o Tesorero.
                </p>
            )}

            {movimientos.length === 0 ? (
                <p className="text-sm text-muted-foreground">Todavía no hay movimientos.</p>
            ) : (
                <div className="grid gap-3">
                    {movimientos
                        .slice()
                        .reverse()
                        .map((movimiento) => (
                            <Card key={movimiento.id} size="sm">
                                <CardContent className="grid gap-1 pt-4">
                                    <p className="text-sm font-medium">
                                        {movimiento.tipo} · ${movimiento.monto.toFixed(2)}
                                    </p>
                                    <p className="text-sm">{movimiento.concepto}</p>
                                    <p className="text-xs text-muted-foreground">
                                        {movimiento.fecha} · {movimiento.nombreRegistro}
                                    </p>
                                </CardContent>
                            </Card>
                        ))}
                </div>
            )}
        </div>
    )
}
