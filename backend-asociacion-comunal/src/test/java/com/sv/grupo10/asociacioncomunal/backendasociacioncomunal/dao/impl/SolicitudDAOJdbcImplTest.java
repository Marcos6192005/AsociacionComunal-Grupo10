package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.SolicitudDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Solicitud;
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
class SolicitudDAOJdbcImplTest {

    @Autowired
    private SolicitudDAO solicitudDAO;

    @Test
    void listar_incluyeLaSemilla() {
        assertTrue(solicitudDAO.listar().size() >= SemillaComunidad.solicitudes().size());
    }

    @Test
    void guardarYActualizar_cambiaElEstado() {
        Solicitud solicitud = new Solicitud(
                "Farol roto en la casa 19",
                "El farol de la esquina parpadea.",
                "miguel.ayala@adesco.com",
                "Miguel Angel Ayala",
                "RECIBIDA",
                "2026-10-25"
        );
        solicitud.setProyectoId(SemillaComunidad.PROY_ALUMBRADO);
        solicitudDAO.guardar(solicitud);

        solicitud.setEstado("EN_REVISION");
        solicitud.setRespuesta("La cuadrilla pasa el lunes.");
        Optional<Solicitud> actualizada = solicitudDAO.actualizar(solicitud.getId(), solicitud);

        assertTrue(actualizada.isPresent());
        Optional<Solicitud> leida = solicitudDAO.buscarPorId(solicitud.getId());
        assertTrue(leida.isPresent());
        assertEquals("EN_REVISION", leida.get().getEstado());
        assertEquals("La cuadrilla pasa el lunes.", leida.get().getRespuesta());
        assertEquals(SemillaComunidad.PROY_ALUMBRADO, leida.get().getProyectoId());
    }
}
