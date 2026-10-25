package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.impl;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.exceptions.AccesoDenegadoException;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.exceptions.RecursoNoEncontradoException;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.exceptions.ReglaNegocioException;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.ActaDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.ComunicadoDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.SolicitudDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.ActaRequestDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.AcuerdoActaDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.ComunicadoRequestDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.ResponderSolicitudRequestDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.SolicitudRequestDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Acta;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.AcuerdoActa;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Comunicado;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.MiembroDirectiva;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Solicitud;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Usuario;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Vecino;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.plantillas.EmailComunicado;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.plantillas.EmailSolicitudRecibida;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.plantillas.EmailSolicitudRespondida;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.plantillas.PlantillaEmail;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.NotificacionCorreo;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.SecretariaService;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.UsuarioService;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.security.Roles;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class SecretariaServiceImpl implements SecretariaService {

    private static final String ESTADO_RECIBIDA = "RECIBIDA";
    private static final String ESTADO_EN_REVISION = "EN_REVISION";
    private static final String ESTADO_RESUELTA = "RESUELTA";
    private static final String ESTADO_RECHAZADA = "RECHAZADA";
    private static final String ESTADO_BORRADOR = "BORRADOR";
    private static final String ESTADO_PUBLICADA = "PUBLICADA";

    private static final Set<String> ESTADOS_RESPUESTA = Set.of(
            ESTADO_EN_REVISION,
            ESTADO_RESUELTA,
            ESTADO_RECHAZADA
    );

    private static final Set<String> CARGOS_ESCRITURA = Set.of(
            Roles.PRESIDENTE,
            Roles.SECRETARIO
    );

    private static final Set<String> ESTADOS_CON_CORREO = Set.of(
            ESTADO_RESUELTA,
            ESTADO_RECHAZADA
    );

    private final SolicitudDAO solicitudDAO;
    private final ActaDAO actaDAO;
    private final ComunicadoDAO comunicadoDAO;
    private final UsuarioService usuarioService;
    private final NotificacionCorreo notificacionCorreo;

    public SecretariaServiceImpl(
            SolicitudDAO solicitudDAO,
            ActaDAO actaDAO,
            ComunicadoDAO comunicadoDAO,
            UsuarioService usuarioService,
            NotificacionCorreo notificacionCorreo
    ) {
        this.solicitudDAO = solicitudDAO;
        this.actaDAO = actaDAO;
        this.comunicadoDAO = comunicadoDAO;
        this.usuarioService = usuarioService;
        this.notificacionCorreo = notificacionCorreo;
    }

    @Override
    public List<Solicitud> listarSolicitudes() {
        return solicitudDAO.listar();
    }

    @Override
    public List<Solicitud> listarSolicitudesPorAutor(String correoAutor) {
        return solicitudDAO.listar().stream()
                .filter(solicitud -> solicitud.getCorreoAutor().equalsIgnoreCase(correoAutor))
                .toList();
    }

    @Override
    public Solicitud crearSolicitud(String correoAutor, SolicitudRequestDTO request) {
        if (request.titulo() == null || request.titulo().isBlank()) {
            throw new ReglaNegocioException("El titulo es obligatorio");
        }
        if (request.descripcion() == null || request.descripcion().isBlank()) {
            throw new ReglaNegocioException("La descripcion es obligatoria");
        }

        String nombre = usuarioService.buscarPorCorreo(correoAutor)
                .map(Usuario::getNombre)
                .orElse("Vecino");

        Solicitud solicitud = new Solicitud(
                request.titulo().trim(),
                request.descripcion().trim(),
                correoAutor,
                nombre,
                ESTADO_RECIBIDA,
                LocalDate.now().toString()
        );

        Solicitud guardada = solicitudDAO.guardar(solicitud);
        avisarSolicitudRecibida(guardada);
        return guardada;
    }

    @Override
    public Solicitud responderSolicitud(String correoAdmin, String solicitudId, ResponderSolicitudRequestDTO request) {
        exigirEscrituraSecretaria(correoAdmin);

        Solicitud existente = solicitudDAO.buscarPorId(solicitudId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la solicitud"));

        if (request.estado() == null || request.estado().isBlank()) {
            throw new ReglaNegocioException("Debes indicar el estado de la respuesta");
        }

        String estado = request.estado().trim().toUpperCase();
        if (!ESTADOS_RESPUESTA.contains(estado)) {
            throw new ReglaNegocioException("Estado invalido. Usa EN_REVISION, RESUELTA o RECHAZADA");
        }

        existente.setEstado(estado);
        if (request.respuesta() != null && !request.respuesta().isBlank()) {
            existente.setRespuesta(request.respuesta().trim());
        }
        if (request.proyectoId() != null && !request.proyectoId().isBlank()) {
            existente.setProyectoId(request.proyectoId().trim());
        }

        Solicitud actualizada = solicitudDAO.actualizar(solicitudId, existente)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se pudo actualizar la solicitud"));

        if (ESTADOS_CON_CORREO.contains(actualizada.getEstado())) {
            notificacionCorreo.avisar(new EmailSolicitudRespondida(
                    actualizada.getCorreoAutor(),
                    actualizada.getTitulo(),
                    actualizada.getEstado(),
                    actualizada.getRespuesta()
            ));
        }

        return actualizada;
    }

    @Override
    public List<Acta> listarActasAdmin(String correoAdmin) {
        exigirMiembroDirectiva(correoAdmin);
        return actaDAO.listar();
    }

    @Override
    public List<Acta> listarActasPublicadas() {
        return actaDAO.listar().stream()
                .filter(acta -> ESTADO_PUBLICADA.equals(acta.getEstado()))
                .toList();
    }

    @Override
    public Acta crearActa(String correoAdmin, ActaRequestDTO request) {
        MiembroDirectiva admin = exigirEscrituraSecretaria(correoAdmin);

        if (request.titulo() == null || request.titulo().isBlank()) {
            throw new ReglaNegocioException("El titulo del acta es obligatorio");
        }
        if (request.contenido() == null || request.contenido().isBlank()) {
            throw new ReglaNegocioException("El contenido del acta es obligatorio");
        }

        String fecha = request.fecha() != null && !request.fecha().isBlank()
                ? request.fecha().trim()
                : LocalDate.now().toString();

        List<AcuerdoActa> acuerdos = mapearAcuerdos(request.acuerdos());

        Acta acta = new Acta(
                request.titulo().trim(),
                request.contenido().trim(),
                fecha,
                ESTADO_BORRADOR,
                correoAdmin,
                admin.getNombre(),
                acuerdos
        );

        return actaDAO.guardar(acta);
    }

    @Override
    public Acta publicarActa(String correoAdmin, String actaId) {
        exigirEscrituraSecretaria(correoAdmin);

        Acta existente = actaDAO.buscarPorId(actaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el acta"));

        existente.setEstado(ESTADO_PUBLICADA);

        return actaDAO.actualizar(actaId, existente)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se pudo publicar el acta"));
    }

    @Override
    public List<Comunicado> listarComunicados() {
        return comunicadoDAO.listar();
    }

    @Override
    public Comunicado crearComunicado(String correoAdmin, ComunicadoRequestDTO request) {
        MiembroDirectiva admin = exigirEscrituraSecretaria(correoAdmin);

        if (request.titulo() == null || request.titulo().isBlank()) {
            throw new ReglaNegocioException("El titulo del comunicado es obligatorio");
        }
        if (request.contenido() == null || request.contenido().isBlank()) {
            throw new ReglaNegocioException("El contenido del comunicado es obligatorio");
        }

        Comunicado comunicado = new Comunicado(
                request.titulo().trim(),
                request.contenido().trim(),
                LocalDate.now().toString(),
                correoAdmin,
                admin.getNombre()
        );

        Comunicado guardado = comunicadoDAO.guardar(comunicado);
        avisarComunicado(guardado);
        return guardado;
    }

    private List<AcuerdoActa> mapearAcuerdos(List<AcuerdoActaDTO> acuerdos) {
        if (acuerdos == null || acuerdos.isEmpty()) {
            return new ArrayList<>();
        }

        List<AcuerdoActa> resultado = new ArrayList<>();
        for (AcuerdoActaDTO acuerdo : acuerdos) {
            if (acuerdo == null || acuerdo.texto() == null || acuerdo.texto().isBlank()) {
                continue;
            }
            String proyectoId = acuerdo.proyectoId() != null && !acuerdo.proyectoId().isBlank()
                    ? acuerdo.proyectoId().trim()
                    : null;
            resultado.add(new AcuerdoActa(acuerdo.texto().trim(), proyectoId));
        }
        return resultado;
    }

    private void avisarSolicitudRecibida(Solicitud solicitud) {
        List<PlantillaEmail> avisos = new ArrayList<>();
        Set<String> vistos = new HashSet<>();

        for (Usuario usuario : usuarioService.listarUsuarios()) {
            if (!(usuario instanceof MiembroDirectiva miembro)) {
                continue;
            }
            if (!CARGOS_ESCRITURA.contains(Roles.normalizar(miembro.getCargo()))) {
                continue;
            }
            String correo = correoUtil(miembro.getCorreo(), vistos);
            if (correo == null) {
                continue;
            }
            avisos.add(new EmailSolicitudRecibida(
                    correo,
                    solicitud.getTitulo(),
                    solicitud.getNombreAutor()
            ));
        }

        notificacionCorreo.avisarVarios(avisos);
    }

    private void avisarComunicado(Comunicado comunicado) {
        List<PlantillaEmail> avisos = new ArrayList<>();
        Set<String> vistos = new HashSet<>();

        for (Usuario usuario : usuarioService.listarUsuarios()) {
            if (!(usuario instanceof Vecino)) {
                continue;
            }
            String correo = correoUtil(usuario.getCorreo(), vistos);
            if (correo == null) {
                continue;
            }
            avisos.add(new EmailComunicado(correo, comunicado.getTitulo(), comunicado.getContenido()));
        }

        notificacionCorreo.avisarVarios(avisos);
    }

    private static String correoUtil(String correo, Set<String> vistos) {
        if (correo == null || correo.isBlank()) {
            return null;
        }
        String limpio = correo.trim();
        if (!vistos.add(limpio.toLowerCase(Locale.ROOT))) {
            return null;
        }
        return limpio;
    }

    private MiembroDirectiva exigirEscrituraSecretaria(String correoAdmin) {
        MiembroDirectiva miembro = exigirMiembroDirectiva(correoAdmin);
        String cargo = Roles.normalizar(miembro.getCargo());

        if (!CARGOS_ESCRITURA.contains(cargo)) {
            throw new AccesoDenegadoException("Solo Presidente o Secretario pueden escribir en secretaria");
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
