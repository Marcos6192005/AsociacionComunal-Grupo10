package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.CuotaDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Cuota;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.utils.ArchivoDatUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CuotaDAODatImpl implements CuotaDAO {

    private static final String ARCHIVO = "cuotas.dat";

    @PostConstruct
    public void poblarSiVacio() {
        if (listar().isEmpty()) {
            ArchivoDatUtil.guardarDatos(ARCHIVO, SemillaComunidad.cuotas());
        }
    }

    @Override
    public Cuota guardar(Cuota cuota) {
        List<Cuota> cuotas = ArchivoDatUtil.leerDatos(ARCHIVO);
        cuotas.add(cuota);
        ArchivoDatUtil.guardarDatos(ARCHIVO, cuotas);
        return cuota;
    }

    @Override
    public List<Cuota> listar() {
        return ArchivoDatUtil.leerDatos(ARCHIVO);
    }

    @Override
    public Optional<Cuota> buscarPorId(String id) {
        return listar().stream()
                .filter(cuota -> cuota.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Cuota> actualizar(String id, Cuota cuota) {
        List<Cuota> cuotas = ArchivoDatUtil.leerDatos(ARCHIVO);

        for (int i = 0; i < cuotas.size(); i++) {
            if (cuotas.get(i).getId().equals(id)) {
                cuota.setId(id);
                cuotas.set(i, cuota);
                ArchivoDatUtil.guardarDatos(ARCHIVO, cuotas);
                return Optional.of(cuota);
            }
        }

        return Optional.empty();
    }
}
