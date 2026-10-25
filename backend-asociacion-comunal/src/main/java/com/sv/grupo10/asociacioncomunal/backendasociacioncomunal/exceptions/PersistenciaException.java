package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.exceptions;

public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
