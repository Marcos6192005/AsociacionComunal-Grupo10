package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.controllers;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.BalanceDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.CuotaRequestDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.MovimientoRequestDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Cuota;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Movimiento;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.TesoreriaService;
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
@RequestMapping("/api/admin/tesoreria")
public class AdminTesoreriaController {

    private final TesoreriaService tesoreriaService;

    public AdminTesoreriaController(TesoreriaService tesoreriaService) {
        this.tesoreriaService = tesoreriaService;
    }

    @GetMapping("/movimientos")
    public ResponseEntity<List<Movimiento>> listarMovimientos(Authentication authentication) {
        return ResponseEntity.ok(tesoreriaService.listarMovimientos(authentication.getName()));
    }

    @PostMapping("/movimientos")
    public ResponseEntity<Movimiento> registrarMovimiento(
            @RequestBody MovimientoRequestDTO request,
            Authentication authentication
    ) {
        Movimiento creado = tesoreriaService.registrarMovimiento(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceDTO> balance() {
        return ResponseEntity.ok(tesoreriaService.calcularBalance());
    }

    @GetMapping("/cuotas")
    public ResponseEntity<List<Cuota>> listarCuotas(Authentication authentication) {
        return ResponseEntity.ok(tesoreriaService.listarCuotas(authentication.getName()));
    }

    @PostMapping("/cuotas")
    public ResponseEntity<Cuota> crearCuota(
            @RequestBody CuotaRequestDTO request,
            Authentication authentication
    ) {
        Cuota creada = tesoreriaService.crearCuota(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/cuotas/{id}/pagar")
    public ResponseEntity<Cuota> marcarPagada(
            @PathVariable String id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(tesoreriaService.marcarCuotaPagada(authentication.getName(), id));
    }
}
