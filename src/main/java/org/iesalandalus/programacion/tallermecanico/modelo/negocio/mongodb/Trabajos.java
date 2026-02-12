package org.iesalandalus.programacion.tallermecanico.modelo.negocio.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.*;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.ITrabajos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Trabajos implements ITrabajos {

    static final String COLECCION = "trabajos";

    static final String DNI_CLIENTE = "dniCliente";
    static final String MATRICULA_VEHICULO = "matriculaVehiculo";

    static final String CLIENTE = "cliente";
    static final String VEHICULO = "vehiculo";

    static final String FECHA_INICIO = "fechaInicio";
    static final String FECHA_FIN = "fechaFin";
    static final String TIPO = "tipo";
    static final String HORAS = "horas";
    static final String PRECIO_MATERIAL = "precioMaterial";

    private static final String CLI_DNI = "dni";
    private static final String CLI_NOMBRE = "nombre";
    private static final String CLI_TELEFONO = "telefono";

    private static final String VEH_MATRICULA = "matricula";
    private static final String VEH_MARCA = "marca";
    private static final String VEH_MODELO = "modelo";

    private static Trabajos instancia;

    private MongoCollection<Document> coleccionTrabajos;
    private final MongoDb mongoDb;

    private Trabajos() {
        mongoDb = new MongoDb();
    }

    static Trabajos getInstancia() {
        if (instancia == null) {
            instancia = new Trabajos();
        }
        return instancia;
    }

    @Override
    public void comenzar() {
        coleccionTrabajos = mongoDb.getBD().getCollection(COLECCION);
    }

    @Override
    public void terminar() {
        mongoDb.cerrarConexion();
    }

    private Trabajo getTrabajo(Document documento) {
        if (documento == null) return null;

        Document docCliente = documento.get(CLIENTE, Document.class);
        Document docVehiculo = documento.get(VEHICULO, Document.class);

        String nombre = docCliente.getString(CLI_NOMBRE);
        String dni = docCliente.getString(CLI_DNI);
        String telefono = docCliente.getString(CLI_TELEFONO);

        String marca = docVehiculo.getString(VEH_MARCA);
        String modelo = docVehiculo.getString(VEH_MODELO);
        String matricula = docVehiculo.getString(VEH_MATRICULA);

        if (dni != null) dni = dni.trim().toUpperCase();
        if (matricula != null) matricula = matricula.trim().toUpperCase();

        Cliente cliente = new Cliente(nombre, dni, telefono);
        Vehiculo vehiculo = new Vehiculo(marca, modelo, matricula);

        LocalDate fechaInicio = LocalDate.parse(documento.getString(FECHA_INICIO));
        String fechaFinTexto = documento.getString(FECHA_FIN);
        LocalDate fechaFin = (fechaFinTexto == null) ? null : LocalDate.parse(fechaFinTexto);

        String tipo = documento.getString(TIPO);
        int horas = documento.getInteger(HORAS, 0);

        Trabajo trabajo;
        if ("Mecanico".equals(tipo)) {
            Mecanico mecanico = new Mecanico(cliente, vehiculo, fechaInicio);
            Double pm = documento.getDouble(PRECIO_MATERIAL);
            float precioMaterial = (pm == null) ? 0f : pm.floatValue();
            if (precioMaterial > 0) {
                try {
                    mecanico.anadirPrecioMaterial(precioMaterial);
                } catch (TallerMecanicoExcepcion ignored) {
                }
            }
            trabajo = mecanico;
        } else {
            trabajo = new Revision(cliente, vehiculo, fechaInicio);
        }

        if (horas > 0) {
            try {
                trabajo.anadirHoras(horas);
            } catch (TallerMecanicoExcepcion ignored) {
            }
        }

        if (fechaFin != null) {
            try {
                trabajo.cerrar(fechaFin);
            } catch (TallerMecanicoExcepcion ignored) {
            }
        }

        return trabajo;
    }


    private Document getDocumento(Trabajo trabajo) {
        Document docCliente = new Document()
                .append(CLI_DNI, trabajo.getCliente().getDni())
                .append(CLI_NOMBRE, trabajo.getCliente().getNombre())
                .append(CLI_TELEFONO, trabajo.getCliente().getTelefono());

        Document docVehiculo = new Document()
                .append(VEH_MATRICULA, trabajo.getVehiculo().matricula())
                .append(VEH_MARCA, trabajo.getVehiculo().marca())
                .append(VEH_MODELO, trabajo.getVehiculo().modelo());

        Document doc = new Document()
                .append(DNI_CLIENTE, trabajo.getCliente().getDni())
                .append(MATRICULA_VEHICULO, trabajo.getVehiculo().matricula())
                .append(CLIENTE, docCliente)
                .append(VEHICULO, docVehiculo)
                .append(FECHA_INICIO, trabajo.getFechaInicio().toString())
                .append(FECHA_FIN, trabajo.getFechaFin() == null ? null : trabajo.getFechaFin().toString())
                .append(TIPO, (trabajo instanceof Mecanico) ? "Mecanico" : "Revision")
                .append(HORAS, trabajo.getHoras());

        if (trabajo instanceof Mecanico mecanico) {
            doc.append(PRECIO_MATERIAL, (double) mecanico.getPrecioMaterial());
        } else {
            doc.append(PRECIO_MATERIAL, 0.0);
        }

        return doc;
    }

    @Override
    public List<Trabajo> get() {
        List<Trabajo> lista = new ArrayList<>();
        for (Document d : coleccionTrabajos.find()) {
            lista.add(getTrabajo(d));
        }
        return lista;
    }

    @Override
    public List<Trabajo> get(Cliente cliente) {
        Objects.requireNonNull(cliente, "No se puede obtener trabajos de un cliente nulo.");
        List<Trabajo> lista = new ArrayList<>();
        for (Document d : coleccionTrabajos.find(Filters.eq(DNI_CLIENTE, cliente.getDni()))) {
            lista.add(getTrabajo(d));
        }
        return lista;
    }

    @Override
    public List<Trabajo> get(Vehiculo vehiculo) {
        Objects.requireNonNull(vehiculo, "No se puede obtener trabajos de un vehículo nulo.");
        List<Trabajo> lista = new ArrayList<>();
        for (Document d : coleccionTrabajos.find(Filters.eq(MATRICULA_VEHICULO, vehiculo.matricula()))) {
            lista.add(getTrabajo(d));
        }
        return lista;
    }

    @Override
    public Map<TipoTrabajo, Integer> getEstadisticasMensuales(LocalDate mes) {
        return Map.of();
    }

    @Override
    public void insertar(Trabajo trabajo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No se puede insertar un trabajo nulo.");
        if (buscar(trabajo) != null) {
            throw new TallerMecanicoExcepcion("Ya existe un trabajo con esos datos.");
        }
        coleccionTrabajos.insertOne(getDocumento(trabajo));
    }

    @Override
    public Trabajo anadirHoras(Trabajo trabajo, int horas) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No se puede añadir horas a un trabajo nulo.");
        Trabajo encontrado = buscar(trabajo);
        if (encontrado == null) {
            throw new TallerMecanicoExcepcion("No existe el trabajo indicado.");
        }
        encontrado.anadirHoras(horas);
        borrar(trabajo);
        insertar(encontrado);
        return encontrado;
    }

    @Override
    public Trabajo anadirPrecioMaterial(Trabajo trabajo, float precioMaterial) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No se puede añadir precio material a un trabajo nulo.");
        if (!(trabajo instanceof Mecanico)) {
            throw new TallerMecanicoExcepcion("Solo se puede añadir precio material a trabajos mecánicos.");
        }
        Trabajo encontrado = buscar(trabajo);
        if (encontrado == null) {
            throw new TallerMecanicoExcepcion("No existe el trabajo indicado.");
        }
        ((Mecanico) encontrado).anadirPrecioMaterial(precioMaterial);
        borrar(trabajo);
        insertar(encontrado);
        return encontrado;
    }

    @Override
    public Trabajo cerrar(Trabajo trabajo, LocalDate fechaFin) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No se puede cerrar un trabajo nulo.");
        Trabajo encontrado = buscar(trabajo);
        if (encontrado == null) {
            throw new TallerMecanicoExcepcion("No existe el trabajo indicado.");
        }
        encontrado.cerrar(fechaFin);
        borrar(trabajo);
        insertar(encontrado);
        return encontrado;
    }

    @Override
    public Trabajo buscar(Trabajo trabajo) {
        Objects.requireNonNull(trabajo, "No se puede buscar un trabajo nulo.");
        Document d = coleccionTrabajos.find(
                Filters.and(
                        Filters.eq(DNI_CLIENTE, trabajo.getCliente().getDni()),
                        Filters.eq(MATRICULA_VEHICULO, trabajo.getVehiculo().matricula()),
                        Filters.eq(FECHA_INICIO, trabajo.getFechaInicio().toString())
                )
        ).first();
        return getTrabajo(d);
    }

    @Override
    public void borrar(Trabajo trabajo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No se puede borrar un trabajo nulo.");
        if (buscar(trabajo) == null) {
            throw new TallerMecanicoExcepcion("No existe el trabajo indicado.");
        }
        coleccionTrabajos.deleteOne(
                Filters.and(
                        Filters.eq(DNI_CLIENTE, trabajo.getCliente().getDni()),
                        Filters.eq(MATRICULA_VEHICULO, trabajo.getVehiculo().matricula()),
                        Filters.eq(FECHA_INICIO, trabajo.getFechaInicio().toString())
                )
        );
    }
}

