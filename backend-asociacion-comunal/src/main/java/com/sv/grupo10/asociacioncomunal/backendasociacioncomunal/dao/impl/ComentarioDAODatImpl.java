package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.ComentarioDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Comentario;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.utils.ArchivoDatUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

@Repository
@Qualifier("dat")
public class ComentarioDAODatImpl implements ComentarioDAO {

    private static final String COMENTARIOS = "comentarios.dat";

    @PostConstruct
    public void poblarSiVacio() {
        if (listarComentarios().isEmpty()) {
            ArchivoDatUtil.guardarDatos(COMENTARIOS, SemillaComunidad.comentarios());
        }
    }

    @Override
    public List<Comentario> listarComentarios() {
        return ArchivoDatUtil.leerDatos(COMENTARIOS);
    }

    @Override
    public List<Comentario> listarPorProyecto(String proyectoId) {
        return listarComentarios().stream()
                .filter(comentario -> comentario.getProyectoId().equals(proyectoId))
                .toList();
    }

    @Override
    public Comentario guardarComentario(Comentario comentario) {
        List<Comentario> comentarios = listarComentarios();
        comentarios.add(comentario);
        ArchivoDatUtil.guardarDatos(COMENTARIOS, comentarios);
        return comentario;
    }
}
