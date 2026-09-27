package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.ComunicadoDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Comunicado;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.utils.ArchivoDatUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ComunicadoDAODatImpl implements ComunicadoDAO {

    private static final String ARCHIVO = "comunicados.dat";

    @PostConstruct
    public void poblarSiVacio() {
        if (listar().isEmpty()) {
            ArchivoDatUtil.guardarDatos(ARCHIVO, SemillaComunidad.comunicados());
        }
    }

    @Override
    public Comunicado guardar(Comunicado comunicado) {
        List<Comunicado> comunicados = ArchivoDatUtil.leerDatos(ARCHIVO);
        comunicados.add(comunicado);
        ArchivoDatUtil.guardarDatos(ARCHIVO, comunicados);
        return comunicado;
    }

    @Override
    public List<Comunicado> listar() {
        return ArchivoDatUtil.leerDatos(ARCHIVO);
    }

    @Override
    public Optional<Comunicado> buscarPorId(String id) {
        return listar().stream()
                .filter(comunicado -> comunicado.getId().equals(id))
                .findFirst();
    }
}
