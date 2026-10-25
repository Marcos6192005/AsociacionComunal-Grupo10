package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.plantillas;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlantillaEmailTest {

    @Test
    void escapar_neutralizaScript() {
        EmailCuotaPendiente plantilla = new EmailCuotaPendiente(
                "vecino@adesco.com",
                "2026-10",
                8.0,
                "<script>alert(1)</script>"
        );

        String html = plantilla.generar().getCuerpo();

        assertFalse(html.contains("<script>"));
        assertTrue(html.contains("&lt;script&gt;"));
    }
}
