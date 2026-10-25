package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.ComentarioDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Comentario;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@Primary
@DependsOn({"usuarioDAOJdbcImpl", "proyectoDAOJdbcImpl"})
public class ComentarioDAOJdbcImpl implements ComentarioDAO {

    private static final String COLUMNAS = "id, proyecto_id, correo_autor, nombre_autor, texto, fecha";
    private static final String INSERT_SQL = "INSERT INTO comentario (" + COLUMNAS + ") VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_ALL = "SELECT " + COLUMNAS + " FROM comentario ORDER BY fecha";
    private static final String SELECT_BY_PROYECTO = "SELECT " + COLUMNAS + " FROM comentario WHERE proyecto_id = ? ORDER BY fecha";

    private final DataSource dataSource;

    public ComentarioDAOJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void poblarSiVacio() {
        if (listarComentarios().isEmpty()) {
            for (Comentario c : SemillaComunidad.comentarios()) {
                guardarComentario(c);
            }
            System.out.println("Comentarios sembrados en PostgreSQL");
        }
    }

    @Override
    public Comentario guardarComentario(Comentario comentario) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, comentario.getId());
            statement.setString(2, comentario.getProyectoId());
            statement.setString(3, comentario.getCorreoAutor());
            statement.setString(4, comentario.getNombreAutor());
            statement.setString(5, comentario.getTexto());
            statement.setString(6, comentario.getFecha());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo guardar el comentario", e);
        }
        return comentario;
    }

    @Override
    public List<Comentario> listarComentarios() {
        List<Comentario> comentarios = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                comentarios.add(mapear(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo listar comentarios", e);
        }
        return comentarios;
    }

    @Override
    public List<Comentario> listarPorProyecto(String proyectoId) {
        List<Comentario> comentarios = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_PROYECTO)) {
            statement.setString(1, proyectoId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    comentarios.add(mapear(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo listar comentarios por proyecto", e);
        }
        return comentarios;
    }

    private Comentario mapear(ResultSet resultSet) throws SQLException {
        Comentario comentario = new Comentario();
        comentario.setId(resultSet.getString("id"));
        comentario.setProyectoId(resultSet.getString("proyecto_id"));
        comentario.setCorreoAutor(resultSet.getString("correo_autor"));
        comentario.setNombreAutor(resultSet.getString("nombre_autor"));
        comentario.setTexto(resultSet.getString("texto"));
        comentario.setFecha(resultSet.getString("fecha"));
        return comentario;
    }
}