package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.plantillas.EmailCuotaPendiente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificacionCorreoTest {

    @Mock
    private EmailService emailService;

    @Test
    void correoVacio_noSeEncola() {
        NotificacionCorreo notificacion = new NotificacionCorreo(emailService);

        notificacion.avisar(new EmailCuotaPendiente("  ", "2026-10", 8.0, "Rosa"));

        verify(emailService, never()).enviar(org.mockito.ArgumentMatchers.any(com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.plantillas.PlantillaEmail.class));
    }

    @Test
    void falloAlEncolar_noSePropaga() {
        NotificacionCorreo notificacion = new NotificacionCorreo(emailService);
        EmailCuotaPendiente plantilla = new EmailCuotaPendiente("vecino@adesco.com", "2026-10", 8.0, "Rosa");
        doThrow(new RuntimeException("smtp caido")).when(emailService).enviar(plantilla);

        assertDoesNotThrow(() -> notificacion.avisar(plantilla));
    }
}
