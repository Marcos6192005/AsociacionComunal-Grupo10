package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos;

public record GenerarCuotasResultado(
        int creadas,
        int existentes,
        int fallidas
) {}
