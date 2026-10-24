package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.ProyectoDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Proyecto;
import jakarta.annotation.PostConstruct;
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
public class ProyectoDAOJdbcImpl implements ProyectoDAO {

    private static final String COLUMNAS = "id, nombre, descripcion, estado, fecha_inicio";
    private static final String INSERT_SQL = "INSERT INTO proyecto (" + COLUMNAS + ") VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE proyecto SET nombre = ?, descripcion = ?, estado = ?, fecha_inicio = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM proyecto WHERE id = ?";
    private static final String SELECT_ALL = "SELECT " + COLUMNAS + " FROM proyecto ORDER BY fecha_inicio";
    private static final String SELECT_BY_ID = "SELECT " + COLUMNAS + " FROM proyecto WHERE id = ?";

    private final DataSource dataSource;

    public ProyectoDAOJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void poblarSiVacio() {
        if (listarProyectos().isEmpty()) {
            for (Proyecto p : SemillaComunidad.proyectos()) {
                guardarProyecto(p);
            }
            System.out.println("Proyectos sembrados en PostgreSQL");
        }
    }

    @Override
    public Proyecto guardarProyecto(Proyecto proyecto) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, proyecto.getId());
            statement.setString(2, proyecto.getNombre());
            statement.setString(3, proyecto.getDescripcion());
            statement.setString(4, proyecto.getEstado());
            statement.setString(5, proyecto.getFechaInicio());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo guardar el proyecto", e);
        }
        return proyecto;
    }

    @Override
    public List<Proyecto> listarProyectos() {
        List<Proyecto> proyectos = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                proyectos.add(mapear(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo listar proyectos", e);
        }
        return proyectos;
    }

    @Override
    public Optional<Proyecto> buscarPorId(String id) {
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
            throw new RuntimeException("No se pudo buscar el proyecto por id", e);
        }
    }

    @Override
    public Optional<Proyecto> actualizarProyecto(String id, Proyecto proyecto) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, proyecto.getNombre());
            statement.setString(2, proyecto.getDescripcion());
            statement.setString(3, proyecto.getEstado());
            statement.setString(4, proyecto.getFechaInicio());
            statement.setString(5, id);
            int filas = statement.executeUpdate();
            if (filas == 1) {
                proyecto.setId(id);
                return Optional.of(proyecto);
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo actualizar el proyecto", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean eliminarProyecto(String id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setString(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo eliminar el proyecto", e);
        }
    }

    private Proyecto mapear(ResultSet resultSet) throws SQLException {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(resultSet.getString("id"));
        proyecto.setNombre(resultSet.getString("nombre"));
        proyecto.setDescripcion(resultSet.getString("descripcion"));
        proyecto.setEstado(resultSet.getString("estado"));
        proyecto.setFechaInicio(resultSet.getString("fecha_inicio"));
        return proyecto;
    }
}