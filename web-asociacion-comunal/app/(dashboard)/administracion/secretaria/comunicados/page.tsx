import {
    crearComunicadoAction,
    getUserCargo,
    listarComunicadosAdminAction,
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
        listarComunicadosAdminAction(),
        getUserCargo(),
    ])
    const comunicados = result.data ?? []
    const puedeEscribir = puedeEscribirSecretaria(cargo)

    return (
        <div>
            <PageHeader
                title="Comunicados oficiales"
                description="Avisos de la junta que la comunidad puede leer de inmediato."
            />

            {result.error && (
                <p className="mb-4 text-sm text-red-500">{result.error}</p>
            )}

            {puedeEscribir ? (
                <Card className="mb-6">
                    <CardHeader>
                        <CardTitle>Nuevo comunicado</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <ActionForm action={crearComunicadoAction} className="grid gap-3">
                            <Field>
                                <FieldLabel htmlFor="titulo">Titulo</FieldLabel>
                                <Input id="titulo" name="titulo" required />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="contenido">Contenido</FieldLabel>
                                <Input id="contenido" name="contenido" required />
                            </Field>
                            <Button type="submit">Publicar</Button>
                        </ActionForm>
                    </CardContent>
                </Card>
            ) : (
                <p className="mb-4 text-sm text-muted-foreground">
                    Tu cargo solo permite lectura. Escritura: Presidente o Secretario.
                </p>
            )}

            {comunicados.length === 0 ? (
                <p className="text-sm text-muted-foreground">Todavía no hay comunicados.</p>
            ) : (
                <div className="grid gap-4">
                    {comunicados.map((comunicado) => (
                        <Card key={comunicado.id}>
                            <CardHeader>
                                <CardTitle>{comunicado.titulo}</CardTitle>
                            </CardHeader>
                            <CardContent className="grid gap-1">
                                <p className="text-sm text-muted-foreground">
                                    {comunicado.nombreAutor} · {comunicado.fecha}
                                </p>
                                <p className="text-sm">{comunicado.contenido}</p>
                            </CardContent>
                        </Card>
                    ))}
                </div>
            )}
        </div>
    )
}
