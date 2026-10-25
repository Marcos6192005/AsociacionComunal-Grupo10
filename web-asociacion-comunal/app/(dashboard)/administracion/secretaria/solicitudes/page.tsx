import {
    getUserCargo,
    listarSolicitudesAdminAction,
    responderSolicitudAction,
} from "@/actions/secretaria-action"
import { ActionForm } from "@/components/forms/action-form"
import { PageHeader } from "@/components/dashboard/page-header"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Field, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { puedeEscribirSecretaria } from "@/lib/cargo"

export default async function Page() {
    const [result, cargo] = await Promise.all([
        listarSolicitudesAdminAction(),
        getUserCargo(),
    ])
    const solicitudes = result.data ?? []
    const puedeEscribir = puedeEscribirSecretaria(cargo)

    return (
        <div>
            <PageHeader
                title="Solicitudes vecinales"
                description="Revisa pedidos de la comunidad y deja una respuesta oficial."
            />

            {result.error && (
                <p className="mb-4 text-sm text-red-500">{result.error}</p>
            )}

            {solicitudes.length === 0 ? (
                <p className="text-sm text-muted-foreground">No hay solicitudes todavía.</p>
            ) : (
                <div className="grid gap-4">
                    {solicitudes.map((solicitud) => (
                        <Card key={solicitud.id}>
                            <CardHeader>
                                <CardTitle>{solicitud.titulo}</CardTitle>
                            </CardHeader>
                            <CardContent className="grid gap-3">
                                <p className="text-sm text-muted-foreground">
                                    {solicitud.nombreAutor} · {solicitud.fecha} · {solicitud.estado}
                                </p>
                                <p className="text-sm">{solicitud.descripcion}</p>
                                {solicitud.respuesta && (
                                    <p className="text-sm">
                                        <span className="font-medium">Respuesta: </span>
                                        {solicitud.respuesta}
                                    </p>
                                )}
                                {solicitud.proyectoId && (
                                    <p className="text-sm text-muted-foreground">
                                        Proyecto vinculado: {solicitud.proyectoId}
                                    </p>
                                )}
                                {puedeEscribir && (
                                    <ActionForm action={responderSolicitudAction} className="grid gap-2 border-t pt-3">
                                        <input type="hidden" name="id" value={solicitud.id} />
                                        <Field>
                                            <FieldLabel htmlFor={`estado-${solicitud.id}`}>Estado</FieldLabel>
                                            <select
                                                id={`estado-${solicitud.id}`}
                                                name="estado"
                                                required
                                                className="h-8 rounded-2xl border border-border bg-background px-3 text-sm"
                                                defaultValue="EN_REVISION"
                                            >
                                                <option value="EN_REVISION">En revisión</option>
                                                <option value="RESUELTA">Resuelta</option>
                                                <option value="RECHAZADA">Rechazada</option>
                                            </select>
                                        </Field>
                                        <Field>
                                            <FieldLabel htmlFor={`respuesta-${solicitud.id}`}>Respuesta</FieldLabel>
                                            <Input
                                                id={`respuesta-${solicitud.id}`}
                                                name="respuesta"
                                                placeholder="Mensaje para el vecino"
                                            />
                                        </Field>
                                        <Field>
                                            <FieldLabel htmlFor={`proyecto-${solicitud.id}`}>
                                                Proyecto relacionado (opcional)
                                            </FieldLabel>
                                            <Input
                                                id={`proyecto-${solicitud.id}`}
                                                name="proyectoId"
                                                placeholder="Id del proyecto"
                                            />
                                        </Field>
                                        <Button type="submit" size="sm">
                                            Guardar respuesta
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
