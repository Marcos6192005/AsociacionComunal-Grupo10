import {
    crearActaAction,
    getUserCargo,
    listarActasAdminAction,
    publicarActaAction,
} from "@/actions/secretaria-action"
import { ActionForm } from "@/components/forms/action-form"
import { PageHeader } from "@/components/dashboard/page-header"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Field, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { puedeEscribirSecretaria } from "@/lib/cargo"

export default async function Page() {
    const [result, cargo] = await Promise.all([listarActasAdminAction(), getUserCargo()])
    const actas = result.data ?? []
    const puedeEscribir = puedeEscribirSecretaria(cargo)

    return (
        <div>
            <PageHeader
                title="Actas"
                description="Registra reuniones y publica el acta cuando la junta lo apruebe."
            />

            {result.error && (
                <p className="mb-4 text-sm text-red-500">{result.error}</p>
            )}

            {puedeEscribir ? (
                <Card className="mb-6">
                    <CardHeader>
                        <CardTitle>Nueva acta (borrador)</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <ActionForm action={crearActaAction} className="grid gap-3">
                            <Field>
                                <FieldLabel htmlFor="titulo">Titulo</FieldLabel>
                                <Input id="titulo" name="titulo" required />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="fecha">Fecha</FieldLabel>
                                <Input id="fecha" name="fecha" type="date" />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="contenido">Contenido</FieldLabel>
                                <Input id="contenido" name="contenido" required />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="acuerdoTexto">Acuerdo (opcional)</FieldLabel>
                                <Input id="acuerdoTexto" name="acuerdoTexto" placeholder="Texto del acuerdo" />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="acuerdoProyectoId">Proyecto relacionado (id opcional)</FieldLabel>
                                <Input id="acuerdoProyectoId" name="acuerdoProyectoId" />
                            </Field>
                            <Button type="submit">Guardar borrador</Button>
                        </ActionForm>
                    </CardContent>
                </Card>
            ) : (
                <p className="mb-4 text-sm text-muted-foreground">
                    Tu cargo solo permite lectura de actas. Escritura: Presidente o Secretario.
                </p>
            )}

            {actas.length === 0 ? (
                <p className="text-sm text-muted-foreground">Todavía no hay actas.</p>
            ) : (
                <div className="grid gap-4">
                    {actas.map((acta) => (
                        <Card key={acta.id}>
                            <CardHeader>
                                <CardTitle>{acta.titulo}</CardTitle>
                            </CardHeader>
                            <CardContent className="grid gap-2">
                                <p className="text-sm text-muted-foreground">
                                    {acta.fecha} · {acta.estado} · {acta.nombreAutor}
                                </p>
                                <p className="text-sm">{acta.contenido}</p>
                                {acta.acuerdos?.length > 0 && (
                                    <ul className="list-disc pl-5 text-sm">
                                        {acta.acuerdos.map((acuerdo, index) => (
                                            <li key={`${acta.id}-${index}`}>
                                                {acuerdo.texto}
                                                {acuerdo.proyectoId ? ` (proyecto ${acuerdo.proyectoId})` : ""}
                                            </li>
                                        ))}
                                    </ul>
                                )}
                                {puedeEscribir && acta.estado === "BORRADOR" && (
                                    <ActionForm
                                        action={publicarActaAction}
                                        confirmMessage="Publicar esta acta? Los vecinos van a poder verla."
                                    >
                                        <input type="hidden" name="id" value={acta.id} />
                                        <Button type="submit" variant="outline" size="sm">
                                            Publicar acta
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
