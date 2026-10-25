package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.MovimientoDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Movimiento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MovimientoDAOJdbcImplTest {

    @Autowired
    private MovimientoDAO movimientoDAO;

    @Test
    void listar_incluyeLaSemilla() {
        assertTrue(movimientoDAO.listar().size() >= SemillaComunidad.movimientos().size());
    }

    @Test
    void guardar_persisteUnEgresoSinCuota() {
        Movimiento movimiento = new Movimiento(
                "EGRESO",
                "Foco de prueba JDBC",
                12.50,
                "2026-10-25",
                SemillaComunidad.PROY_ALUMBRADO,
                SemillaComunidad.TESORERO_CORREO,
                SemillaComunidad.TESORERO_NOMBRE
        );
        movimientoDAO.guardar(movimiento);

        Movimiento leido = movimientoDAO.listar().stream()
                .filter(item -> movimiento.getId().equals(item.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals("EGRESO", leido.getTipo());
        assertEquals(12.50, leido.getMonto());
        assertEquals(SemillaComunidad.PROY_ALUMBRADO, leido.getProyectoId());
    }
}
