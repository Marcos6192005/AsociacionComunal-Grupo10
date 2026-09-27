package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.SolicitudDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Solicitud;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.utils.ArchivoDatUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SolicitudDAODatImpl implements SolicitudDAO {

    private static final String ARCHIVO = "solicitudes.dat";

    @PostConstruct
    public void poblarSiVacio() {
        if (listar().isEmpty()) {
            ArchivoDatUtil.guardarDatos(ARCHIVO, SemillaComunidad.solicitudes());
        }
    }

    @Override
    public Solicitud guardar(Solicitud solicitud) {
        List<Solicitud> solicitudes = ArchivoDatUtil.leerDatos(ARCHIVO);
        solicitudes.add(solicitud);
        ArchivoDatUtil.guardarDatos(ARCHIVO, solicitudes);
        return solicitud;
    }

    @Override
    public List<Solicitud> listar() {
        return ArchivoDatUtil.leerDatos(ARCHIVO);
    }

    @Override
    public Optional<Solicitud> buscarPorId(String id) {
        return listar().stream()
                .filter(solicitud -> solicitud.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Solicitud> actualizar(String id, Solicitud solicitud) {
        List<Solicitud> solicitudes = ArchivoDatUtil.leerDatos(ARCHIVO);

        for (int i = 0; i < solicitudes.size(); i++) {
            if (solicitudes.get(i).getId().equals(id)) {
                solicitud.setId(id);
                solicitudes.set(i, solicitud);
                ArchivoDatUtil.guardarDatos(ARCHIVO, solicitudes);
                return Optional.of(solicitud);
            }
        }

        return Optional.empty();
    }
}
