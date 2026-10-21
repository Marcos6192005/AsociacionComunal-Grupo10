package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.UsuarioDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.MiembroDirectiva;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Usuario;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Vecino;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioDAOJdbcImpl implements UsuarioDAO {

    private static final String COLUMNAS = "id, nombre, correo, password, tipo, numero_casa, cargo";

    private final DataSource dataSource;
    private final PasswordEncoder passwordEncoder;

    public UsuarioDAOJdbcImpl(DataSource dataSource, PasswordEncoder passwordEncoder) {
        this.dataSource = dataSource;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void guardarUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuario (" + COLUMNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, usuario);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo guardar el usuario", e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        String sql = "SELECT " + COLUMNAS + " FROM usuario WHERE lower(correo) = lower(?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, correo);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapear(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo buscar el usuario por correo", e);
        }
    }

    @Override
    public List<Usuario> listarUsuarios() {
        String sql = "SELECT " + COLUMNAS + " FROM usuario ORDER BY nombre";
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                usuarios.add(mapear(resultSet));
            }
            return usuarios;
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo listar usuarios", e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        String sql = "SELECT " + COLUMNAS + " FROM usuario WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapear(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo buscar el usuario por id", e);
        }
    }

    @Override
    public boolean actualizarUsuario(Usuario usuario) {
        String sql = """
                UPDATE usuario
                SET nombre = ?, correo = ?, password = ?, tipo = ?, numero_casa = ?, cargo = ?
                WHERE id = ?
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindSinId(statement, usuario);
            statement.setString(7, usuario.getId());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo actualizar el usuario", e);
        }
    }

    @Override
    public boolean eliminarUsuario(String id) {
        String sql = "DELETE FROM usuario WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo eliminar el usuario", e);
        }
    }

    @PostConstruct
    public void poblarSiVacio() {
        if (!listarUsuarios().isEmpty()) {
            return;
        }
        for (Usuario usuario : SemillaComunidad.usuarios()) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            guardarUsuario(usuario);
        }
        System.out.println("Usuarios sembrados en PostgreSQL");
    }

    private void bind(PreparedStatement statement, Usuario usuario) throws SQLException {
        statement.setString(1, usuario.getId());
        bindSinId(statement, usuario, 2);
    }

    private void bindSinId(PreparedStatement statement, Usuario usuario) throws SQLException {
        bindSinId(statement, usuario, 1);
    }

    private void bindSinId(PreparedStatement statement, Usuario usuario, int inicio) throws SQLException {
        statement.setString(inicio, usuario.getNombre());
        statement.setString(inicio + 1, usuario.getCorreo());
        statement.setString(inicio + 2, usuario.getPassword());
        if (usuario instanceof Vecino vecino) {
            statement.setString(inicio + 3, "VECINO");
            statement.setString(inicio + 4, vecino.getNumeroCasa());
            statement.setNull(inicio + 5, Types.VARCHAR);
        } else if (usuario instanceof MiembroDirectiva miembro) {
            statement.setString(inicio + 3, "DIRECTIVA");
            statement.setNull(inicio + 4, Types.VARCHAR);
            statement.setString(inicio + 5, miembro.getCargo());
        } else {
            throw new IllegalArgumentException("Tipo de usuario no soportado");
        }
    }

    private Usuario mapear(ResultSet resultSet) throws SQLException {
        String id = resultSet.getString("id");
        String nombre = resultSet.getString("nombre");
        String correo = resultSet.getString("correo");
        String password = resultSet.getString("password");
        String tipo = resultSet.getString("tipo");

        Usuario usuario;
        if ("VECINO".equals(tipo)) {
            usuario = new Vecino(nombre, correo, password, resultSet.getString("numero_casa"));
        } else if ("DIRECTIVA".equals(tipo)) {
            usuario = new MiembroDirectiva(nombre, correo, password, resultSet.getString("cargo"));
        } else {
            throw new IllegalArgumentException("Tipo de usuario desconocido: " + tipo);
        }
        usuario.setId(id);
        return usuario;
    }
}