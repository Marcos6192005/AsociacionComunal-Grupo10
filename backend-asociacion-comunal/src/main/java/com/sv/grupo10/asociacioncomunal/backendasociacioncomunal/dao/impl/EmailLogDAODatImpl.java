package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.impl;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.SemillaComunidad;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao.interfaces.EmailLogDAO;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.RegistroEmail;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación de EmailLogDAO que guarda la bitácora de correos en emails.dat.
 *
 * Colección: ConcurrentHashMap, porque varios hilos de envío actualizan
 * registros al mismo tiempo y se necesita búsqueda por id en O(1)
 * sin bloquear todo el mapa.
 */
@Repository
public class EmailLogDAODatImpl implements EmailLogDAO {

    private static final Logger log = LoggerFactory.getLogger(EmailLogDAODatImpl.class);

    private final Path archivo;
    private final Map<String, RegistroEmail> registros = new ConcurrentHashMap<>();

    public EmailLogDAODatImpl(@Value("${app.mail.log-path:emails.dat}") String ruta) {
        this.archivo = Path.of(ruta);
    }

    @PostConstruct
    @SuppressWarnings("unchecked")
    void cargar() {
        if (!Files.exists(archivo)) {
            return;
        }
        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(archivo)))) {
            List<RegistroEmail> lista = (List<RegistroEmail>) in.readObject();
            lista.forEach(r -> registros.put(r.getId(), r));
            log.info("Bitácora de correos cargada: {} registros", registros.size());
        } catch (IOException | ClassNotFoundException e) {
            log.warn("No se pudo leer {}: {}", archivo, e.getMessage());
        }
        if (registros.isEmpty()) {
            SemillaComunidad.correos().forEach(registro -> registros.put(registro.getId(), registro));
            persistir();
            log.info("Bitacora de correos sembrada: {} registros", registros.size());
        }
    }

    @Override
    public void guardar(RegistroEmail registro) {
        registros.put(registro.getId(), registro);
        persistir();
    }

    @Override
    public void actualizar(RegistroEmail registro) {
        guardar(registro);
    }

    @Override
    public Optional<RegistroEmail> buscarPorId(String id) {
        return Optional.ofNullable(registros.get(id));
    }

    @Override
    public List<RegistroEmail> listarTodos() {
        List<RegistroEmail> lista = new ArrayList<>(registros.values());
        lista.sort(Comparator.comparing(RegistroEmail::getFechaCreacion).reversed());
        return lista;
    }

    /**
     * synchronized: solo un hilo escribe el archivo a la vez.
     * Se escribe a un temporal y luego se reemplaza, para no dejar
     * el .dat corrupto si algo falla a la mitad.
     */
    private synchronized void persistir() {
        try {
            Path carpeta = archivo.toAbsolutePath().getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }
            Path temporal = archivo.resolveSibling(archivo.getFileName() + ".tmp");
            try (ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(temporal)))) {
                out.writeObject(new ArrayList<>(registros.values()));
            }
            Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Error guardando bitácora de correos en {}", archivo, e);
        }
    }
}
