package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.UsuarioDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.MiembroDirectiva;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Usuario;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Vecino;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.utils.ArchivoDatUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioDAODatImpl implements UsuarioDAO {

    private static final String USUARIOS = "usuarios.dat";

    @Override
    public void guardarUsuario(Usuario usuario) {
        List<Usuario> usuarios = ArchivoDatUtil.leerDatos(USUARIOS);

        usuarios.add(usuario);

        ArchivoDatUtil.guardarDatos(USUARIOS, usuarios);
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        List<Usuario> usuarios = ArchivoDatUtil.leerDatos(USUARIOS);

        return usuarios.stream()
                .filter(u -> u.getCorreo().equalsIgnoreCase(correo))
                .findFirst();
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return ArchivoDatUtil.leerDatos(USUARIOS);
    }

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        List<Usuario> usuarios = ArchivoDatUtil.leerDatos(USUARIOS);

        return usuarios.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    @Override
    public boolean actualizarUsuario(Usuario usuario) {
        List<Usuario> usuarios = ArchivoDatUtil.leerDatos(USUARIOS);

        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getId().equals(usuario.getId())) {
                usuarios.set(i, usuario);
                ArchivoDatUtil.guardarDatos(USUARIOS, usuarios);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean eliminarUsuario(String id) {
        List<Usuario> usuarios = ArchivoDatUtil.leerDatos(USUARIOS);

        boolean eliminado = usuarios.removeIf(u -> u.getId().equals(id));

        if (eliminado) {
            ArchivoDatUtil.guardarDatos(USUARIOS, usuarios);
        }

        return eliminado;
    }

    @PostConstruct
    public void poblarDatosIniciales() {
        List<Usuario> usuarios = ArchivoDatUtil.leerDatos(USUARIOS);

        if (usuarios.isEmpty()) {
            System.out.println("usuarios.dat vacio. Sembrando Residencial Los Maquilishuats, San Salvador...");
            sembrarUsuariosBase();
            return;
        }

        asegurarUsuariosBase(usuarios);
    }

    private void sembrarUsuariosBase() {
        ArchivoDatUtil.guardarDatos(USUARIOS, SemillaComunidad.usuarios());
        System.out.println("Comunidad sembrada: junta y 18 vecinos de Residencial Los Maquilishuats.");
        System.out.println("Accesos: admin@adesco.com / admin123, presidente@adesco.com / presi123, vecino@adesco.com / vecino123");
    }

    private void asegurarUsuariosBase(List<Usuario> usuarios) {
        asegurarSiFalta(usuarios, "presidente@adesco.com", () -> new MiembroDirectiva(
                "Presidente ADESCO",
                "presidente@adesco.com",
                "presi123",
                "Presidente"
        ));
        asegurarSiFalta(usuarios, "secretario@adesco.com", () -> new MiembroDirectiva(
                "Secretario ADESCO",
                "secretario@adesco.com",
                "secre123",
                "Secretario"
        ));
        asegurarSiFalta(usuarios, "tesorero@adesco.com", () -> new MiembroDirectiva(
                "Tesorero ADESCO",
                "tesorero@adesco.com",
                "teso123",
                "Tesorero"
        ));
        asegurarSiFalta(usuarios, "admin@adesco.com", () -> new MiembroDirectiva(
                "Admin ADESCO",
                "admin@adesco.com",
                "admin123",
                "Presidente"
        ));
        asegurarSiFalta(usuarios, "vecino@adesco.com", () -> new Vecino(
                "Vecino 1",
                "vecino@adesco.com",
                "vecino123",
                "Casa 42, Senda B"
        ));
    }

    private void asegurarSiFalta(List<Usuario> usuarios, String correo, java.util.function.Supplier<Usuario> factory) {
        boolean existe = usuarios.stream()
                .anyMatch(usuario -> correo.equalsIgnoreCase(usuario.getCorreo()));

        if (!existe) {
            Usuario creado = factory.get();
            guardarUsuario(creado);
            usuarios.add(creado);
            System.out.println("Usuario base creado: " + correo + " (" + creado.getRol() + ")");
        }
    }
}
