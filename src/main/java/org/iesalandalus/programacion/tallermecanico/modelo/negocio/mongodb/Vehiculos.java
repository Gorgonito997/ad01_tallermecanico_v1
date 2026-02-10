package org.iesalandalus.programacion.tallermecanico.modelo.negocio.mongodb;



import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Vehiculo;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.IVehiculos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Vehiculos implements IVehiculos {

    static final String COLECCION = "vehiculos";
    static final String MATRICULA = "matricula";
    static final String MARCA = "marca";
    static final String MODELO = "modelo";

    private static Vehiculos instancia;

    private MongoDb mongoDb;
    private MongoCollection<Document> coleccionVehiculos;

    private Vehiculos() {
    }

    static Vehiculos getInstancia() {
        if (instancia == null) {
            instancia = new Vehiculos();
        }
        return instancia;
    }

    @Override
    public void comenzar() {
        mongoDb = new MongoDb();
        coleccionVehiculos = mongoDb.getBD().getCollection(COLECCION);
    }

    @Override
    public void terminar() {
        mongoDb.cerrarConexion();
    }

    private Vehiculo getVehiculo(Document doc) {
        if (doc == null) return null;
        return new Vehiculo(
                doc.getString(MARCA),
                doc.getString(MODELO),
                doc.getString(MATRICULA)
        );
    }

    private Document getDocumento(Vehiculo vehiculo) {
        return new Document()
                .append(MATRICULA, vehiculo.matricula())
                .append(MARCA, vehiculo.marca())
                .append(MODELO, vehiculo.modelo());
    }

    @Override
    public List<Vehiculo> get() {
        List<Vehiculo> lista = new ArrayList<>();
        for (Document d : coleccionVehiculos.find().sort(Sorts.ascending(MATRICULA))) {
            lista.add(getVehiculo(d));
        }
        return lista;
    }

    @Override
    public void insertar(Vehiculo vehiculo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(vehiculo, "No se puede insertar un vehículo nulo.");
        if (coleccionVehiculos.find(Filters.eq(MATRICULA, vehiculo.matricula())).first() != null) {
            throw new TallerMecanicoExcepcion("Ya existe un vehículo con esa matrícula.");
        }
        coleccionVehiculos.insertOne(getDocumento(vehiculo));
    }

    @Override
    public Vehiculo buscar(Vehiculo vehiculo) {
        Objects.requireNonNull(vehiculo, "No se puede buscar un vehículo nulo.");
        Document d = coleccionVehiculos.find(Filters.eq(MATRICULA, vehiculo.matricula())).first();
        return getVehiculo(d);
    }

    @Override
    public void borrar(Vehiculo vehiculo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(vehiculo, "No se puede borrar un vehículo nulo.");
        if (buscar(vehiculo) == null) {
            throw new TallerMecanicoExcepcion("No existe ningún vehículo con esa matrícula.");
        }
        coleccionVehiculos.deleteOne(Filters.eq(MATRICULA, vehiculo.matricula()));
    }
}

