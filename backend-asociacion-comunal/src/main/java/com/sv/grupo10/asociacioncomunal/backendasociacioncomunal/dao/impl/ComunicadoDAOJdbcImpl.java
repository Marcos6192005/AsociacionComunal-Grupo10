package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.ComunicadoDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Comunicado;
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
@DependsOn("usuarioDAOJdbcImpl")
public class ComunicadoDAOJdbcImpl implements ComunicadoDAO {

    private static final String COLUMNAS = "id, titulo, contenido, fecha, correo_autor, nombre_autor";
    private static final String INSERT_SQL = "INSERT INTO comunicado (" + COLUMNAS + ") VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_ALL = "SELECT " + COLUMNAS + " FROM comunicado ORDER BY fecha";
    private static final String SELECT_BY_ID = "SELECT " + COLUMNAS + " FROM comunicado WHERE id = ?";

    private final DataSource dataSource;

    public ComunicadoDAOJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void poblarSiVacio() {
        if (listar().isEmpty()) {
            for (Comunicado comunicado : SemillaComunidad.comunicados()) {
                guardar(comunicado);
            }
            System.out.println("Comunicados sembrados en PostgreSQL");
        }
    }

    @Override
    public Comunicado guardar(Comunicado comunicado) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, comunicado.getId());
            statement.setString(2, comunicado.getTitulo());
            statement.setString(3, comunicado.getContenido());
            statement.setString(4, comunicado.getFecha());
            statement.setString(5, comunicado.getCorreoAutor());
            statement.setString(6, comunicado.getNombreAutor());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo guardar el comunicado", e);
        }
        return comunicado;
    }

    @Override
    public List<Comunicado> listar() {
        List<Comunicado> comunicados = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                comunicados.add(mapear(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo listar comunicados", e);
        }
        return comunicados;
    }

    @Override
    public Optional<Comunicado> buscarPorId(String id) {
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
            throw new RuntimeException("No se pudo buscar el comunicado", e);
        }
    }

    private Comunicado mapear(ResultSet resultSet) throws SQLException {
        Comunicado comunicado = new Comunicado();
        comunicado.setId(resultSet.getString("id"));
        comunicado.setTitulo(resultSet.getString("titulo"));
        comunicado.setContenido(resultSet.getString("contenido"));
        comunicado.setFecha(resultSet.getString("fecha"));
        comunicado.setCorreoAutor(resultSet.getString("correo_autor"));
        comunicado.setNombreAutor(resultSet.getString("nombre_autor"));
        return comunicado;
    }
}
