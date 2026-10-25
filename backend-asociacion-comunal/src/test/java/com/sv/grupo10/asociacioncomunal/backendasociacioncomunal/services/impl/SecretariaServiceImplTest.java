package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.ActaDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.ComunicadoDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.SolicitudDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.exceptions.RecursoNoEncontradoException;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.ResponderSolicitudRequestDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.SolicitudRequestDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.MiembroDirectiva;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Solicitud;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Vecino;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.plantillas.PlantillaEmail;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.NotificacionCorreo;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecretariaServiceImplTest {

    @Mock
    private SolicitudDAO solicitudDAO;
    @Mock
    private ActaDAO actaDAO;
    @Mock
    private ComunicadoDAO comunicadoDAO;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private NotificacionCorreo notificacionCorreo;

    private SecretariaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SecretariaServiceImpl(
                solicitudDAO, actaDAO, comunicadoDAO, usuarioService, notificacionCorreo);
    }

    @Test
    void crearSolicitud_avisaAPresidenteYSecretario() {
        when(usuarioService.buscarPorCorreo("vecino@adesco.com"))
                .thenReturn(Optional.of(new Vecino("Rosa", "vecino@adesco.com", "x", "Casa 12")));
        when(usuarioService.listarUsuarios()).thenReturn(List.of(
                new MiembroDirectiva("Marta", "presidente@adesco.com", "x", "Presidente"),
                new MiembroDirectiva("Jorge", "secretario@adesco.com", "x", "Secretario"),
                new MiembroDirectiva("Silvia", "tesorero@adesco.com", "x", "Tesorero")
        ));
        when(solicitudDAO.guardar(any(Solicitud.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.crearSolicitud("vecino@adesco.com", new SolicitudRequestDTO("Farol", "No enciende"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PlantillaEmail>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificacionCorreo).avisarVarios(captor.capture());
        assertEquals(2, captor.getValue().size());
    }

    @Test
    void responderEnRevision_noEnviaCorreo() {
        when(usuarioService.buscarPorCorreo("secretario@adesco.com"))
                .thenReturn(Optional.of(new MiembroDirectiva("Jorge", "secretario@adesco.com", "x", "Secretario")));
        Solicitud solicitud = new Solicitud("Farol", "No enciende", "vecino@adesco.com", "Rosa", "RECIBIDA", "2026-10-25");
        when(solicitudDAO.buscarPorId(solicitud.getId())).thenReturn(Optional.of(solicitud));
        when(solicitudDAO.actualizar(any(), any())).thenAnswer(invocation -> Optional.of(invocation.getArgument(1)));

        service.responderSolicitud(
                "secretario@adesco.com",
                solicitud.getId(),
                new ResponderSolicitudRequestDTO("EN_REVISION", null, null));

        verify(notificacionCorreo, never()).avisar(any());
    }

    @Test
    void responderSolicitudInexistente_lanzaNoEncontrado() {
        when(usuarioService.buscarPorCorreo("secretario@adesco.com"))
                .thenReturn(Optional.of(new MiembroDirectiva("Jorge", "secretario@adesco.com", "x", "Secretario")));
        when(solicitudDAO.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.responderSolicitud(
                "secretario@adesco.com",
                "no-existe",
                new ResponderSolicitudRequestDTO("RESUELTA", "Listo", null)));
    }
}
