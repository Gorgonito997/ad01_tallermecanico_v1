package org.iesalandalus.programacion.tallermecanico.modelo.negocio.ficheros.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Vehiculo;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.IVehiculos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Vehiculos implements IVehiculos {

    private static final String FICHERO_VEHICULOS = String.format("%s%s%s%s%s%s%s",
            "datos", File.separator, "ficheros", File.separator, "json", File.separator, "vehiculos.json");

    private final List<Vehiculo> coleccionVehiculos;
    private static Vehiculos instancia;
    private final ObjectMapper mapper;

    private Vehiculos() {
        coleccionVehiculos = new ArrayList<>();
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    static Vehiculos getInstancia() {
        if (instancia == null) {
            instancia = new Vehiculos();
        }
        return instancia;
    }

    @Override
    public void comenzar() {
        File fichero = new File(FICHERO_VEHICULOS);
        if (!fichero.exists()) {
            return;
        }
        try {
            List<Vehiculo> vehiculosLeidos = mapper.readValue(fichero, new TypeReference<List<Vehiculo>>() {});
            coleccionVehiculos.clear();
            if (vehiculosLeidos != null) {
                for (int i = 0; i < vehiculosLeidos.size(); i++) {
                    try {
                        insertar(vehiculosLeidos.get(i));
                    } catch (TallerMecanicoExcepcion | IllegalArgumentException | NullPointerException e) {
                        System.out.printf("Error al leer el vehículo %d. --> %s%n", i, e.getMessage());
                    }
                }
            }
            System.out.printf("Fichero %s leído correctamente.%n", FICHERO_VEHICULOS);
        } catch (IOException e) {
            System.out.printf("Error al leer el fichero %s. --> %s%n", FICHERO_VEHICULOS, e.getMessage());
        }
    }

    @Override
    public void terminar() {
        File fichero = new File(FICHERO_VEHICULOS);
        File directorio = fichero.getParentFile();
        if (directorio != null && !directorio.exists()) {
            directorio.mkdirs();
        }
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(fichero, coleccionVehiculos);
        } catch (IOException e) {
            System.out.printf("Error al escribir en el fichero %s. --> %s%n", FICHERO_VEHICULOS, e.getMessage());
        }
    }

    @Override
    public List<Vehiculo> get() {
        return new ArrayList<>(coleccionVehiculos);
    }

    @Override
    public void insertar(Vehiculo vehiculo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(vehiculo, "No se puede insertar un vehículo nulo.");
        if (coleccionVehiculos.contains(vehiculo)) {
            throw new TallerMecanicoExcepcion("Ya existe un vehículo con esa matrícula.");
        }
        coleccionVehiculos.add(vehiculo);
    }

    @Override
    public Vehiculo buscar(Vehiculo vehiculo) {
        Objects.requireNonNull(vehiculo, "No se puede buscar un vehículo nulo.");
        int indice = coleccionVehiculos.indexOf(vehiculo);
        return (indice == -1) ? null : coleccionVehiculos.get(indice);
    }

    @Override
    public void borrar(Vehiculo vehiculo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(vehiculo, "No se puede borrar un vehículo nulo.");
        if (!coleccionVehiculos.contains(vehiculo)) {
            throw new TallerMecanicoExcepcion("No existe ningún vehículo con esa matrícula.");
        }
        coleccionVehiculos.remove(vehiculo);
    }
}

