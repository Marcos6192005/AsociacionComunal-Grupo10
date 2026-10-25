package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.exceptions;

public class AccesoDenegadoException extends SecurityException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
