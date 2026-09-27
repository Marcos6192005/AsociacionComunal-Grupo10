package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.VotoDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Voto;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.utils.ArchivoDatUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class VotoDAODatImpl implements VotoDAO {

    private static final String VOTOS = "votos.dat";

    @PostConstruct
    public void poblarSiVacio() {
        if (listarVotos().isEmpty()) {
            ArchivoDatUtil.guardarDatos(VOTOS, SemillaComunidad.votos());
        }
    }

    @Override
    public List<Voto> listarVotos() {
        return ArchivoDatUtil.leerDatos(VOTOS);
    }

    @Override
    public List<Voto> listarPorProyecto(String proyectoId) {
        return listarVotos().stream()
                .filter(voto -> voto.getProyectoId().equals(proyectoId))
                .toList();
    }

    @Override
    public Optional<Voto> buscarPorProyectoYCorreo(String proyectoId, String correo) {
        return listarVotos().stream()
                .filter(voto -> voto.getProyectoId().equals(proyectoId)
                        && voto.getCorreoUsuario().equalsIgnoreCase(correo))
                .findFirst();
    }

    @Override
    public Voto guardarVoto(Voto voto) {
        List<Voto> votos = listarVotos();

        Optional<Voto> existente = votos.stream()
                .filter(item -> item.getProyectoId().equals(voto.getProyectoId())
                        && item.getCorreoUsuario().equalsIgnoreCase(voto.getCorreoUsuario()))
                .findFirst();

        if (existente.isPresent()) {
            existente.get().setValor(voto.getValor());
        } else {
            votos.add(voto);
        }

        ArchivoDatUtil.guardarDatos(VOTOS, votos);
        return voto;
    }
}
