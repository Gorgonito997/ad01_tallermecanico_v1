package org.iesalandalus.programacion.tallermecanico.modelo.negocio.ficheros.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Cliente;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.IClientes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Clientes implements IClientes {
//Clase clientes
    private static final String FICHERO_CLIENTES = String.format("%s%s%s%s%s%s%s",
            "datos", File.separator, "ficheros", File.separator, "json", File.separator, "clientes.json");

    private final List<Cliente> coleccionClientes;
    private static Clientes instancia;
    private final ObjectMapper mapper;

    private Clientes() {
        coleccionClientes = new ArrayList<>();
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    static Clientes getInstancia() {
        if (instancia == null) {
            instancia = new Clientes();
        }
        return instancia;
    }

    @Override
    public void comenzar() {
        File fichero = new File(FICHERO_CLIENTES);
        if (!fichero.exists()) {
            return;
        }
        try {
            List<Cliente> clientesLeidos = mapper.readValue(fichero, new TypeReference<List<Cliente>>() {});
            coleccionClientes.clear();
            if (clientesLeidos != null) {
                for (int i = 0; i < clientesLeidos.size(); i++) {
                    try {
                        insertar(clientesLeidos.get(i));
                    } catch (TallerMecanicoExcepcion | IllegalArgumentException | NullPointerException e) {
                        System.out.printf("Error al leer el cliente %d. --> %s%n", i, e.getMessage());
                    }
                }
            }
            System.out.printf("Fichero %s leído correctamente.%n", FICHERO_CLIENTES);
        } catch (IOException e) {
            System.out.printf("Error al leer el fichero %s. --> %s%n", FICHERO_CLIENTES, e.getMessage());
        }
    }

    @Override
    public void terminar() {
        File fichero = new File(FICHERO_CLIENTES);
        File directorio = fichero.getParentFile();
        if (directorio != null && !directorio.exists()) {
            directorio.mkdirs();
        }
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(fichero, coleccionClientes);
        } catch (IOException e) {
            System.out.printf("Error al escribir en el fichero %s. --> %s%n", FICHERO_CLIENTES, e.getMessage());
        }
    }

    @Override
    public List<Cliente> get() {
        return new ArrayList<>(coleccionClientes);
    }

    @Override
    public void insertar(Cliente cliente) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(cliente, "No se puede insertar un cliente nulo.");
        if (coleccionClientes.contains(cliente)) {
            throw new TallerMecanicoExcepcion("Ya existe un cliente con ese DNI.");
        }
        coleccionClientes.add(cliente);
    }

    @Override
    public Cliente modificar(Cliente cliente, String nombre, String telefono) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(cliente, "No se puede modificar un cliente nulo.");
        Cliente clienteEncontrado = buscar(cliente);
        if (clienteEncontrado == null) {
            throw new TallerMecanicoExcepcion("No existe ningún cliente con ese DNI.");
        }
        if (nombre != null && !nombre.isBlank()) {
            clienteEncontrado.setNombre(nombre);
        }
        if (telefono != null && !telefono.isBlank()) {
            clienteEncontrado.setTelefono(telefono);
        }
        return clienteEncontrado;
    }

    @Override
    public Cliente buscar(Cliente cliente) {
        Objects.requireNonNull(cliente, "No se puede buscar un cliente nulo.");
        int indice = coleccionClientes.indexOf(cliente);
        return (indice == -1) ? null : coleccionClientes.get(indice);
    }

    @Override
    public void borrar(Cliente cliente) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(cliente, "No se puede borrar un cliente nulo.");
        if (!coleccionClientes.contains(cliente)) {
            throw new TallerMecanicoExcepcion("No existe ningún cliente con ese DNI.");
        }
        coleccionClientes.remove(cliente);
    }
}
