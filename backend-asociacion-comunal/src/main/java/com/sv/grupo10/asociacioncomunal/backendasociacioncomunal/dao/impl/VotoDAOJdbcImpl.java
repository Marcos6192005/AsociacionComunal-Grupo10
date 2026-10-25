package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.VotoDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Voto;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.exceptions.PersistenciaException;
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
public class VotoDAOJdbcImpl implements VotoDAO {

    private static final String COLUMNAS = "id, proyecto_id, correo_usuario, valor";
    private static final String INSERT_SQL = "INSERT INTO voto (" + COLUMNAS + ") VALUES (?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE voto SET valor = ? WHERE id = ?";
    private static final String SELECT_ALL = "SELECT " + COLUMNAS + " FROM voto ORDER BY proyecto_id, correo_usuario";
    private static final String SELECT_BY_PROYECTO = "SELECT " + COLUMNAS + " FROM voto WHERE proyecto_id = ? ORDER BY correo_usuario";
    private static final String SELECT_BY_PROYECTO_Y_CORREO =
            "SELECT " + COLUMNAS + " FROM voto WHERE proyecto_id = ? AND lower(correo_usuario) = lower(?)";

    private final DataSource dataSource;

    public VotoDAOJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void poblarSiVacio() {
        if (listarVotos().isEmpty()) {
            for (Voto voto : SemillaComunidad.votos()) {
                guardarVoto(voto);
            }
            System.out.println("Votos sembrados en PostgreSQL");
        }
    }

    @Override
    public List<Voto> listarVotos() {
        List<Voto> votos = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                votos.add(mapear(resultSet));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo listar votos", e);
        }
        return votos;
    }

    @Override
    public List<Voto> listarPorProyecto(String proyectoId) {
        List<Voto> votos = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_PROYECTO)) {
            statement.setString(1, proyectoId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    votos.add(mapear(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo listar votos del proyecto", e);
        }
        return votos;
    }

    @Override
    public Optional<Voto> buscarPorProyectoYCorreo(String proyectoId, String correo) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_PROYECTO_Y_CORREO)) {
            statement.setString(1, proyectoId);
            statement.setString(2, correo);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapear(resultSet));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo buscar el voto", e);
        }
    }

    @Override
    public Voto guardarVoto(Voto voto) {
        Optional<Voto> existente = buscarPorProyectoYCorreo(voto.getProyectoId(), voto.getCorreoUsuario());
        if (existente.isPresent()) {
            return actualizarValor(existente.get().getId(), voto);
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, voto.getId());
            statement.setString(2, voto.getProyectoId());
            statement.setString(3, voto.getCorreoUsuario());
            statement.setString(4, voto.getValor());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo guardar el voto", e);
        }
        return voto;
    }

    private Voto actualizarValor(String id, Voto voto) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, voto.getValor());
            statement.setString(2, id);
            statement.executeUpdate();
            voto.setId(id);
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo actualizar el voto", e);
        }
        return voto;
    }

    private Voto mapear(ResultSet resultSet) throws SQLException {
        Voto voto = new Voto();
        voto.setId(resultSet.getString("id"));
        voto.setProyectoId(resultSet.getString("proyecto_id"));
        voto.setCorreoUsuario(resultSet.getString("correo_usuario"));
        voto.setValor(resultSet.getString("valor"));
        return voto;
    }
}
