package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.controllers;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.SolicitudRequestDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Acta;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Comunicado;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Solicitud;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.SecretariaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comunidad/secretaria")
public class ComunidadSecretariaController {

    private final SecretariaService secretariaService;

    public ComunidadSecretariaController(SecretariaService secretariaService) {
        this.secretariaService = secretariaService;
    }

    @GetMapping("/solicitudes")
    public ResponseEntity<List<Solicitud>> listarMisSolicitudes(Authentication authentication) {
        return ResponseEntity.ok(secretariaService.listarSolicitudesPorAutor(authentication.getName()));
    }

    @PostMapping("/solicitudes")
    public ResponseEntity<?> crearSolicitud(
            @RequestBody SolicitudRequestDTO request,
            Authentication authentication
    ) {
        Solicitud creada = secretariaService.crearSolicitud(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping("/actas")
    public ResponseEntity<List<Acta>> listarActasPublicadas() {
        return ResponseEntity.ok(secretariaService.listarActasPublicadas());
    }

    @GetMapping("/comunicados")
    public ResponseEntity<List<Comunicado>> listarComunicados() {
        return ResponseEntity.ok(secretariaService.listarComunicados());
    }
}
