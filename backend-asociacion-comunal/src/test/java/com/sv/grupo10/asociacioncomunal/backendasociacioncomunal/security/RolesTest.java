package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RolesTest {

    @Test
    void rolDesdeCargoReconocePresidenteAunqueVengaEnMinuscula() {
        assertEquals(Roles.PRESIDENTE, Roles.rolDesdeCargo("presidente"));
    }

    @Test
    void rolDesdeCargoVacioCaeEnAdministracion() {
        assertEquals(Roles.ADMINISTRACION, Roles.rolDesdeCargo(""));
        assertEquals(Roles.ADMINISTRACION, Roles.rolDesdeCargo(null));
    }

    @Test
    void rolDesdeCargoDesconocidoCaeEnAdministracion() {
        assertEquals(Roles.ADMINISTRACION, Roles.rolDesdeCargo("portero"));
    }

    @Test
    void esRolDirectivaDistingueJuntaDeVecino() {
        assertTrue(Roles.esRolDirectiva("secretario"));
        assertFalse(Roles.esRolDirectiva("vecino"));
    }
}