package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.CuotaDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Cuota;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CuotaDAOJdbcImplTest {

    @Autowired
    private CuotaDAO cuotaDAO;

    @Test
    void listar_incluyeLaSemilla() {
        assertTrue(cuotaDAO.listar().size() >= SemillaComunidad.cuotas().size());
    }

    @Test
    void guardarYActualizar_marcaLaCuotaPagada() {
        Cuota cuota = new Cuota(
                "vecino@adesco.com",
                "Rosa Elena Martinez",
                "Casa 12, Pasaje Los Pinos",
                "2026-10",
                8.00,
                "PENDIENTE"
        );
        cuotaDAO.guardar(cuota);

        cuota.setEstado("PAGADA");
        cuota.setFechaPago("2026-10-12");
        Optional<Cuota> actualizada = cuotaDAO.actualizar(cuota.getId(), cuota);

        assertTrue(actualizada.isPresent());
        Optional<Cuota> leida = cuotaDAO.buscarPorId(cuota.getId());
        assertTrue(leida.isPresent());
        assertEquals("PAGADA", leida.get().getEstado());
        assertEquals("2026-10-12", leida.get().getFechaPago());
        assertEquals(8.00, leida.get().getMonto());
    }
}
