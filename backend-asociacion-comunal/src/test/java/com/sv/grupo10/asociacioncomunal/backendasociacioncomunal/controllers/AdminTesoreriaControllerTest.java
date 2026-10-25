package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.controllers;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.exceptions.AccesoDenegadoException;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.exceptions.ApiExceptionHandler;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.exceptions.ReglaNegocioException;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.security.JwtProvider;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.TesoreriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminTesoreriaController.class)
@AutoConfigureMockMvc
@Import(ApiExceptionHandler.class)
@WithMockUser(username = "tesorero@adesco.com", roles = "TESORERO")
class AdminTesoreriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TesoreriaService tesoreriaService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    void listarCuotas_devuelve200() throws Exception {
        when(tesoreriaService.listarCuotas("tesorero@adesco.com")).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/tesoreria/cuotas"))
                .andExpect(status().isOk());
    }

    @Test
    void crearCuota_devuelve400CuandoLaReglaFalla() throws Exception {
        when(tesoreriaService.crearCuota(eq("tesorero@adesco.com"), any()))
                .thenThrow(new ReglaNegocioException("Ya existe una cuota para ese vecino en el periodo"));

        mockMvc.perform(post("/api/admin/tesoreria/cuotas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correoVecino\":\"vecino@adesco.com\",\"periodo\":\"2026-10\",\"monto\":8}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Ya existe una cuota para ese vecino en el periodo"));
    }

    @Test
    void listarCuotas_devuelve403SinCargo() throws Exception {
        when(tesoreriaService.listarCuotas("tesorero@adesco.com"))
                .thenThrow(new AccesoDenegadoException("Se requiere un miembro de la directiva"));

        mockMvc.perform(get("/api/admin/tesoreria/cuotas"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Se requiere un miembro de la directiva"));
    }
}
