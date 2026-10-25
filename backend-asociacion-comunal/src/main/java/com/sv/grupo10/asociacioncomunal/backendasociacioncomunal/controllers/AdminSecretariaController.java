package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.controllers;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.ActaRequestDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.ComunicadoRequestDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.ResponderSolicitudRequestDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Acta;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Comunicado;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Solicitud;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.SecretariaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/secretaria")
public class AdminSecretariaController {

    private final SecretariaService secretariaService;

    public AdminSecretariaController(SecretariaService secretariaService) {
        this.secretariaService = secretariaService;
    }

    @GetMapping("/solicitudes")
    public ResponseEntity<List<Solicitud>> listarSolicitudes() {
        return ResponseEntity.ok(secretariaService.listarSolicitudes());
    }

    @PutMapping("/solicitudes/{id}/responder")
    public ResponseEntity<Solicitud> responderSolicitud(
            @PathVariable String id,
            @RequestBody ResponderSolicitudRequestDTO request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(secretariaService.responderSolicitud(authentication.getName(), id, request));
    }

    @GetMapping("/actas")
    public ResponseEntity<List<Acta>> listarActas(Authentication authentication) {
        return ResponseEntity.ok(secretariaService.listarActasAdmin(authentication.getName()));
    }

    @PostMapping("/actas")
    public ResponseEntity<Acta> crearActa(
            @RequestBody ActaRequestDTO request,
            Authentication authentication
    ) {
        Acta creada = secretariaService.crearActa(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/actas/{id}/publicar")
    public ResponseEntity<Acta> publicarActa(
            @PathVariable String id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(secretariaService.publicarActa(authentication.getName(), id));
    }

    @GetMapping("/comunicados")
    public ResponseEntity<List<Comunicado>> listarComunicados() {
        return ResponseEntity.ok(secretariaService.listarComunicados());
    }

    @PostMapping("/comunicados")
    public ResponseEntity<Comunicado> crearComunicado(
            @RequestBody ComunicadoRequestDTO request,
            Authentication authentication
    ) {
        Comunicado creado = secretariaService.crearComunicado(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}
