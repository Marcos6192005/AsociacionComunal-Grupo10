package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.SolicitudDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Solicitud;
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
import java.util.Optional;

@Repository
@Primary
@DependsOn({"usuarioDAOJdbcImpl", "proyectoDAOJdbcImpl"})
public class SolicitudDAOJdbcImpl implements SolicitudDAO {

    private static final String COLUMNAS =
            "id, titulo, descripcion, correo_autor, nombre_autor, estado, respuesta, proyecto_id, fecha";
    private static final String INSERT_SQL =
            "INSERT INTO solicitud (" + COLUMNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL =
            "UPDATE solicitud SET titulo = ?, descripcion = ?, correo_autor = ?, nombre_autor = ?, estado = ?, respuesta = ?, proyecto_id = ?, fecha = ? WHERE id = ?";
    private static final String SELECT_ALL = "SELECT " + COLUMNAS + " FROM solicitud ORDER BY fecha";
    private static final String SELECT_BY_ID = "SELECT " + COLUMNAS + " FROM solicitud WHERE id = ?";

    private final DataSource dataSource;

    public SolicitudDAOJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void poblarSiVacio() {
        if (listar().isEmpty()) {
            for (Solicitud solicitud : SemillaComunidad.solicitudes()) {
                guardar(solicitud);
            }
            System.out.println("Solicitudes sembradas en PostgreSQL");
        }
    }

    @Override
    public Solicitud guardar(Solicitud solicitud) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            llenar(statement, solicitud);
            statement.setString(9, solicitud.getFecha());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo guardar la solicitud", e);
        }
        return solicitud;
    }

    @Override
    public List<Solicitud> listar() {
        List<Solicitud> solicitudes = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                solicitudes.add(mapear(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo listar solicitudes", e);
        }
        return solicitudes;
    }

    @Override
    public Optional<Solicitud> buscarPorId(String id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapear(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo buscar la solicitud", e);
        }
    }

    @Override
    public Optional<Solicitud> actualizar(String id, Solicitud solicitud) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, solicitud.getTitulo());
            statement.setString(2, solicitud.getDescripcion());
            statement.setString(3, solicitud.getCorreoAutor());
            statement.setString(4, solicitud.getNombreAutor());
            statement.setString(5, solicitud.getEstado());
            statement.setString(6, solicitud.getRespuesta());
            statement.setString(7, solicitud.getProyectoId());
            statement.setString(8, solicitud.getFecha());
            statement.setString(9, id);
            if (statement.executeUpdate() != 1) {
                return Optional.empty();
            }
            solicitud.setId(id);
            return Optional.of(solicitud);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo actualizar la solicitud", e);
        }
    }

    private void llenar(PreparedStatement statement, Solicitud solicitud) throws SQLException {
        statement.setString(1, solicitud.getId());
        statement.setString(2, solicitud.getTitulo());
        statement.setString(3, solicitud.getDescripcion());
        statement.setString(4, solicitud.getCorreoAutor());
        statement.setString(5, solicitud.getNombreAutor());
        statement.setString(6, solicitud.getEstado());
        statement.setString(7, solicitud.getRespuesta());
        statement.setString(8, solicitud.getProyectoId());
    }

    private Solicitud mapear(ResultSet resultSet) throws SQLException {
        Solicitud solicitud = new Solicitud();
        solicitud.setId(resultSet.getString("id"));
        solicitud.setTitulo(resultSet.getString("titulo"));
        solicitud.setDescripcion(resultSet.getString("descripcion"));
        solicitud.setCorreoAutor(resultSet.getString("correo_autor"));
        solicitud.setNombreAutor(resultSet.getString("nombre_autor"));
        solicitud.setEstado(resultSet.getString("estado"));
        solicitud.setRespuesta(resultSet.getString("respuesta"));
        solicitud.setProyectoId(resultSet.getString("proyecto_id"));
        solicitud.setFecha(resultSet.getString("fecha"));
        return solicitud;
    }
}
