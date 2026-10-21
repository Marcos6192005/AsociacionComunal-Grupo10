package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.ProyectoDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Proyecto;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.utils.ArchivoDatUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProyectoDAODatImpl implements ProyectoDAO {

    private static final String PROYECTOS = "proyectos.dat";

    @PostConstruct
    public void poblarSiVacio() {
        if (listarProyectos().isEmpty()) {
            ArchivoDatUtil.guardarDatos(PROYECTOS, SemillaComunidad.proyectos());
        }
    }

    @Override
    public Proyecto guardarProyecto(Proyecto proyecto) {
        List<Proyecto> proyectos = ArchivoDatUtil.leerDatos(PROYECTOS);

        proyectos.add(proyecto);

        ArchivoDatUtil.guardarDatos(PROYECTOS, proyectos);

        return proyecto;
    }

    @Override
    public List<Proyecto> listarProyectos() {
        return ArchivoDatUtil.leerDatos(PROYECTOS);
    }

    @Override
    public Optional<Proyecto> buscarPorId(String id) {
        List<Proyecto> proyectos = ArchivoDatUtil.leerDatos(PROYECTOS);

        return proyectos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Proyecto> actualizarProyecto(String id, Proyecto proyecto) {
        List<Proyecto> proyectos = ArchivoDatUtil.leerDatos(PROYECTOS);

        for (int i = 0; i < proyectos.size(); i++) {
            if (proyectos.get(i).getId().equals(id)) {
                proyecto.setId(id);
                proyectos.set(i, proyecto);
                ArchivoDatUtil.guardarDatos(PROYECTOS, proyectos);
                return Optional.of(proyecto);
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean eliminarProyecto(String id) {
        List<Proyecto> proyectos = ArchivoDatUtil.leerDatos(PROYECTOS);

        boolean eliminado = proyectos.removeIf(p -> p.getId().equals(id));

        if (eliminado) {
            ArchivoDatUtil.guardarDatos(PROYECTOS, proyectos);
        }

        return eliminado;
    }
}
