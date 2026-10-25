package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.impl;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.exceptions.AccesoDenegadoException;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.exceptions.PersistenciaException;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.exceptions.RecursoNoEncontradoException;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.exceptions.ReglaNegocioException;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.GenerarCuotasRequestDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.GenerarCuotasResultado;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.CuotaDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.MovimientoDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.BalanceDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.CuotaRequestDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.MovimientoRequestDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Cuota;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.MiembroDirectiva;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Movimiento;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Usuario;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Vecino;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.plantillas.EmailCuotaPendiente;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.plantillas.EmailPagoRegistrado;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.NotificacionCorreo;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.TesoreriaService;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.UsuarioService;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.security.Roles;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TesoreriaServiceImpl implements TesoreriaService {

    private static final String INGRESO = "INGRESO";
    private static final String EGRESO = "EGRESO";
    private static final String PENDIENTE = "PENDIENTE";
    private static final String PAGADA = "PAGADA";

    private static final Set<String> CARGOS_ESCRITURA = Set.of(
            Roles.PRESIDENTE,
            Roles.TESORERO
    );
    private static final Set<String> TIPOS_MOVIMIENTO = Set.of(INGRESO, EGRESO);

    private final MovimientoDAO movimientoDAO;
    private final CuotaDAO cuotaDAO;
    private final UsuarioService usuarioService;
    private final NotificacionCorreo notificacionCorreo;

    public TesoreriaServiceImpl(
            MovimientoDAO movimientoDAO,
            CuotaDAO cuotaDAO,
            UsuarioService usuarioService,
            NotificacionCorreo notificacionCorreo
    ) {
        this.movimientoDAO = movimientoDAO;
        this.cuotaDAO = cuotaDAO;
        this.usuarioService = usuarioService;
        this.notificacionCorreo = notificacionCorreo;
    }

    @Override
    public List<Movimiento> listarMovimientos(String correoAdmin) {
        exigirMiembroDirectiva(correoAdmin);
        return movimientoDAO.listar();
    }

    @Override
    public Movimiento registrarMovimiento(String correoAdmin, MovimientoRequestDTO request) {
        MiembroDirectiva admin = exigirEscrituraTesoreria(correoAdmin);

        if (request.tipo() == null || request.tipo().isBlank()) {
            throw new ReglaNegocioException("El tipo es obligatorio (INGRESO o EGRESO)");
        }

        String tipo = request.tipo().trim().toUpperCase();
        if (!TIPOS_MOVIMIENTO.contains(tipo)) {
            throw new ReglaNegocioException("El tipo debe ser INGRESO o EGRESO");
        }

        if (request.concepto() == null || request.concepto().isBlank()) {
            throw new ReglaNegocioException("El concepto es obligatorio");
        }

        if (request.monto() == null || request.monto() <= 0) {
            throw new ReglaNegocioException("El monto debe ser mayor a cero");
        }

        String fecha = request.fecha() != null && !request.fecha().isBlank()
                ? request.fecha().trim()
                : LocalDate.now().toString();

        String proyectoId = request.proyectoId() != null && !request.proyectoId().isBlank()
                ? request.proyectoId().trim()
                : null;

        Movimiento movimiento = new Movimiento(
                tipo,
                request.concepto().trim(),
                request.monto(),
                fecha,
                proyectoId,
                correoAdmin,
                admin.getNombre()
        );

        return movimientoDAO.guardar(movimiento);
    }

    @Override
    public BalanceDTO calcularBalance() {
        List<Movimiento> movimientos = movimientoDAO.listar();
        double ingresos = movimientos.stream()
                .filter(movimiento -> INGRESO.equals(movimiento.getTipo()))
                .mapToDouble(Movimiento::getMonto)
                .sum();
        double egresos = movimientos.stream()
                .filter(movimiento -> EGRESO.equals(movimiento.getTipo()))
                .mapToDouble(Movimiento::getMonto)
                .sum();

        return new BalanceDTO(ingresos, egresos, ingresos - egresos, movimientos.size());
    }

    @Override
    public List<Cuota> listarCuotas(String correoAdmin) {
        exigirMiembroDirectiva(correoAdmin);
        return cuotaDAO.listar();
    }

    @Override
    public List<Cuota> listarCuotasPorVecino(String correoVecino) {
        return cuotaDAO.listar().stream()
                .filter(cuota -> cuota.getCorreoVecino().equalsIgnoreCase(correoVecino))
                .toList();
    }

    @Override
    public Cuota crearCuota(String correoAdmin, CuotaRequestDTO request) {
        exigirEscrituraTesoreria(correoAdmin);

        if (request.correoVecino() == null || request.correoVecino().isBlank()) {
            throw new ReglaNegocioException("El correo del vecino es obligatorio");
        }
        if (request.periodo() == null || request.periodo().isBlank()) {
            throw new ReglaNegocioException("El periodo es obligatorio (YYYY-MM)");
        }
        if (request.monto() == null || request.monto() <= 0) {
            throw new ReglaNegocioException("El monto debe ser mayor a cero");
        }

        Usuario usuario = usuarioService.buscarPorCorreo(request.correoVecino().trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el vecino"));

        if (!(usuario instanceof Vecino vecino)) {
            throw new ReglaNegocioException("Solo se pueden asignar cuotas a vecinos");
        }

        String periodo = request.periodo().trim();
        boolean duplicada = cuotaDAO.listar().stream()
                .anyMatch(cuota ->
                        cuota.getCorreoVecino().equalsIgnoreCase(vecino.getCorreo())
                                && cuota.getPeriodo().equalsIgnoreCase(periodo));

        if (duplicada) {
            throw new ReglaNegocioException("Ya existe una cuota para ese vecino en el periodo");
        }

        Cuota cuota = new Cuota(
                vecino.getCorreo(),
                vecino.getNombre(),
                vecino.getNumeroCasa(),
                periodo,
                request.monto(),
                PENDIENTE
        );

        Cuota guardada = cuotaDAO.guardar(cuota);
        notificacionCorreo.avisar(new EmailCuotaPendiente(
                guardada.getCorreoVecino(),
                guardada.getPeriodo(),
                guardada.getMonto(),
                guardada.getNombreVecino()
        ));
        return guardada;
    }

    @Override
    public GenerarCuotasResultado generarCuotasDelMes(String correoAdmin, GenerarCuotasRequestDTO request) {
        exigirEscrituraTesoreria(correoAdmin);
        if (request.periodo() == null || request.periodo().isBlank()) {
            throw new ReglaNegocioException("El periodo es obligatorio (YYYY-MM)");
        }
        if (request.monto() == null || request.monto() <= 0) {
            throw new ReglaNegocioException("El monto debe ser mayor a cero");
        }

        String periodo = request.periodo().trim();
        List<Vecino> vecinos = usuarioService.listarUsuarios().stream()
                .filter(Vecino.class::isInstance)
                .map(Vecino.class::cast)
                .toList();

        AtomicInteger creadas = new AtomicInteger();
        AtomicInteger existentes = new AtomicInteger();
        AtomicInteger fallidas = new AtomicInteger();
        int hilos = Math.max(1, Math.min(4, vecinos.size()));
        ExecutorService executor = Executors.newFixedThreadPool(hilos);
        try {
            List<Future<?>> tareas = new ArrayList<>();
            for (Vecino vecino : vecinos) {
                tareas.add(executor.submit(() ->
                        insertarCuotaDelMes(vecino, periodo, request.monto(), creadas, existentes, fallidas)));
            }
            for (Future<?> tarea : tareas) {
                tarea.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PersistenciaException("Se interrumpio la generacion de cuotas", e);
        } catch (ExecutionException e) {
            throw new PersistenciaException("Fallo una tarea de generacion de cuotas", e.getCause());
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        return new GenerarCuotasResultado(creadas.get(), existentes.get(), fallidas.get());
    }

    /**
     * Each call to {@code cuotaDAO.guardar} borrows its own pooled connection.
     * A connection is never shared between these threads.
     */
    private void insertarCuotaDelMes(
            Vecino vecino,
            String periodo,
            double monto,
            AtomicInteger creadas,
            AtomicInteger existentes,
            AtomicInteger fallidas
    ) {
        Cuota cuota = new Cuota(
                vecino.getCorreo(),
                vecino.getNombre(),
                vecino.getNumeroCasa(),
                periodo,
                monto,
                PENDIENTE
        );
        try {
            Cuota guardada = cuotaDAO.guardar(cuota);
            creadas.incrementAndGet();
            notificacionCorreo.avisar(new EmailCuotaPendiente(
                    guardada.getCorreoVecino(),
                    guardada.getPeriodo(),
                    guardada.getMonto(),
                    guardada.getNombreVecino()
            ));
        } catch (PersistenciaException ex) {
            if (esClaveDuplicada(ex)) {
                existentes.incrementAndGet();
                return;
            }
            fallidas.incrementAndGet();
        }
    }

    private boolean esClaveDuplicada(Throwable error) {
        Throwable actual = error;
        while (actual != null) {
            if (actual instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
                return true;
            }
            actual = actual.getCause();
        }
        return false;
    }

    @Override
    public Cuota marcarCuotaPagada(String correoAdmin, String cuotaId) {
        MiembroDirectiva admin = exigirEscrituraTesoreria(correoAdmin);

        Cuota cuota = cuotaDAO.buscarPorId(cuotaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la cuota"));

        if (PAGADA.equals(cuota.getEstado())) {
            throw new ReglaNegocioException("La cuota ya esta pagada");
        }

        String fechaPago = LocalDate.now().toString();
        cuota.setEstado(PAGADA);
        cuota.setFechaPago(fechaPago);

        Cuota actualizada = cuotaDAO.actualizar(cuotaId, cuota)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se pudo actualizar la cuota"));

        Movimiento ingreso = new Movimiento(
                INGRESO,
                "Cuota " + cuota.getPeriodo() + " - " + cuota.getNombreVecino(),
                cuota.getMonto(),
                fechaPago,
                null,
                correoAdmin,
                admin.getNombre()
        );
        ingreso.setCuotaId(cuota.getId());
        movimientoDAO.guardar(ingreso);

        notificacionCorreo.avisar(new EmailPagoRegistrado(
                actualizada.getCorreoVecino(),
                actualizada.getPeriodo(),
                actualizada.getMonto(),
                actualizada.getFechaPago()
        ));

        return actualizada;
    }

    private MiembroDirectiva exigirEscrituraTesoreria(String correoAdmin) {
        MiembroDirectiva miembro = exigirMiembroDirectiva(correoAdmin);
        String cargo = Roles.normalizar(miembro.getCargo());

        if (!CARGOS_ESCRITURA.contains(cargo)) {
            throw new AccesoDenegadoException("Solo Presidente o Tesorero pueden escribir en tesoreria");
        }

        return miembro;
    }

    private MiembroDirectiva exigirMiembroDirectiva(String correoAdmin) {
        Usuario usuario = usuarioService.buscarPorCorreo(correoAdmin)
                .orElseThrow(() -> new AccesoDenegadoException("Usuario no autenticado"));

        if (!(usuario instanceof MiembroDirectiva miembro)) {
            throw new AccesoDenegadoException("Se requiere un miembro de la directiva");
        }

        return miembro;
    }
}
