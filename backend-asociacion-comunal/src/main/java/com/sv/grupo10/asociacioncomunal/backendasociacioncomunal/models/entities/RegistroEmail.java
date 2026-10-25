package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registro (bitácora) de cada correo que pasa por el sistema.
 * Se persiste en archivo .dat. Sus métodos que cambian estado son
 * synchronized porque lo modifican los hilos de envío.
 */
public class RegistroEmail implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String destinatario;
    private final String asunto;
    private final LocalDateTime fechaCreacion;

    private EstadoEnvio estado;
    private int intentos;
    private LocalDateTime fechaEnvio;
    private String ultimoError;
    private String hiloProcesador;

    public RegistroEmail(String destinatario, String asunto) {
        this.id = UUID.randomUUID().toString();
        this.destinatario = destinatario;
        this.asunto = asunto;
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoEnvio.PENDIENTE;
    }

    /** Rebuilds a row read from the database. The creation constructor always assigns a new id. */
    public RegistroEmail(
            String id,
            String destinatario,
            String asunto,
            LocalDateTime fechaCreacion,
            EstadoEnvio estado,
            int intentos,
            LocalDateTime fechaEnvio,
            String ultimoError,
            String hiloProcesador
    ) {
        this.id = id;
        this.destinatario = destinatario;
        this.asunto = asunto;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
        this.intentos = intentos;
        this.fechaEnvio = fechaEnvio;
        this.ultimoError = ultimoError;
        this.hiloProcesador = hiloProcesador;
    }

    public synchronized void registrarIntento(String nombreHilo) {
        this.intentos++;
        this.hiloProcesador = nombreHilo;
    }

    public synchronized void registrarError(String error) {
        this.ultimoError = error;
    }

    public synchronized void marcarEnviado() {
        this.estado = EstadoEnvio.ENVIADO;
        this.fechaEnvio = LocalDateTime.now();
        this.ultimoError = null;
    }

    public synchronized void marcarFallido(String error) {
        this.estado = EstadoEnvio.FALLIDO;
        this.ultimoError = error;
    }

    /** Garantiza una "foto" consistente del objeto al escribirlo al .dat. */
    @Serial
    private synchronized void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    public String getId()                          { return id; }
    public String getDestinatario()                { return destinatario; }
    public String getAsunto()                      { return asunto; }
    public LocalDateTime getFechaCreacion()        { return fechaCreacion; }
    public synchronized EstadoEnvio getEstado()    { return estado; }
    public synchronized int getIntentos()          { return intentos; }
    public synchronized LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public synchronized String getUltimoError()    { return ultimoError; }
    public synchronized String getHiloProcesador() { return hiloProcesador; }
}
