package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.CuotaDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.MovimientoDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.exceptions.AccesoDenegadoException;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.exceptions.PersistenciaException;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.exceptions.ReglaNegocioException;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.CuotaRequestDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.GenerarCuotasRequestDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.GenerarCuotasResultado;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Cuota;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.MiembroDirectiva;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Movimiento;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Vecino;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.NotificacionCorreo;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TesoreriaServiceImplTest {

    @Mock
    private MovimientoDAO movimientoDAO;
    @Mock
    private CuotaDAO cuotaDAO;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private NotificacionCorreo notificacionCorreo;

    private TesoreriaServiceImpl service;

    private final MiembroDirectiva tesorero = new MiembroDirectiva(
            "Silvia", "tesorero@adesco.com", "teso123", "Tesorero");
    private final MiembroDirectiva secretario = new MiembroDirectiva(
            "Jorge", "secretario@adesco.com", "secre123", "Secretario");
    private final Vecino vecino = new Vecino(
            "Rosa", "vecino@adesco.com", "vecino123", "Casa 12");

    @BeforeEach
    void setUp() {
        service = new TesoreriaServiceImpl(movimientoDAO, cuotaDAO, usuarioService, notificacionCorreo);
    }

    @Test
    void crearCuota_guardaYAvisa() {
        when(usuarioService.buscarPorCorreo("tesorero@adesco.com")).thenReturn(Optional.of(tesorero));
        when(usuarioService.buscarPorCorreo("vecino@adesco.com")).thenReturn(Optional.of(vecino));
        when(cuotaDAO.listar()).thenReturn(List.of());
        when(cuotaDAO.guardar(any(Cuota.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cuota creada = service.crearCuota(
                "tesorero@adesco.com",
                new CuotaRequestDTO("vecino@adesco.com", "2026-10", 8.0));

        assertEquals("PENDIENTE", creada.getEstado());
        assertEquals("2026-10", creada.getPeriodo());
        verify(notificacionCorreo).avisar(any());
    }

    @Test
    void crearCuota_rechazaDuplicada() {
        when(usuarioService.buscarPorCorreo("tesorero@adesco.com")).thenReturn(Optional.of(tesorero));
        when(usuarioService.buscarPorCorreo("vecino@adesco.com")).thenReturn(Optional.of(vecino));
        Cuota existente = new Cuota("vecino@adesco.com", "Rosa", "Casa 12", "2026-10", 8.0, "PENDIENTE");
        when(cuotaDAO.listar()).thenReturn(List.of(existente));

        assertThrows(ReglaNegocioException.class, () -> service.crearCuota(
                "tesorero@adesco.com",
                new CuotaRequestDTO("vecino@adesco.com", "2026-10", 8.0)));
        verify(cuotaDAO, never()).guardar(any());
    }

    @Test
    void crearCuota_rechazaSiNoEsVecino() {
        when(usuarioService.buscarPorCorreo("tesorero@adesco.com")).thenReturn(Optional.of(tesorero));
        when(usuarioService.buscarPorCorreo("secretario@adesco.com")).thenReturn(Optional.of(secretario));

        assertThrows(ReglaNegocioException.class, () -> service.crearCuota(
                "tesorero@adesco.com",
                new CuotaRequestDTO("secretario@adesco.com", "2026-10", 8.0)));
    }

    @Test
    void marcarPagada_creaElIngreso() {
        when(usuarioService.buscarPorCorreo("tesorero@adesco.com")).thenReturn(Optional.of(tesorero));
        Cuota pendiente = new Cuota("vecino@adesco.com", "Rosa", "Casa 12", "2026-10", 8.0, "PENDIENTE");
        pendiente.setId("cuota-1");
        when(cuotaDAO.buscarPorId("cuota-1")).thenReturn(Optional.of(pendiente));
        when(cuotaDAO.actualizar(any(), any())).thenAnswer(invocation -> Optional.of(invocation.getArgument(1)));

        Cuota pagada = service.marcarCuotaPagada("tesorero@adesco.com", "cuota-1");

        assertEquals("PAGADA", pagada.getEstado());
        ArgumentCaptor<Movimiento> captor = ArgumentCaptor.forClass(Movimiento.class);
        verify(movimientoDAO).guardar(captor.capture());
        assertEquals("INGRESO", captor.getValue().getTipo());
        assertEquals("cuota-1", captor.getValue().getCuotaId());
    }

    @Test
    void secretarioNoPuedeCrearCuota() {
        when(usuarioService.buscarPorCorreo("secretario@adesco.com")).thenReturn(Optional.of(secretario));

        assertThrows(AccesoDenegadoException.class, () -> service.crearCuota(
                "secretario@adesco.com",
                new CuotaRequestDTO("vecino@adesco.com", "2026-10", 8.0)));
        verify(cuotaDAO, never()).guardar(any());
    }

    @Test
    void generarCuotas_cuentaCreadasYDuplicadas() {
        when(usuarioService.buscarPorCorreo("tesorero@adesco.com")).thenReturn(Optional.of(tesorero));
        Vecino otro = new Vecino("Carlos", "carlos.hernandez@adesco.com", "vecino123", "Casa 8");
        Vecino tercero = new Vecino("Ana", "ana.guzman@adesco.com", "vecino123", "Casa 15");
        when(usuarioService.listarUsuarios()).thenReturn(List.of(vecino, otro, tercero, tesorero));
        when(cuotaDAO.guardar(any(Cuota.class))).thenAnswer(invocation -> {
            Cuota cuota = invocation.getArgument(0);
            if ("ana.guzman@adesco.com".equals(cuota.getCorreoVecino())) {
                throw new PersistenciaException("duplicada", new SQLException("duplicate", "23505"));
            }
            return cuota;
        });

        GenerarCuotasResultado resultado = service.generarCuotasDelMes(
                "tesorero@adesco.com",
                new GenerarCuotasRequestDTO("2026-11", 8.0));

        assertEquals(2, resultado.creadas());
        assertEquals(1, resultado.existentes());
        assertEquals(0, resultado.fallidas());
        verify(cuotaDAO, times(3)).guardar(any());
        verify(notificacionCorreo, times(2)).avisar(any());
    }
}
