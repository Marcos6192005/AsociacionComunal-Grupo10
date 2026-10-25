package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.EmailLogDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.EstadoEnvio;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.RegistroEmail;
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
class EmailLogDAOJdbcImplTest {

    @Autowired
    private EmailLogDAO emailLogDAO;

    @Test
    void listarTodos_incluyeLaSemilla() {
        assertTrue(emailLogDAO.listarTodos().size() >= SemillaComunidad.correos().size());
    }

    @Test
    void guardarYActualizar_marcaElCorreoEnviado() {
        RegistroEmail registro = new RegistroEmail("vecino@adesco.com", "Aviso de prueba JDBC");
        emailLogDAO.guardar(registro);

        registro.registrarIntento("test-hilo");
        registro.marcarEnviado();
        emailLogDAO.actualizar(registro);

        Optional<RegistroEmail> leido = emailLogDAO.buscarPorId(registro.getId());
        assertTrue(leido.isPresent());
        assertEquals(EstadoEnvio.ENVIADO, leido.get().getEstado());
        assertEquals(1, leido.get().getIntentos());
        assertEquals("test-hilo", leido.get().getHiloProcesador());
        assertEquals("Aviso de prueba JDBC", leido.get().getAsunto());
    }
}
