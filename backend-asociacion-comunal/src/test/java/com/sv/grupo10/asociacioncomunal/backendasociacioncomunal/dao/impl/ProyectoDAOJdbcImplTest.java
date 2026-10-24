package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.ProyectoDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Proyecto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ProyectoDAOJdbcImplTest {

    @Autowired
    private ProyectoDAO proyectoDAO;

    private static final String NOMBRE_TEST = "Proyecto Test JDBC";

    @BeforeEach
    void setUp() {
        // Transactional will rollback after each test
    }

    @Test
    void guardarProyecto_persisteEnBD() {
        Proyecto proyecto = new Proyecto(
                NOMBRE_TEST,
                "Descripcion de prueba JDBC",
                "PLANIFICADO",
                "2026-10-25"
        );
        proyecto.setId(UUID.randomUUID().toString());

        Proyecto guardado = proyectoDAO.guardarProyecto(proyecto);

        assertNotNull(guardado);
        assertEquals(proyecto.getId(), guardado.getId());
        assertEquals(proyecto.getNombre(), guardado.getNombre());
        assertEquals(proyecto.getEstado(), guardado.getEstado());
    }

    @Test
    void listarProyectos_devuelveTodos() {
        List<Proyecto> proyectos = proyectoDAO.listarProyectos();

        assertNotNull(proyectos);
        assertTrue(proyectos.size() >= 6); // 6 proyectos sembrados
    }

    @Test
    void buscarPorId_devuelveProyectoCorrecto() {
        List<Proyecto> proyectos = proyectoDAO.listarProyectos();
        assertTrue(!proyectos.isEmpty());

        String primerId = proyectos.get(0).getId();
        Optional<Proyecto> encontrado = proyectoDAO.buscarPorId(primerId);

        assertTrue(encontrado.isPresent());
        assertEquals(primerId, encontrado.get().getId());
    }

    @Test
    void actualizarProyecto_cambiaEstado() {
        List<Proyecto> proyectos = proyectoDAO.listarProyectos();
        assertTrue(!proyectos.isEmpty());

        String id = proyectos.get(0).getId();
        Proyecto proyectoActualizado = new Proyecto(
                "Proyecto Actualizado",
                "Descripcion actualizada",
                "FINALIZADO",
                "2026-10-26"
        );

        Optional<Proyecto> resultado = proyectoDAO.actualizarProyecto(id, proyectoActualizado);

        assertTrue(resultado.isPresent());
        assertEquals("FINALIZADO", resultado.get().getEstado());
        assertEquals(id, resultado.get().getId());
    }

    @Test
    void eliminarProyecto_eliminaCorrectamente() {
        Proyecto proyecto = new Proyecto(
                "Proyecto Para Eliminar",
                "Descripcion temporal",
                "PLANIFICADO",
                "2026-10-25"
        );
        proyecto.setId(UUID.randomUUID().toString());
        proyectoDAO.guardarProyecto(proyecto);

        boolean eliminado = proyectoDAO.eliminarProyecto(proyecto.getId());

        assertTrue(eliminado);
        assertTrue(proyectoDAO.buscarPorId(proyecto.getId()).isEmpty());
    }
}