package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ArchivoDatUtil {

    public static <T> void guardarDatos(String rutaArchivo, List<T> lista) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(rutaArchivo))) {
            oos.writeObject(lista);
        } catch (IOException e) {
            System.err.println("Error al guardar en el archivo " + rutaArchivo + ": " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> leerDatos(String rutaArchivo) {
        List<T> lista = new ArrayList<>();
        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) {
            return lista;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(rutaArchivo))) {
            List<T> leida = (List<T>) ois.readObject();
            if (leida != null) {
                lista = new ArrayList<>(leida);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al leer el archivo " + rutaArchivo + ": " + e.getMessage());
        }

        return lista;
    }
}
