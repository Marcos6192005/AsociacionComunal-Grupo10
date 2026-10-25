package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.EmailLogDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.EstadoEnvio;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.RegistroEmail;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class EmailLogDAOJdbcImpl implements EmailLogDAO {

    private static final String COLUMNAS =
            "id, destinatario, asunto, fecha_creacion, estado, intentos, fecha_envio, ultimo_error, hilo_procesador";
    private static final String INSERT_SQL =
            "INSERT INTO registro_email (" + COLUMNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL =
            "UPDATE registro_email SET destinatario = ?, asunto = ?, fecha_creacion = ?, estado = ?, intentos = ?, fecha_envio = ?, ultimo_error = ?, hilo_procesador = ? WHERE id = ?";
    private static final String SELECT_ALL =
            "SELECT " + COLUMNAS + " FROM registro_email ORDER BY fecha_creacion DESC";
    private static final String SELECT_BY_ID = "SELECT " + COLUMNAS + " FROM registro_email WHERE id = ?";

    private final DataSource dataSource;

    public EmailLogDAOJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void poblarSiVacio() {
        if (listarTodos().isEmpty()) {
            for (RegistroEmail registro : SemillaComunidad.correos()) {
                guardar(registro);
            }
            System.out.println("Bitacora de correos sembrada en PostgreSQL");
        }
    }

    @Override
    public void guardar(RegistroEmail registro) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, registro.getId());
            statement.setString(2, registro.getDestinatario());
            statement.setString(3, registro.getAsunto());
            statement.setTimestamp(4, Timestamp.valueOf(registro.getFechaCreacion()));
            statement.setString(5, registro.getEstado().name());
            statement.setInt(6, registro.getIntentos());
            statement.setTimestamp(7, timestamp(registro.getFechaEnvio()));
            statement.setString(8, registro.getUltimoError());
            statement.setString(9, registro.getHiloProcesador());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo guardar el registro de correo", e);
        }
    }

    @Override
    public void actualizar(RegistroEmail registro) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, registro.getDestinatario());
            statement.setString(2, registro.getAsunto());
            statement.setTimestamp(3, Timestamp.valueOf(registro.getFechaCreacion()));
            statement.setString(4, registro.getEstado().name());
            statement.setInt(5, registro.getIntentos());
            statement.setTimestamp(6, timestamp(registro.getFechaEnvio()));
            statement.setString(7, registro.getUltimoError());
            statement.setString(8, registro.getHiloProcesador());
            statement.setString(9, registro.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo actualizar el registro de correo", e);
        }
    }

    @Override
    public Optional<RegistroEmail> buscarPorId(String id) {
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
            throw new RuntimeException("No se pudo buscar el registro de correo", e);
        }
    }

    @Override
    public List<RegistroEmail> listarTodos() {
        List<RegistroEmail> registros = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                registros.add(mapear(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo listar la bitacora de correos", e);
        }
        return registros;
    }

    private RegistroEmail mapear(ResultSet resultSet) throws SQLException {
        Timestamp fechaEnvio = resultSet.getTimestamp("fecha_envio");
        return new RegistroEmail(
                resultSet.getString("id"),
                resultSet.getString("destinatario"),
                resultSet.getString("asunto"),
                resultSet.getTimestamp("fecha_creacion").toLocalDateTime(),
                EstadoEnvio.valueOf(resultSet.getString("estado")),
                resultSet.getInt("intentos"),
                fechaEnvio == null ? null : fechaEnvio.toLocalDateTime(),
                resultSet.getString("ultimo_error"),
                resultSet.getString("hilo_procesador")
        );
    }

    private Timestamp timestamp(LocalDateTime valor) {
        return valor == null ? null : Timestamp.valueOf(valor);
    }
}
