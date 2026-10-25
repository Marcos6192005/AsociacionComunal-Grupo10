"use client"

import { useActionState } from "react"
import { formStateInicial, type FormState } from "@/lib/form-state"

type FormAction = (prev: FormState, formData: FormData) => Promise<FormState>

export function ActionForm({
  action,
  className,
  confirmMessage,
  children,
}: {
  action: FormAction
  className?: string
  confirmMessage?: string
  children: React.ReactNode
}) {
  const [state, formAction, pending] = useActionState(action, formStateInicial)

  return (
    <form
      action={formAction}
      className={className}
      onSubmit={(event) => {
        if (confirmMessage && !window.confirm(confirmMessage)) {
          event.preventDefault()
        }
      }}
    >
      {children}
      {pending && <p className="text-sm text-muted-foreground">Enviando...</p>}
      {state.mensaje && (
        <p
          className={state.ok ? "text-sm text-emerald-600" : "text-sm text-red-500"}
          role="status"
        >
          {state.mensaje}
        </p>
      )}
    </form>
  )
}
