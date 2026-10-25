package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.CuotaDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Cuota;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.exceptions.PersistenciaException;
import javax.sql.DataSource;
import java.math.BigDecimal;
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
public class CuotaDAOJdbcImpl implements CuotaDAO {

    private static final String COLUMNAS =
            "id, correo_vecino, nombre_vecino, numero_casa, periodo, monto, estado, fecha_pago";
    private static final String INSERT_SQL =
            "INSERT INTO cuota (" + COLUMNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL =
            "UPDATE cuota SET correo_vecino = ?, nombre_vecino = ?, numero_casa = ?, periodo = ?, monto = ?, estado = ?, fecha_pago = ? WHERE id = ?";
    private static final String SELECT_ALL = "SELECT " + COLUMNAS + " FROM cuota ORDER BY periodo, correo_vecino";
    private static final String SELECT_BY_ID = "SELECT " + COLUMNAS + " FROM cuota WHERE id = ?";

    private final DataSource dataSource;

    public CuotaDAOJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void poblarSiVacio() {
        if (listar().isEmpty()) {
            for (Cuota cuota : SemillaComunidad.cuotas()) {
                guardar(cuota);
            }
            System.out.println("Cuotas sembradas en PostgreSQL");
        }
    }

    @Override
    public Cuota guardar(Cuota cuota) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, cuota.getId());
            statement.setString(2, cuota.getCorreoVecino());
            statement.setString(3, cuota.getNombreVecino());
            statement.setString(4, cuota.getNumeroCasa());
            statement.setString(5, cuota.getPeriodo());
            statement.setBigDecimal(6, BigDecimal.valueOf(cuota.getMonto()));
            statement.setString(7, cuota.getEstado());
            statement.setString(8, cuota.getFechaPago());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo guardar la cuota", e);
        }
        return cuota;
    }

    @Override
    public List<Cuota> listar() {
        List<Cuota> cuotas = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                cuotas.add(mapear(resultSet));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo listar cuotas", e);
        }
        return cuotas;
    }

    @Override
    public Optional<Cuota> buscarPorId(String id) {
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
            throw new PersistenciaException("No se pudo buscar la cuota", e);
        }
    }

    @Override
    public Optional<Cuota> actualizar(String id, Cuota cuota) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, cuota.getCorreoVecino());
            statement.setString(2, cuota.getNombreVecino());
            statement.setString(3, cuota.getNumeroCasa());
            statement.setString(4, cuota.getPeriodo());
            statement.setBigDecimal(5, BigDecimal.valueOf(cuota.getMonto()));
            statement.setString(6, cuota.getEstado());
            statement.setString(7, cuota.getFechaPago());
            statement.setString(8, id);
            if (statement.executeUpdate() != 1) {
                return Optional.empty();
            }
            cuota.setId(id);
            return Optional.of(cuota);
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo actualizar la cuota", e);
        }
    }

    private Cuota mapear(ResultSet resultSet) throws SQLException {
        Cuota cuota = new Cuota();
        cuota.setId(resultSet.getString("id"));
        cuota.setCorreoVecino(resultSet.getString("correo_vecino"));
        cuota.setNombreVecino(resultSet.getString("nombre_vecino"));
        cuota.setNumeroCasa(resultSet.getString("numero_casa"));
        cuota.setPeriodo(resultSet.getString("periodo"));
        cuota.setMonto(resultSet.getBigDecimal("monto").doubleValue());
        cuota.setEstado(resultSet.getString("estado"));
        cuota.setFechaPago(resultSet.getString("fecha_pago"));
        return cuota;
    }
}
