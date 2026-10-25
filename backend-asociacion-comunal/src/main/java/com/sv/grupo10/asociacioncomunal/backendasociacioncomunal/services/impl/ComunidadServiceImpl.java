package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.impl;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.exceptions.RecursoNoEncontradoException;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.exceptions.ReglaNegocioException;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.ComentarioDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.ProyectoDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.VotoDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.ComentarioResponseDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.ComunidadProyectoDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.dtos.ParticipacionProyectoDTO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Comentario;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Proyecto;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Usuario;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Voto;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.ComunidadService;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.services.UsuarioService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ComunidadServiceImpl implements ComunidadService {

    private static final String A_FAVOR = "A_FAVOR";
    private static final String EN_CONTRA = "EN_CONTRA";

    private final ProyectoDAO proyectoDAO;
    private final VotoDAO votoDAO;
    private final ComentarioDAO comentarioDAO;
    private final UsuarioService usuarioService;

    public ComunidadServiceImpl(
            ProyectoDAO proyectoDAO,
            VotoDAO votoDAO,
            ComentarioDAO comentarioDAO,
            UsuarioService usuarioService
    ) {
        this.proyectoDAO = proyectoDAO;
        this.votoDAO = votoDAO;
        this.comentarioDAO = comentarioDAO;
        this.usuarioService = usuarioService;
    }

    @Override
    public List<ComunidadProyectoDTO> listarProyectos(String correoUsuario) {
        return proyectoDAO.listarProyectos().stream()
                .map(proyecto -> mapearProyecto(proyecto, correoUsuario))
                .toList();
    }

    @Override
    public List<ParticipacionProyectoDTO> listarParticipacion() {
        return proyectoDAO.listarProyectos().stream()
                .map(this::mapearParticipacion)
                .toList();
    }

    @Override
    public Optional<ComunidadProyectoDTO> obtenerProyecto(String proyectoId, String correoUsuario) {
        return proyectoDAO.buscarPorId(proyectoId)
                .map(proyecto -> mapearProyecto(proyecto, correoUsuario));
    }

    @Override
    public Voto votar(String proyectoId, String correoUsuario, String valor) {
        proyectoDAO.buscarPorId(proyectoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el proyecto"));

        String votoNormalizado = normalizarVoto(valor);
        Voto voto = new Voto(proyectoId, correoUsuario, votoNormalizado);
        return votoDAO.guardarVoto(voto);
    }

    @Override
    public List<ComentarioResponseDTO> listarComentarios() {
        List<Proyecto> proyectos = proyectoDAO.listarProyectos();

        return comentarioDAO.listarComentarios().stream()
                .map(comentario -> {
                    String nombreProyecto = proyectos.stream()
                            .filter(proyecto -> proyecto.getId().equals(comentario.getProyectoId()))
                            .map(Proyecto::getNombre)
                            .findFirst()
                            .orElse("Proyecto");

                    return new ComentarioResponseDTO(
                            comentario.getId(),
                            comentario.getProyectoId(),
                            nombreProyecto,
                            comentario.getNombreAutor(),
                            comentario.getTexto(),
                            comentario.getFecha()
                    );
                })
                .toList();
    }

    @Override
    public Comentario comentar(String proyectoId, String correoUsuario, String texto) {
        proyectoDAO.buscarPorId(proyectoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el proyecto"));

        if (texto == null || texto.isBlank()) {
            throw new ReglaNegocioException("El comentario no puede estar vacio");
        }

        String nombre = usuarioService.buscarPorCorreo(correoUsuario)
                .map(Usuario::getNombre)
                .orElse("Vecino");

        Comentario comentario = new Comentario(
                proyectoId,
                correoUsuario,
                nombre,
                texto.trim(),
                LocalDate.now().toString()
        );

        return comentarioDAO.guardarComentario(comentario);
    }

    private ComunidadProyectoDTO mapearProyecto(Proyecto proyecto, String correoUsuario) {
        List<Voto> votos = votoDAO.listarPorProyecto(proyecto.getId());
        Optional<Voto> votoUsuario = votos.stream()
                .filter(voto -> voto.getCorreoUsuario().equalsIgnoreCase(correoUsuario))
                .findFirst();
        Conteos conteos = contar(proyecto, votos);

        return new ComunidadProyectoDTO(
                proyecto.getId(),
                proyecto.getNombre(),
                proyecto.getDescripcion(),
                proyecto.getEstado(),
                proyecto.getFechaInicio(),
                conteos.aFavor(),
                conteos.enContra(),
                votoUsuario.isPresent(),
                votoUsuario.map(Voto::getValor).orElse(null),
                conteos.comentarios()
        );
    }

    private ParticipacionProyectoDTO mapearParticipacion(Proyecto proyecto) {
        Conteos conteos = contar(proyecto, votoDAO.listarPorProyecto(proyecto.getId()));

        return new ParticipacionProyectoDTO(
                proyecto.getId(),
                proyecto.getNombre(),
                proyecto.getDescripcion(),
                proyecto.getEstado(),
                proyecto.getFechaInicio(),
                conteos.aFavor(),
                conteos.enContra(),
                conteos.aFavor() + conteos.enContra(),
                conteos.comentarios()
        );
    }

    private Conteos contar(Proyecto proyecto, List<Voto> votos) {
        int aFavor = (int) votos.stream().filter(voto -> A_FAVOR.equals(voto.getValor())).count();
        int enContra = (int) votos.stream().filter(voto -> EN_CONTRA.equals(voto.getValor())).count();
        int comentarios = comentarioDAO.listarPorProyecto(proyecto.getId()).size();
        return new Conteos(aFavor, enContra, comentarios);
    }

    private record Conteos(int aFavor, int enContra, int comentarios) {}

    private String normalizarVoto(String valor) {
        if (valor == null) {
            throw new ReglaNegocioException("Debes indicar A_FAVOR o EN_CONTRA");
        }

        String normalizado = valor.trim().toUpperCase();

        if (!A_FAVOR.equals(normalizado) && !EN_CONTRA.equals(normalizado)) {
            throw new ReglaNegocioException("El voto debe ser A_FAVOR o EN_CONTRA");
        }

        return normalizado;
    }
}
