package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.controllers;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.ComentarioRequestDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.ComentarioResponseDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.ComunidadProyectoDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.VotoRequestDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Comentario;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Voto;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.ComunidadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comunidad")
public class ComunidadProyectoController {

    private final ComunidadService comunidadService;

    public ComunidadProyectoController(ComunidadService comunidadService) {
        this.comunidadService = comunidadService;
    }

    @GetMapping("/proyectos")
    public ResponseEntity<List<ComunidadProyectoDTO>> listarProyectos(Authentication authentication) {
        return ResponseEntity.ok(comunidadService.listarProyectos(authentication.getName()));
    }

    @GetMapping("/proyectos/{id}")
    public ResponseEntity<ComunidadProyectoDTO> obtenerProyecto(
            @PathVariable String id,
            Authentication authentication
    ) {
        return comunidadService.obtenerProyecto(id, authentication.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/proyectos/{id}/votos")
    public ResponseEntity<?> votar(
            @PathVariable String id,
            @RequestBody VotoRequestDTO request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(comunidadService.votar(id, authentication.getName(), request.valor()));
    }

    @GetMapping("/comentarios")
    public ResponseEntity<List<ComentarioResponseDTO>> listarComentarios() {
        return ResponseEntity.ok(comunidadService.listarComentarios());
    }

    @PostMapping("/proyectos/{id}/comentarios")
    public ResponseEntity<?> comentar(
            @PathVariable String id,
            @RequestBody ComentarioRequestDTO request,
            Authentication authentication
    ) {
        Comentario comentario = comunidadService.comentar(id, authentication.getName(), request.texto());
        return ResponseEntity.status(HttpStatus.CREATED).body(comentario);
    }
}
