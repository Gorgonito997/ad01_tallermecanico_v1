package org.iesalandalus.programacion.tallermecanico.modelo.negocio.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Cliente;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.IClientes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Clientes implements IClientes {

    static final String COLECCION = "clientes";
    static final String DNI = "dni";
    static final String NOMBRE = "nombre";
    static final String TELEFONO = "telefono";

    private static Clientes instancia;

    private MongoDb mongoDb;
    private MongoCollection<Document> coleccionClientes;

    private Clientes() {
    }

    static Clientes getInstancia() {
        if (instancia == null) {
            instancia = new Clientes();
        }
        return instancia;
    }

    @Override
    public void comenzar() {
        mongoDb = new MongoDb();
        coleccionClientes = mongoDb.getBD().getCollection(COLECCION);
    }

    @Override
    public void terminar() {
        mongoDb.cerrarConexion();
    }

    private Cliente getCliente(Document doc) {
        if (doc == null) return null;
        return new Cliente(
                doc.getString(NOMBRE),
                doc.getString(DNI),
                doc.getString(TELEFONO)
        );
    }

    private Document getDocumento(Cliente cliente) {
        return new Document()
                .append(DNI, cliente.getDni())
                .append(NOMBRE, cliente.getNombre())
                .append(TELEFONO, cliente.getTelefono());
    }

    @Override
    public List<Cliente> get() {
        List<Cliente> clientes = new ArrayList<>();
        for (Document d : coleccionClientes.find().sort(Sorts.ascending(DNI))) {
            clientes.add(getCliente(d));
        }
        return clientes;
    }

    @Override
    public void insertar(Cliente cliente) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(cliente, "No se puede insertar un cliente nulo.");
        if (coleccionClientes.find(Filters.eq(DNI, cliente.getDni())).first() != null) {
            throw new TallerMecanicoExcepcion("Ya existe un cliente con ese DNI.");
        }
        coleccionClientes.insertOne(getDocumento(cliente));
    }

    @Override
    public Cliente modificar(Cliente cliente, String nombre, String telefono) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(cliente, "No se puede modificar un cliente nulo.");
        Cliente encontrado = buscar(cliente);
        if (encontrado == null) {
            throw new TallerMecanicoExcepcion("No existe ningún cliente con ese DNI.");
        }

        List<Bson> cambios = new ArrayList<>();

        if (nombre != null && !nombre.isBlank()) {
            cambios.add(Updates.set(NOMBRE, nombre));
            encontrado.setNombre(nombre);
        }
        if (telefono != null && !telefono.isBlank()) {
            cambios.add(Updates.set(TELEFONO, telefono));
            encontrado.setTelefono(telefono);
        }

        if (!cambios.isEmpty()) {
            coleccionClientes.updateOne(Filters.eq(DNI, cliente.getDni()), Updates.combine(cambios));
        }

        return encontrado;
    }

    @Override
    public Cliente buscar(Cliente cliente) {
        Objects.requireNonNull(cliente, "No se puede buscar un cliente nulo.");
        return getCliente(
                coleccionClientes.find(Filters.eq(DNI, cliente.getDni())).first()
        );
    }

    @Override
    public void borrar(Cliente cliente) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(cliente, "No se puede borrar un cliente nulo.");
        if (buscar(cliente) == null) {
            throw new TallerMecanicoExcepcion("No existe ningún cliente con ese DNI.");
        }
        coleccionClientes.deleteOne(Filters.eq(DNI, cliente.getDni()));
    }
}


