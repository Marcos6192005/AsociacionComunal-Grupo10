package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.UsuarioDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Usuario;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.plantillas.EmailBienvenida;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.NotificacionCorreo;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioDAO usuarioDAO;
    private final NotificacionCorreo notificacionCorreo;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioDAO usuarioDAO, NotificacionCorreo notificacionCorreo, PasswordEncoder passwordEncoder) {
        this.usuarioDAO = usuarioDAO;
        this.notificacionCorreo = notificacionCorreo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<Usuario> autenticar(String correo, String password) {
        Optional<Usuario> usuarioOpt = usuarioDAO.buscarPorCorreo(correo);

        if (usuarioOpt.isPresent() && passwordEncoder.matches(password, usuarioOpt.get().getPassword())) {
            return usuarioOpt;
        }

        return Optional.empty();
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioDAO.buscarPorCorreo(correo);
    }

    @Override
    public Usuario crearUsuario(Usuario usuario) {
        if (usuarioDAO.buscarPorCorreo(usuario.getCorreo()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo");
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuarioDAO.guardarUsuario(usuario);
        notificacionCorreo.avisar(new EmailBienvenida(usuario.getCorreo(), usuario.getNombre()));
        return usuario;
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioDAO.listarUsuarios();
    }

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        return usuarioDAO.buscarPorId(id);
    }

    @Override
    public Usuario actualizarUsuario(Usuario usuario) {
        
        if (usuario.getPassword() == null || !usuario.getPassword().startsWith("$2")) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }

        boolean actualizado = usuarioDAO.actualizarUsuario(usuario);

        if (!actualizado) {
            throw new NoSuchElementException("No existe un usuario con id " + usuario.getId());
        }

        return usuario;
    }

    @Override
    public void eliminarUsuario(String id) {
        boolean eliminado = usuarioDAO.eliminarUsuario(id);

        if (!eliminado) {
            throw new NoSuchElementException("No existe un usuario con id " + id);
        }
    }
}