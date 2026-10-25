package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.MovimientoDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Movimiento;
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

@Repository
@Primary
@DependsOn({"usuarioDAOJdbcImpl", "proyectoDAOJdbcImpl", "cuotaDAOJdbcImpl"})
public class MovimientoDAOJdbcImpl implements MovimientoDAO {

    private static final String COLUMNAS =
            "id, tipo, concepto, monto, fecha, proyecto_id, correo_registro, nombre_registro, cuota_id";
    private static final String INSERT_SQL =
            "INSERT INTO movimiento (" + COLUMNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_ALL = "SELECT " + COLUMNAS + " FROM movimiento ORDER BY fecha, id";

    private final DataSource dataSource;

    public MovimientoDAOJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void poblarSiVacio() {
        if (listar().isEmpty()) {
            for (Movimiento movimiento : SemillaComunidad.movimientos()) {
                guardar(movimiento);
            }
            System.out.println("Movimientos sembrados en PostgreSQL");
        }
    }

    @Override
    public Movimiento guardar(Movimiento movimiento) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, movimiento.getId());
            statement.setString(2, movimiento.getTipo());
            statement.setString(3, movimiento.getConcepto());
            statement.setBigDecimal(4, BigDecimal.valueOf(movimiento.getMonto()));
            statement.setString(5, movimiento.getFecha());
            statement.setString(6, movimiento.getProyectoId());
            statement.setString(7, movimiento.getCorreoRegistro());
            statement.setString(8, movimiento.getNombreRegistro());
            statement.setString(9, movimiento.getCuotaId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo guardar el movimiento", e);
        }
        return movimiento;
    }

    @Override
    public List<Movimiento> listar() {
        List<Movimiento> movimientos = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                movimientos.add(mapear(resultSet));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo listar movimientos", e);
        }
        return movimientos;
    }

    private Movimiento mapear(ResultSet resultSet) throws SQLException {
        Movimiento movimiento = new Movimiento();
        movimiento.setId(resultSet.getString("id"));
        movimiento.setTipo(resultSet.getString("tipo"));
        movimiento.setConcepto(resultSet.getString("concepto"));
        movimiento.setMonto(resultSet.getBigDecimal("monto").doubleValue());
        movimiento.setFecha(resultSet.getString("fecha"));
        movimiento.setProyectoId(resultSet.getString("proyecto_id"));
        movimiento.setCorreoRegistro(resultSet.getString("correo_registro"));
        movimiento.setNombreRegistro(resultSet.getString("nombre_registro"));
        movimiento.setCuotaId(resultSet.getString("cuota_id"));
        return movimiento;
    }
}
