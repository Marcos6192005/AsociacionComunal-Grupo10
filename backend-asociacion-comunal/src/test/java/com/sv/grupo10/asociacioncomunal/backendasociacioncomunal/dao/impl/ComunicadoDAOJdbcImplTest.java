package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.ComunicadoDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Comunicado;
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
class ComunicadoDAOJdbcImplTest {

    @Autowired
    private ComunicadoDAO comunicadoDAO;

    @Test
    void listar_incluyeLaSemilla() {
        assertTrue(comunicadoDAO.listar().size() >= SemillaComunidad.comunicados().size());
    }

    @Test
    void guardar_persisteYSePuedeBuscar() {
        Comunicado comunicado = new Comunicado(
                "Aviso de prueba JDBC",
                "El agua vuelve a las 2 pm.",
                "2026-10-25",
                SemillaComunidad.SECRETARIO_CORREO,
                SemillaComunidad.SECRETARIO_NOMBRE
        );
        comunicadoDAO.guardar(comunicado);

        Optional<Comunicado> leido = comunicadoDAO.buscarPorId(comunicado.getId());
        assertTrue(leido.isPresent());
        assertEquals("Aviso de prueba JDBC", leido.get().getTitulo());
        assertEquals(SemillaComunidad.SECRETARIO_CORREO, leido.get().getCorreoAutor());
    }
}
