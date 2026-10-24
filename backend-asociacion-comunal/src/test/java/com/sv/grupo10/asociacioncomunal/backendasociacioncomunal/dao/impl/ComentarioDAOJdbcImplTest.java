package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.ComentarioDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Comentario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ComentarioDAOJdbcImplTest {

    @Autowired
    private ComentarioDAO comentarioDAO;

    @Autowired
    private ComentarioDAOJdbcImpl comentarioDAOJdbcImpl;

    private static final String PROYECTO_TEST = "proy-alumbrado";
    private static final String CORREO_TEST = "vecino@adesco.com";

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        // Transactional will rollback after each test
    }

    @Test
    void guardarComentario_persisteEnBD() {
        Comentario comentario = new Comentario(
                PROYECTO_TEST,
                CORREO_TEST,
                "Test Usuario",
                "Comentario de prueba JDBC",
                "2026-10-25"
        );
        comentario.setId(UUID.randomUUID().toString());

        Comentario guardado = comentarioDAO.guardarComentario(comentario);

        assertNotNull(guardado);
        assertEquals(comentario.getId(), guardado.getId());
        assertEquals(comentario.getTexto(), guardado.getTexto());
        assertEquals(comentario.getProyectoId(), guardado.getProyectoId());
    }

    @Test
    void listarComentarios_devuelveTodos() {
        List<Comentario> comentarios = comentarioDAO.listarComentarios();

        assertNotNull(comentarios);
        assertTrue(comentarios.size() >= 14); // 14 seed comments
    }

    @Test
    void listarPorProyecto_filtraCorrectamente() {
        List<Comentario> comentarios = comentarioDAO.listarPorProyecto(PROYECTO_TEST);

        assertNotNull(comentarios);
        assertTrue(comentarios.size() >= 3); // Seed has 3 comments for proy-alumbrado
        for (Comentario c : comentarios) {
            assertEquals(PROYECTO_TEST, c.getProyectoId());
        }
    }

    @Test
    void poblarSiVacio_sembrarSeedAlArranque() {
        // The @PostConstruct already ran during context initialization
        List<Comentario> comentarios = comentarioDAO.listarComentarios();
        assertTrue(comentarios.size() >= 14);
    }

    @Test
    void guardarComentario_generaIdUnico() {
        Comentario c1 = new Comentario(PROYECTO_TEST, CORREO_TEST, "A", "Texto 1", "2026-10-25");
        c1.setId(UUID.randomUUID().toString());
        Comentario c2 = new Comentario(PROYECTO_TEST, CORREO_TEST, "B", "Texto 2", "2026-10-25");
        c2.setId(UUID.randomUUID().toString());

        comentarioDAO.guardarComentario(c1);
        comentarioDAO.guardarComentario(c2);

        assertNotNull(c1.getId());
        assertNotNull(c2.getId());
        assertEquals(c1.getId(), comentarioDAO.listarPorProyecto(PROYECTO_TEST).stream()
                .filter(c -> c.getTexto().equals("Texto 1"))
                .findFirst()
                .map(Comentario::getId)
                .orElse(null));
    }
}