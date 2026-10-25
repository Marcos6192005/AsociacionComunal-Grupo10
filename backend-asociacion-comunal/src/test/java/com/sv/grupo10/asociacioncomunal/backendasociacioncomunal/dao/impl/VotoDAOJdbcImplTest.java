package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.VotoDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Voto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class VotoDAOJdbcImplTest {

    private static final String CORREO_SIN_VOTO = "diego.cruz@adesco.com";

    @Autowired
    private VotoDAO votoDAO;

    @Test
    void listarVotos_incluyeLaSemilla() {
        List<Voto> votos = votoDAO.listarVotos();

        assertTrue(votos.size() >= SemillaComunidad.votos().size());
    }

    @Test
    void listarPorProyecto_filtraLaCancha() {
        List<Voto> votos = votoDAO.listarPorProyecto(SemillaComunidad.PROY_CANCHA);

        assertTrue(votos.size() >= 15);
        assertTrue(votos.stream().allMatch(voto -> SemillaComunidad.PROY_CANCHA.equals(voto.getProyectoId())));
    }

    @Test
    void guardarVoto_insertaYLuegoActualizaElMismoPar() {
        Voto primero = new Voto(SemillaComunidad.PROY_CANCHA, CORREO_SIN_VOTO, "A_FAVOR");
        votoDAO.guardarVoto(primero);

        Voto cambio = new Voto(SemillaComunidad.PROY_CANCHA, CORREO_SIN_VOTO, "EN_CONTRA");
        votoDAO.guardarVoto(cambio);

        Optional<Voto> guardado = votoDAO.buscarPorProyectoYCorreo(SemillaComunidad.PROY_CANCHA, CORREO_SIN_VOTO);
        assertTrue(guardado.isPresent());
        assertEquals("EN_CONTRA", guardado.get().getValor());
        assertEquals(primero.getId(), guardado.get().getId());
    }
}
