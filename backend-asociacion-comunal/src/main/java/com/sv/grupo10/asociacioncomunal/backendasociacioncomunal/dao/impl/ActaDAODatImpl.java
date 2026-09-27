package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.ActaDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Acta;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.utils.ArchivoDatUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ActaDAODatImpl implements ActaDAO {

    private static final String ARCHIVO = "actas.dat";

    @PostConstruct
    public void poblarSiVacio() {
        if (listar().isEmpty()) {
            ArchivoDatUtil.guardarDatos(ARCHIVO, SemillaComunidad.actas());
        }
    }

    @Override
    public Acta guardar(Acta acta) {
        List<Acta> actas = ArchivoDatUtil.leerDatos(ARCHIVO);
        actas.add(acta);
        ArchivoDatUtil.guardarDatos(ARCHIVO, actas);
        return acta;
    }

    @Override
    public List<Acta> listar() {
        return ArchivoDatUtil.leerDatos(ARCHIVO);
    }

    @Override
    public Optional<Acta> buscarPorId(String id) {
        return listar().stream()
                .filter(acta -> acta.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Acta> actualizar(String id, Acta acta) {
        List<Acta> actas = ArchivoDatUtil.leerDatos(ARCHIVO);

        for (int i = 0; i < actas.size(); i++) {
            if (actas.get(i).getId().equals(id)) {
                acta.setId(id);
                actas.set(i, acta);
                ArchivoDatUtil.guardarDatos(ARCHIVO, actas);
                return Optional.of(acta);
            }
        }

        return Optional.empty();
    }
}
