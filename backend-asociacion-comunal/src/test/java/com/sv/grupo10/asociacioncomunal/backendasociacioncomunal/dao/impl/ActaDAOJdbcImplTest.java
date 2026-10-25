package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.ActaDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Acta;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.AcuerdoActa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ActaDAOJdbcImplTest {

    @Autowired
    private ActaDAO actaDAO;

    @Test
    void listar_incluyeSemillaYAcuerdos() {
        List<Acta> actas = actaDAO.listar();

        assertTrue(actas.size() >= SemillaComunidad.actas().size());
        assertTrue(actas.stream().anyMatch(acta -> acta.getAcuerdos().size() >= 2));
    }

    @Test
    void guardarYActualizar_reemplazaLosAcuerdos() {
        List<AcuerdoActa> acuerdos = new ArrayList<>();
        acuerdos.add(new AcuerdoActa("Pintar las bancas del parque.", SemillaComunidad.PROY_PARQUE));
        Acta acta = new Acta(
                "Asamblea de prueba JDBC",
                "Borrador para la prueba.",
                "2026-10-25",
                "BORRADOR",
                SemillaComunidad.SECRETARIO_CORREO,
                SemillaComunidad.SECRETARIO_NOMBRE,
                acuerdos
        );
        actaDAO.guardar(acta);

        Optional<Acta> guardada = actaDAO.buscarPorId(acta.getId());
        assertTrue(guardada.isPresent());
        assertEquals(1, guardada.get().getAcuerdos().size());
        assertEquals(SemillaComunidad.PROY_PARQUE, guardada.get().getAcuerdos().get(0).getProyectoId());

        acta.setEstado("PUBLICADA");
        acta.setAcuerdos(List.of(
                new AcuerdoActa("Cerrar el acuerdo del parque.", SemillaComunidad.PROY_PARQUE),
                new AcuerdoActa("Sin proyecto ligado.", null)
        ));
        actaDAO.actualizar(acta.getId(), acta);

        Optional<Acta> actualizada = actaDAO.buscarPorId(acta.getId());
        assertTrue(actualizada.isPresent());
        assertEquals("PUBLICADA", actualizada.get().getEstado());
        assertEquals(2, actualizada.get().getAcuerdos().size());
        assertEquals("Sin proyecto ligado.", actualizada.get().getAcuerdos().get(1).getTexto());
    }
}
