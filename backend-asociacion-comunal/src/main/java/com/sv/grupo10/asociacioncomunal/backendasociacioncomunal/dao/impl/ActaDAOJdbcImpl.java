package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.ActaDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Acta;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.AcuerdoActa;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Primary
@DependsOn({"usuarioDAOJdbcImpl", "proyectoDAOJdbcImpl"})
public class ActaDAOJdbcImpl implements ActaDAO {

    private static final String COLUMNAS =
            "id, titulo, contenido, fecha, estado, correo_autor, nombre_autor";
    private static final String INSERT_SQL =
            "INSERT INTO acta (" + COLUMNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL =
            "UPDATE acta SET titulo = ?, contenido = ?, fecha = ?, estado = ?, correo_autor = ?, nombre_autor = ? WHERE id = ?";
    private static final String SELECT_ALL = "SELECT " + COLUMNAS + " FROM acta ORDER BY fecha";
    private static final String SELECT_BY_ID = "SELECT " + COLUMNAS + " FROM acta WHERE id = ?";
    private static final String INSERT_ACUERDO =
            "INSERT INTO acuerdo_acta (acta_id, texto, proyecto_id, posicion) VALUES (?, ?, ?, ?)";
    private static final String DELETE_ACUERDOS = "DELETE FROM acuerdo_acta WHERE acta_id = ?";
    private static final String SELECT_ACUERDOS =
            "SELECT acta_id, texto, proyecto_id FROM acuerdo_acta ORDER BY acta_id, posicion";
    private static final String SELECT_ACUERDOS_POR_ACTA =
            "SELECT texto, proyecto_id FROM acuerdo_acta WHERE acta_id = ? ORDER BY posicion";

    private final DataSource dataSource;

    public ActaDAOJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void poblarSiVacio() {
        if (listar().isEmpty()) {
            for (Acta acta : SemillaComunidad.actas()) {
                guardar(acta);
            }
            System.out.println("Actas sembradas en PostgreSQL");
        }
    }

    @Override
    public Acta guardar(Acta acta) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                insertarActa(connection, acta);
                insertarAcuerdos(connection, acta.getId(), acta.getAcuerdos());
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo guardar el acta", e);
        }
        return acta;
    }

    @Override
    public List<Acta> listar() {
        List<Acta> actas = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                actas.add(mapearActa(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo listar actas", e);
        }
        adjuntarAcuerdos(actas);
        return actas;
    }

    @Override
    public Optional<Acta> buscarPorId(String id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                Acta acta = mapearActa(resultSet);
                acta.setAcuerdos(acuerdosDe(id));
                return Optional.of(acta);
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo buscar el acta", e);
        }
    }

    @Override
    public Optional<Acta> actualizar(String id, Acta acta) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
                    statement.setString(1, acta.getTitulo());
                    statement.setString(2, acta.getContenido());
                    statement.setString(3, acta.getFecha());
                    statement.setString(4, acta.getEstado());
                    statement.setString(5, acta.getCorreoAutor());
                    statement.setString(6, acta.getNombreAutor());
                    statement.setString(7, id);
                    if (statement.executeUpdate() != 1) {
                        connection.rollback();
                        return Optional.empty();
                    }
                }
                try (PreparedStatement delete = connection.prepareStatement(DELETE_ACUERDOS)) {
                    delete.setString(1, id);
                    delete.executeUpdate();
                }
                insertarAcuerdos(connection, id, acta.getAcuerdos());
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo actualizar el acta", e);
        }
        acta.setId(id);
        return Optional.of(acta);
    }

    private void insertarActa(Connection connection, Acta acta) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, acta.getId());
            statement.setString(2, acta.getTitulo());
            statement.setString(3, acta.getContenido());
            statement.setString(4, acta.getFecha());
            statement.setString(5, acta.getEstado());
            statement.setString(6, acta.getCorreoAutor());
            statement.setString(7, acta.getNombreAutor());
            statement.executeUpdate();
        }
    }

    private void insertarAcuerdos(Connection connection, String actaId, List<AcuerdoActa> acuerdos) throws SQLException {
        if (acuerdos == null || acuerdos.isEmpty()) {
            return;
        }
        try (PreparedStatement statement = connection.prepareStatement(INSERT_ACUERDO)) {
            for (int i = 0; i < acuerdos.size(); i++) {
                AcuerdoActa acuerdo = acuerdos.get(i);
                statement.setString(1, actaId);
                statement.setString(2, acuerdo.getTexto());
                statement.setString(3, acuerdo.getProyectoId());
                statement.setInt(4, i);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void adjuntarAcuerdos(List<Acta> actas) {
        Map<String, List<AcuerdoActa>> porActa = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ACUERDOS);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String actaId = resultSet.getString("acta_id");
                porActa.computeIfAbsent(actaId, id -> new ArrayList<>()).add(mapearAcuerdo(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron listar los acuerdos", e);
        }
        for (Acta acta : actas) {
            acta.setAcuerdos(porActa.getOrDefault(acta.getId(), new ArrayList<>()));
        }
    }

    private List<AcuerdoActa> acuerdosDe(String actaId) {
        List<AcuerdoActa> acuerdos = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ACUERDOS_POR_ACTA)) {
            statement.setString(1, actaId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    acuerdos.add(mapearAcuerdo(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron leer los acuerdos del acta", e);
        }
        return acuerdos;
    }

    private Acta mapearActa(ResultSet resultSet) throws SQLException {
        Acta acta = new Acta();
        acta.setId(resultSet.getString("id"));
        acta.setTitulo(resultSet.getString("titulo"));
        acta.setContenido(resultSet.getString("contenido"));
        acta.setFecha(resultSet.getString("fecha"));
        acta.setEstado(resultSet.getString("estado"));
        acta.setCorreoAutor(resultSet.getString("correo_autor"));
        acta.setNombreAutor(resultSet.getString("nombre_autor"));
        return acta;
    }

    private AcuerdoActa mapearAcuerdo(ResultSet resultSet) throws SQLException {
        return new AcuerdoActa(resultSet.getString("texto"), resultSet.getString("proyecto_id"));
    }
}
