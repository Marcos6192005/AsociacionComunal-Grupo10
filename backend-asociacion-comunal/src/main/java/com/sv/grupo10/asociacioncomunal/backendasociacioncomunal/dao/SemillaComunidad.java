package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.dao;

import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Acta;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.AcuerdoActa;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Comentario;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Comunicado;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Cuota;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.MiembroDirectiva;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Movimiento;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Proyecto;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.RegistroEmail;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Solicitud;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Usuario;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Vecino;
import com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.models.entities.Voto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Demo data for ADESCO Residencial Los Maquilishuats, San Salvador.
 * Fixed ids keep projects, votes, comments, cuotas and ledger rows linked
 * even though each .dat file is written by its own DAO.
 */
public final class SemillaComunidad {

    public static final String PROY_ALUMBRADO = "proy-alumbrado";
    public static final String PROY_CANCHA = "proy-cancha";
    public static final String PROY_CALLE = "proy-calle";
    public static final String PROY_AGUA = "proy-agua";
    public static final String PROY_PARQUE = "proy-parque";
    public static final String PROY_DRENAJE = "proy-drenaje";

    public static final String PRESIDENTE_CORREO = "presidente@adesco.com";
    public static final String PRESIDENTE_NOMBRE = "Marta Alicia Quintanilla";
    public static final String SECRETARIO_CORREO = "secretario@adesco.com";
    public static final String SECRETARIO_NOMBRE = "Jorge Alberto Menjivar";
    public static final String TESORERO_CORREO = "tesorero@adesco.com";
    public static final String TESORERO_NOMBRE = "Silvia Esperanza Rivas";

    private static final double CUOTA = 8.00;
    private static final String CLAVE_VECINO = "vecino123";

    private static final VecinoBase[] VECINOS = {
            new VecinoBase("Rosa Elena Martinez", "vecino@adesco.com", "Casa 12, Pasaje Los Pinos"),
            new VecinoBase("Carlos Hernandez", "carlos.hernandez@adesco.com", "Casa 8, Pasaje Los Pinos"),
            new VecinoBase("Ana Guadalupe Guzman", "ana.guzman@adesco.com", "Casa 15, Pasaje Los Pinos"),
            new VecinoBase("Jose Alfredo Rivera", "jose.rivera@adesco.com", "Casa 3, Pasaje El Roble"),
            new VecinoBase("Maria de los Angeles Lopez", "maria.lopez@adesco.com", "Casa 7, Pasaje El Roble"),
            new VecinoBase("Luis Ernesto Paredes", "luis.paredes@adesco.com", "Casa 11, Pasaje El Roble"),
            new VecinoBase("Carmen Vasquez", "carmen.vasquez@adesco.com", "Casa 2, Calle Principal"),
            new VecinoBase("Pedro Antonio Mejia", "pedro.mejia@adesco.com", "Casa 6, Calle Principal"),
            new VecinoBase("Sofia Ramirez", "sofia.ramirez@adesco.com", "Casa 9, Calle Principal"),
            new VecinoBase("Diego Armando Cruz", "diego.cruz@adesco.com", "Casa 14, Calle Principal"),
            new VecinoBase("Gloria Esperanza Flores", "gloria.flores@adesco.com", "Casa 1, Senda Las Acacias"),
            new VecinoBase("Oscar Bonilla", "oscar.bonilla@adesco.com", "Casa 4, Senda Las Acacias"),
            new VecinoBase("Lucia Molina", "lucia.molina@adesco.com", "Casa 10, Senda Las Acacias"),
            new VecinoBase("Fernando Diaz", "fernando.diaz@yopmail.com", "Casa 18, Pasaje Los Pinos"),
            new VecinoBase("Patricia Ortiz", "patricia.ortiz@adesco.com", "Casa 21, Pasaje El Roble"),
            new VecinoBase("Rafael Sanchez", "rafael.sanchez@adesco.com", "Casa 5, Calle Principal"),
            new VecinoBase("Elena Castro", "elena.castro@adesco.com", "Casa 16, Senda Las Acacias"),
            new VecinoBase("Miguel Angel Ayala", "miguel.ayala@adesco.com", "Casa 19, Pasaje Los Pinos"),
    };

    private SemillaComunidad() {
    }

    public static List<Usuario> usuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(new MiembroDirectiva(PRESIDENTE_NOMBRE, PRESIDENTE_CORREO, "presi123", "Presidente"));
        usuarios.add(new MiembroDirectiva(SECRETARIO_NOMBRE, SECRETARIO_CORREO, "secre123", "Secretario"));
        usuarios.add(new MiembroDirectiva(TESORERO_NOMBRE, TESORERO_CORREO, "teso123", "Tesorero"));
        usuarios.add(new MiembroDirectiva("Admin ADESCO", "admin@adesco.com", "admin123", "Presidente"));
        for (VecinoBase vecino : VECINOS) {
            usuarios.add(new Vecino(vecino.nombre, vecino.correo, CLAVE_VECINO, vecino.casa));
        }
        return usuarios;
    }

    public static List<Proyecto> proyectos() {
        return new ArrayList<>(List.of(
                proyecto(PROY_ALUMBRADO, "Alumbrado del Pasaje Los Pinos",
                        "Cambio de luminarias fundidas y cableado del pasaje, desde la casa 8 hasta la 18.",
                        "EN_EJECUCION", "2026-01-15"),
                proyecto(PROY_AGUA, "Tanque y red de agua",
                        "Limpieza del tanque comunal y reparacion de la fuga en la red que abastece el Pasaje El Roble.",
                        "EN_EJECUCION", "2025-11-20"),
                proyecto(PROY_CALLE, "Bacheo de la calle principal",
                        "Relleno de baches entre el porton y la Senda Las Acacias. Obra cerrada en agosto.",
                        "FINALIZADO", "2025-08-10"),
                proyecto(PROY_CANCHA, "Techado de la cancha",
                        "Estructura y lamina para la cancha comunal, pendiente de cotizacion y voto de la asamblea.",
                        "PLANIFICADO", "2026-03-01"),
                proyecto(PROY_PARQUE, "Juegos del parque infantil",
                        "Reparacion de columpios y pintura del parque junto a la casa comunal.",
                        "PLANIFICADO", "2026-04-02"),
                proyecto(PROY_DRENAJE, "Canaletas de aguas lluvias",
                        "Canaleta en la calle principal para la epoca de invierno, antes de octubre.",
                        "PLANIFICADO", "2026-05-12")
        ));
    }

    public static List<Comentario> comentarios() {
        return new ArrayList<>(List.of(
                comentario(PROY_ALUMBRADO, 0, "La luminaria frente a mi casa sigue apagada desde el martes.", "2026-08-18"),
                comentario(PROY_ALUMBRADO, 1, "Ya pasaron a medir el poste de la casa 8. Ojala terminen esta semana.", "2026-08-22"),
                comentario(PROY_ALUMBRADO, 13, "En la casa 18 el cable quedo colgando. Hay que revisarlo antes de que llueva.", "2026-09-03"),
                comentario(PROY_AGUA, 2, "El chorro de la casa 15 sale con aire por las noches.", "2026-07-28"),
                comentario(PROY_AGUA, 3, "En El Roble el agua llego turbia despues de la limpieza del tanque.", "2026-09-08"),
                comentario(PROY_AGUA, 4, "A mi si me mejoro la presion desde que cambiaron el tramo de tuberia.", "2026-09-11"),
                comentario(PROY_CALLE, 6, "El bache del porton quedo bien. Ya no se encharca tanto.", "2026-08-16"),
                comentario(PROY_CALLE, 7, "Frente a la casa 6 todavia hay un hueco chiquito del lado de la acera.", "2026-08-19"),
                comentario(PROY_CANCHA, 5, "Si van a techar, que dejen altura para jugar basketball los sabados.", "2026-09-02"),
                comentario(PROY_CANCHA, 8, "Apoyo el techado, pero que no suban la cuota de un solo.", "2026-09-04"),
                comentario(PROY_PARQUE, 10, "Los columpios del parque estan oxidados. Mis hijos ya no los usan.", "2026-07-14"),
                comentario(PROY_PARQUE, 11, "Conviene pintar tambien las bancas, no solo los juegos.", "2026-08-30"),
                comentario(PROY_DRENAJE, 15, "En invierno el agua de la calle principal se mete a la casa 5.", "2026-09-15"),
                comentario(PROY_DRENAJE, 16, "La canaleta tiene que llegar hasta Las Acacias, no solo hasta el porton.", "2026-09-18")
        ));
    }

    public static List<Voto> votos() {
        List<Voto> votos = new ArrayList<>();
        String[] abiertos = {PROY_CANCHA, PROY_PARQUE, PROY_DRENAJE};
        for (String proyectoId : abiertos) {
            for (int i = 0; i < VECINOS.length; i++) {
                if (i == 9 || i == 14 || i == 17) {
                    continue;
                }
                String valor = (i == 5 || i == 8 || i == 12) ? "EN_CONTRA" : "A_FAVOR";
                votos.add(new Voto(proyectoId, VECINOS[i].correo, valor));
            }
        }
        return votos;
    }

    public static List<Solicitud> solicitudes() {
        return new ArrayList<>(List.of(
                solicitud(0, "Luminaria fundida en casa 12",
                        "La lampara frente a la casa 12 del Pasaje Los Pinos no enciende desde el 12 de agosto.",
                        "RESUELTA", "2026-08-14", "Se cambio la luminaria el 20 de agosto.", PROY_ALUMBRADO),
                solicitud(1, "Bache frente al porton",
                        "Quedo un bache despues de las lluvias de julio, justo antes de entrar al residencial.",
                        "EN_REVISION", "2026-08-21", null, PROY_CALLE),
                solicitud(2, "Fuga de agua en Pasaje Los Pinos",
                        "Hay humedad en la acera de la casa 15. Parece fuga de la red comunal.",
                        "RESUELTA", "2026-07-09", "Se reparo el tramo y se recupero la presion.", PROY_AGUA),
                solicitud(3, "Perros sueltos por la noche",
                        "Varios perros andan sueltos en El Roble despues de las 8 pm y asustan a los ninos.",
                        "RECIBIDA", "2026-09-17", null, null),
                solicitud(4, "Copia del acta de julio",
                        "Necesito copia del acta de la asamblea del 12 de julio para un tramite.",
                        "RESUELTA", "2026-09-05", "La secretaria entrego copia en la casa comunal.", null),
                solicitud(5, "Ruido en la cancha",
                        "Pido que la cancha cierre a las 7 pm entre semana por el ruido.",
                        "RECHAZADA", "2026-08-02", "La asamblea ya acordo cierre a las 8 pm. Se mantiene ese horario.", PROY_CANCHA),
                solicitud(6, "Arbol tapa el alumbrado",
                        "El arbol de la casa 2 tapa dos lamparas de la calle principal.",
                        "EN_REVISION", "2026-09-12", null, PROY_ALUMBRADO),
                solicitud(10, "Rampa en el parque infantil",
                        "En el parque no hay rampa. Una vecina mayor no puede entrar con su andador.",
                        "RECIBIDA", "2026-09-22", null, PROY_PARQUE)
        ));
    }

    public static List<Acta> actas() {
        return new ArrayList<>(List.of(
                acta("Asamblea ordinaria de junio",
                        "Se presento el balance de mayo, se aprobo la compra de luminarias y se recordo el horario de la cancha.",
                        "2026-06-14", "PUBLICADA",
                        new AcuerdoActa("Comprar luminarias LED para el Pasaje Los Pinos.", PROY_ALUMBRADO),
                        new AcuerdoActa("Mantener el cierre de la cancha a las 8 pm.", null)),
                acta("Asamblea ordinaria de julio",
                        "Se informo la reparacion de la fuga de agua y se recibieron quejas por baches en la calle principal.",
                        "2026-07-12", "PUBLICADA",
                        new AcuerdoActa("Priorizar el tramo de tuberia del Pasaje Los Pinos.", PROY_AGUA),
                        new AcuerdoActa("Pedir cotizacion de bacheo antes de agosto.", PROY_CALLE)),
                acta("Asamblea ordinaria de agosto",
                        "Se cerro el bacheo de la calle principal y se abrio la votacion del techado de la cancha.",
                        "2026-08-09", "PUBLICADA",
                        new AcuerdoActa("Dar por finalizado el bacheo de la calle principal.", PROY_CALLE),
                        new AcuerdoActa("Someter el techado de la cancha al voto de los vecinos.", PROY_CANCHA)),
                acta("Asamblea ordinaria de septiembre",
                        "Borrador. Falta pasar lista y el acuerdo de canaletas antes de publicar.",
                        "2026-09-20", "BORRADOR",
                        new AcuerdoActa("Revisar avance de canaletas antes de las lluvias de octubre.", PROY_DRENAJE))
        ));
    }

    public static List<Comunicado> comunicados() {
        return new ArrayList<>(List.of(
                comunicado("Asamblea ordinaria de agosto",
                        "Vecinos de Residencial Los Maquilishuats: la asamblea es el domingo 9 de agosto, 9:00 am, en la casa comunal. Lleven su cuota al dia si pueden.",
                        "2026-08-02", PRESIDENTE_CORREO, PRESIDENTE_NOMBRE),
                comunicado("Corte de agua por limpieza del tanque",
                        "El miercoles 6 de agosto no habra agua de 8:00 am a 2:00 pm. La cuadrilla limpia el tanque comunal. Junten agua el martes.",
                        "2026-08-04", SECRETARIO_CORREO, SECRETARIO_NOMBRE),
                comunicado("Cuota de septiembre",
                        "La cuota de septiembre sigue en $8.00 por casa. Pueden pasar a dejarla con la tesorera, Silvia Rivas, en la casa comunal, de lunes a viernes de 5:00 a 7:00 pm.",
                        "2026-09-01", TESORERO_CORREO, TESORERO_NOMBRE),
                comunicado("Jornada de limpieza",
                        "Sabado 13 de septiembre, 7:30 am, limpieza de la calle principal y la Senda Las Acacias. La junta lleva bolsas y rastrillos.",
                        "2026-09-08", SECRETARIO_CORREO, SECRETARIO_NOMBRE),
                comunicado("Avance del alumbrado",
                        "Ya se cambiaron 11 luminarias del Pasaje Los Pinos. Faltan la casa 12 y la 18. Si su lampara sigue apagada, dejen la solicitud en secretaria.",
                        "2026-09-10", PRESIDENTE_CORREO, PRESIDENTE_NOMBRE),
                comunicado("Basura en la calle principal",
                        "El tren de aseo no se lleva bolsas dejadas fuera del horario. Sacar la basura martes y viernes antes de las 7:00 am, no la noche anterior.",
                        "2026-09-19", SECRETARIO_CORREO, SECRETARIO_NOMBRE)
        ));
    }

    public static List<Cuota> cuotas() {
        List<Cuota> cuotas = new ArrayList<>();
        String[] periodos = {"2026-04", "2026-05", "2026-06", "2026-07", "2026-08", "2026-09"};
        for (int i = 0; i < VECINOS.length; i++) {
            for (String periodo : periodos) {
                boolean pagada = estaPagada(i, periodo);
                Cuota cuota = new Cuota(
                        VECINOS[i].correo,
                        VECINOS[i].nombre,
                        VECINOS[i].casa,
                        periodo,
                        CUOTA,
                        pagada ? "PAGADA" : "PENDIENTE"
                );
                cuota.setId(idCuota(i, periodo));
                if (pagada) {
                    cuota.setFechaPago(periodo + "-12");
                }
                cuotas.add(cuota);
            }
        }
        return cuotas;
    }

    public static List<Movimiento> movimientos() {
        List<Movimiento> movimientos = new ArrayList<>();
        for (Cuota cuota : cuotas()) {
            if (!"PAGADA".equals(cuota.getEstado())) {
                continue;
            }
            Movimiento ingreso = new Movimiento(
                    "INGRESO",
                    "Cuota " + cuota.getPeriodo() + " - " + cuota.getNombreVecino(),
                    cuota.getMonto(),
                    cuota.getFechaPago(),
                    null,
                    TESORERO_CORREO,
                    TESORERO_NOMBRE
            );
            ingreso.setCuotaId(cuota.getId());
            movimientos.add(ingreso);
        }
        movimientos.add(egreso("Compra de luminarias LED, Pasaje Los Pinos", 186.50, "2026-07-20", PROY_ALUMBRADO));
        movimientos.add(egreso("Material de bacheo, calle principal", 140.00, "2026-08-03", PROY_CALLE));
        movimientos.add(egreso("Limpieza del tanque comunal", 60.00, "2026-08-06", PROY_AGUA));
        movimientos.add(egreso("Energia electrica de areas comunes, agosto", 40.00, "2026-08-28", null));
        movimientos.add(egreso("Bolsas y rastrillos para jornada de limpieza", 22.00, "2026-09-12", null));
        movimientos.add(egreso("Papeleria y refrigerio de asamblea", 28.00, "2026-09-02", null));
        movimientos.add(egreso("Pintura para juegos del parque", 45.00, "2026-09-14", PROY_PARQUE));
        movimientos.add(egreso("Reparacion del porton del residencial", 85.00, "2026-09-08", null));
        return movimientos;
    }

    public static List<RegistroEmail> correos() {
        List<RegistroEmail> correos = new ArrayList<>();
        correos.add(enviado(VECINOS[0].correo, "Bienvenido/a a la Asociacion Comunal"));
        correos.add(enviado(PRESIDENTE_CORREO, "Nueva solicitud de la comunidad"));
        correos.add(enviado(SECRETARIO_CORREO, "Nueva solicitud de la comunidad"));
        correos.add(enviado(VECINOS[0].correo, "Tu solicitud fue respondida"));
        correos.add(enviado(VECINOS[2].correo, "Tu solicitud fue respondida"));
        correos.add(enviado(VECINOS[4].correo, "Comunicado de la asociacion: Cuota de septiembre"));
        correos.add(enviado(VECINOS[6].correo, "Comunicado de la asociacion: Jornada de limpieza"));
        correos.add(enviado(VECINOS[10].correo, "Cuota pendiente 2026-09"));
        correos.add(enviado(VECINOS[1].correo, "Pago de cuota registrado 2026-08"));
        correos.add(enviado(VECINOS[7].correo, "Pago de cuota registrado 2026-07"));
        return correos;
    }

    private static Proyecto proyecto(String id, String nombre, String descripcion, String estado, String fecha) {
        Proyecto proyecto = new Proyecto(nombre, descripcion, estado, fecha);
        proyecto.setId(id);
        return proyecto;
    }

    private static Comentario comentario(String proyectoId, int vecino, String texto, String fecha) {
        VecinoBase autor = VECINOS[vecino];
        return new Comentario(proyectoId, autor.correo, autor.nombre, texto, fecha);
    }

    private static Solicitud solicitud(
            int vecino,
            String titulo,
            String descripcion,
            String estado,
            String fecha,
            String respuesta,
            String proyectoId
    ) {
        VecinoBase autor = VECINOS[vecino];
        Solicitud solicitud = new Solicitud(titulo, descripcion, autor.correo, autor.nombre, estado, fecha);
        solicitud.setRespuesta(respuesta);
        solicitud.setProyectoId(proyectoId);
        return solicitud;
    }

    private static Acta acta(String titulo, String contenido, String fecha, String estado, AcuerdoActa... acuerdos) {
        return new Acta(
                titulo,
                contenido,
                fecha,
                estado,
                SECRETARIO_CORREO,
                SECRETARIO_NOMBRE,
                new ArrayList<>(Arrays.asList(acuerdos))
        );
    }

    private static Comunicado comunicado(String titulo, String contenido, String fecha, String correo, String nombre) {
        return new Comunicado(titulo, contenido, fecha, correo, nombre);
    }

    private static Movimiento egreso(String concepto, double monto, String fecha, String proyectoId) {
        return new Movimiento("EGRESO", concepto, monto, fecha, proyectoId, TESORERO_CORREO, TESORERO_NOMBRE);
    }

    private static RegistroEmail enviado(String destinatario, String asunto) {
        RegistroEmail registro = new RegistroEmail(destinatario, asunto);
        registro.registrarIntento("email-sender-1");
        registro.marcarEnviado();
        return registro;
    }

    private static boolean estaPagada(int indice, String periodo) {
        return switch (periodo) {
            case "2026-04", "2026-05", "2026-06" -> true;
            case "2026-07" -> indice < 16;
            case "2026-08" -> indice < 12;
            case "2026-09" -> indice < 6;
            default -> false;
        };
    }

    private static String idCuota(int indice, String periodo) {
        return "cuota-" + indice + "-" + periodo;
    }

    private record VecinoBase(String nombre, String correo, String casa) {
    }
}
