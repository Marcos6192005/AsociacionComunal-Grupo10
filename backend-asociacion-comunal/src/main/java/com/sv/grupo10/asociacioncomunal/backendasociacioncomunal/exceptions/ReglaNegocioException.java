package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.exceptions;

public class ReglaNegocioException extends IllegalArgumentException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
