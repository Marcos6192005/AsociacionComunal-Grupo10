package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.UsuarioDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.MiembroDirectiva;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Usuario;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Vecino;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.plantillas.EmailBienvenida;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.NotificacionCorreo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioDAO usuarioDAO;

    @Mock
    private NotificacionCorreo notificacionCorreo;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private UsuarioServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UsuarioServiceImpl(usuarioDAO, notificacionCorreo, passwordEncoder);
    }

    @Test
    void autenticarDevuelveElUsuarioCuandoLaClaveCoincide() {
        Vecino vecino = vecinoConClave("vecino123");
        when(usuarioDAO.buscarPorCorreo(vecino.getCorreo())).thenReturn(Optional.of(vecino));

        Optional<Usuario> resultado = service.autenticar(vecino.getCorreo(), "vecino123");

        assertTrue(resultado.isPresent());
        assertEquals(vecino.getCorreo(), resultado.get().getCorreo());
    }

    @Test
    void autenticarRechazaUnaClaveDistinta() {
        Vecino vecino = vecinoConClave("vecino123");
        when(usuarioDAO.buscarPorCorreo(vecino.getCorreo())).thenReturn(Optional.of(vecino));

        Optional<Usuario> resultado = service.autenticar(vecino.getCorreo(), "otra-clave");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void autenticarRechazaUnCorreoQueNoExiste() {
        when(usuarioDAO.buscarPorCorreo("nadie@adesco.com")).thenReturn(Optional.empty());

        Optional<Usuario> resultado = service.autenticar("nadie@adesco.com", "vecino123");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void crearUsuarioRechazaUnCorreoDuplicado() {
        Vecino vecino = new Vecino("Ana", "ana@adesco.com", "vecino123", "Casa 2");
        when(usuarioDAO.buscarPorCorreo(vecino.getCorreo())).thenReturn(Optional.of(vecino));

        assertThrows(IllegalArgumentException.class, () -> service.crearUsuario(vecino));

        verify(usuarioDAO, never()).guardarUsuario(any());
        verify(notificacionCorreo, never()).avisar(any());
    }

    @Test
    void crearUsuarioGuardaLaClaveHasheadaYAvisa() {
        Vecino vecino = new Vecino("Ana", "ana@adesco.com", "vecino123", "Casa 2");
        when(usuarioDAO.buscarPorCorreo(vecino.getCorreo())).thenReturn(Optional.empty());

        service.crearUsuario(vecino);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioDAO).guardarUsuario(captor.capture());
        assertNotEquals("vecino123", captor.getValue().getPassword());
        assertTrue(passwordEncoder.matches("vecino123", captor.getValue().getPassword()));
        verify(notificacionCorreo).avisar(any(EmailBienvenida.class));
    }

    @Test
    void actualizarUsuarioInexistenteLanzaNoSuchElement() {
        Vecino vecino = vecinoConClave("vecino123");
        when(usuarioDAO.actualizarUsuario(vecino)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> service.actualizarUsuario(vecino));
    }

    @Test
    void eliminarUsuarioInexistenteLanzaNoSuchElement() {
        when(usuarioDAO.eliminarUsuario("id-que-no-esta")).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> service.eliminarUsuario("id-que-no-esta"));
    }

    @Test
    void crearUsuarioRechazaUnSegundoPresidente() {
        MiembroDirectiva presidenteActual = presidente("Marta", "marta@adesco.com");
        MiembroDirectiva nuevo = new MiembroDirectiva("Luis", "luis@adesco.com", "presi123", "Presidente");
        when(usuarioDAO.buscarPorCorreo(nuevo.getCorreo())).thenReturn(Optional.empty());
        when(usuarioDAO.listarUsuarios()).thenReturn(List.of(presidenteActual));

        assertThrows(IllegalArgumentException.class, () -> service.crearUsuario(nuevo));

        verify(usuarioDAO, never()).guardarUsuario(any());
        verify(notificacionCorreo, never()).avisar(any());
    }

    @Test
    void crearUsuarioPermiteElPrimerPresidente() {
        MiembroDirectiva nuevo = new MiembroDirectiva("Luis", "luis@adesco.com", "presi123", "Presidente");
        when(usuarioDAO.buscarPorCorreo(nuevo.getCorreo())).thenReturn(Optional.empty());
        when(usuarioDAO.listarUsuarios()).thenReturn(List.of(vecinoConClave("vecino123")));

        service.crearUsuario(nuevo);

        verify(usuarioDAO).guardarUsuario(nuevo);
    }

    @Test
    void crearUsuarioNoRevisaPresidentesSiNoEsPresidente() {
        MiembroDirectiva secretario = new MiembroDirectiva("Ana", "ana@adesco.com", "secre123", "Secretario");
        when(usuarioDAO.buscarPorCorreo(secretario.getCorreo())).thenReturn(Optional.empty());

        service.crearUsuario(secretario);

        verify(usuarioDAO, never()).listarUsuarios();
        verify(usuarioDAO).guardarUsuario(secretario);
    }

    @Test
    void actualizarUsuarioRechazaConvertirAOtroEnPresidente() {
        MiembroDirectiva presidenteActual = presidente("Marta", "marta@adesco.com");
        MiembroDirectiva aspirante = new MiembroDirectiva(
                "Ana", "ana@adesco.com", passwordEncoder.encode("secre123"), "Presidente");
        when(usuarioDAO.listarUsuarios()).thenReturn(List.of(presidenteActual, aspirante));

        assertThrows(IllegalArgumentException.class, () -> service.actualizarUsuario(aspirante));

        verify(usuarioDAO, never()).actualizarUsuario(any());
    }

    @Test
    void actualizarUsuarioPermiteEditarAlPresidenteActual() {
        MiembroDirectiva presidenteActual = presidente("Marta", "marta@adesco.com");
        when(usuarioDAO.listarUsuarios()).thenReturn(List.of(presidenteActual));
        when(usuarioDAO.actualizarUsuario(presidenteActual)).thenReturn(true);

        Usuario resultado = service.actualizarUsuario(presidenteActual);

        assertEquals(presidenteActual.getId(), resultado.getId());
    }

    private MiembroDirectiva presidente(String nombre, String correo) {
        return new MiembroDirectiva(nombre, correo, passwordEncoder.encode("presi123"), "Presidente");
    }

    private Vecino vecinoConClave(String clavePlana) {
        return new Vecino(
                "Rosa",
                "rosa@adesco.com",
                passwordEncoder.encode(clavePlana),
                "Casa 1"
        );
    }
}