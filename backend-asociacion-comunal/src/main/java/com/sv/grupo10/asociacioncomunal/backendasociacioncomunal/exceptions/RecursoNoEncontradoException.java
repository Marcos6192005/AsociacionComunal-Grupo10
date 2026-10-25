package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.exceptions;

import java.util.NoSuchElementException;

public class RecursoNoEncontradoException extends NoSuchElementException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
