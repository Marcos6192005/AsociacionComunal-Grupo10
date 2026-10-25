import {
    crearSolicitudVecinoAction,
    listarMisSolicitudesAction,
} from "@/actions/secretaria-action"
import { ActionForm } from "@/components/forms/action-form"
import { PageHeader } from "@/components/dashboard/page-header"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Field, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"

export default async function Page() {
    const result = await listarMisSolicitudesAction()
    const solicitudes = result.data ?? []

    return (
        <div>
            <PageHeader
                title="Mis solicitudes"
                description="Envia pedidos a la secretaría y sigue el estado de cada uno."
            />

            {result.error && (
                <p className="mb-4 text-sm text-red-500">{result.error}</p>
            )}

            <Card className="mb-6">
                <CardHeader>
                    <CardTitle>Nueva solicitud</CardTitle>
                </CardHeader>
                <CardContent>
                    <ActionForm action={crearSolicitudVecinoAction} className="grid gap-3">
                        <Field>
                            <FieldLabel htmlFor="titulo">Titulo</FieldLabel>
                            <Input id="titulo" name="titulo" required />
                        </Field>
                        <Field>
                            <FieldLabel htmlFor="descripcion">Descripcion</FieldLabel>
                            <Input id="descripcion" name="descripcion" required />
                        </Field>
                        <Button type="submit">Enviar solicitud</Button>
                    </ActionForm>
                </CardContent>
            </Card>

            {solicitudes.length === 0 ? (
                <p className="text-sm text-muted-foreground">Todavía no enviaste solicitudes.</p>
            ) : (
                <div className="grid gap-4">
                    {solicitudes.map((solicitud) => (
                        <Card key={solicitud.id}>
                            <CardHeader>
                                <CardTitle>{solicitud.titulo}</CardTitle>
                            </CardHeader>
                            <CardContent className="grid gap-1">
                                <p className="text-sm text-muted-foreground">
                                    {solicitud.fecha} · {solicitud.estado}
                                </p>
                                <p className="text-sm">{solicitud.descripcion}</p>
                                {solicitud.respuesta && (
                                    <p className="text-sm">
                                        <span className="font-medium">Respuesta: </span>
                                        {solicitud.respuesta}
                                    </p>
                                )}
                            </CardContent>
                        </Card>
                    ))}
                </div>
            )}
        </div>
    )
}
