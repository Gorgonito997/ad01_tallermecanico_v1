package org.iesalandalus.programacion.tallermecanico.modelo.negocio.mongodb;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.*;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.ITrabajos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Trabajos implements ITrabajos {

    private static final String COLECCION = "trabajos";

    private static final String CLIENTE = "cliente";
    private static final String VEHICULO = "vehiculo";

    private static final String DNI_CLIENTE = "dniCliente";
    private static final String MATRICULA_VEHICULO = "matriculaVehiculo";

    private static final String FECHA_INICIO = "fechaInicio";
    private static final String FECHA_FIN = "fechaFin";
    private static final String TIPO = "tipo";
    private static final String REVISION = "revision";
    private static final String MECANICO = "mecanico";
    private static final String HORAS = "horas";
    private static final String PRECIO_MATERIAL = "precioMaterial";

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
        mongoDb.establecerConexion();
        coleccionTrabajos = mongoDb.getBD().getCollection(COLECCION);
    }

    @Override
    public void terminar() {
        mongoDb.cerrarConexion();
    }

    private Trabajo getTrabajo(Document documento) {
        if (documento == null) {
            return null;
        }

        Document docCliente = documento.get(CLIENTE, Document.class);
        Document docVehiculo = documento.get(VEHICULO, Document.class);

        String dni = docCliente.getString(Clientes.DNI);
        String nombre = docCliente.getString(Clientes.NOMBRE);
        String telefono = docCliente.getString(Clientes.TELEFONO);

        String matricula = docVehiculo.getString(Vehiculos.MATRICULA);
        String marca = docVehiculo.getString(Vehiculos.MARCA);
        String modelo = docVehiculo.getString(Vehiculos.MODELO);

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
            double precioMaterial = documento.getDouble(PRECIO_MATERIAL) == null ? 0.0 : documento.getDouble(PRECIO_MATERIAL);
            if (precioMaterial > 0) {
                try {
                    mecanico.anadirPrecioMaterial((float) precioMaterial);
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

        boolean esRevision = trabajo instanceof Revision;
        boolean esMecanico = trabajo instanceof Mecanico;

        Document documento = new Document()
                .append(DNI_CLIENTE, trabajo.getCliente().getDni())
                .append(MATRICULA_VEHICULO, trabajo.getVehiculo().matricula())
                .append(CLIENTE, docCliente)
                .append(VEHICULO, docVehiculo)
                .append(FECHA_INICIO, trabajo.getFechaInicio().toString())
                .append(FECHA_FIN, trabajo.estaCerrado() ? trabajo.getFechaFin().toString() : null)
                .append(TIPO, trabajo.getTipo())
                .append(REVISION, esRevision)
                .append(MECANICO, esMecanico)
                .append(HORAS, trabajo.getHoras());

        if (trabajo instanceof Mecanico mecanico) {
            documento.append(PRECIO_MATERIAL, (double) mecanico.getPrecioMaterial());
        } else {
            documento.append(PRECIO_MATERIAL, 0.0);
        }

        return documento;
    }

    @Override
    public List<Trabajo> get() {
        List<Trabajo> trabajos = new ArrayList<>();
        FindIterable<Document> documentos = coleccionTrabajos.find()
                .sort(Sorts.ascending(DNI_CLIENTE, MATRICULA_VEHICULO, FECHA_INICIO));
        for (Document documento : documentos) {
            trabajos.add(getTrabajo(documento));
        }
        return trabajos;
    }

    @Override
    public List<Trabajo> get(Cliente cliente) {
        Objects.requireNonNull(cliente, "El cliente no puede ser nulo.");
        List<Trabajo> trabajos = new ArrayList<>();
        FindIterable<Document> documentos = coleccionTrabajos.find(Filters.eq(DNI_CLIENTE, cliente.getDni()))
                .sort(Sorts.ascending(FECHA_INICIO));
        for (Document documento : documentos) {
            trabajos.add(getTrabajo(documento));
        }
        return trabajos;
    }

    @Override
    public List<Trabajo> get(Vehiculo vehiculo) {
        Objects.requireNonNull(vehiculo, "El vehículo no puede ser nulo.");
        List<Trabajo> trabajos = new ArrayList<>();
        FindIterable<Document> documentos = coleccionTrabajos.find(Filters.eq(MATRICULA_VEHICULO, vehiculo.matricula()))
                .sort(Sorts.ascending(FECHA_INICIO));
        for (Document documento : documentos) {
            trabajos.add(getTrabajo(documento));
        }
        return trabajos;
    }

    @Override
    public Map<TipoTrabajo, Integer> getEstadisticasMensuales(LocalDate mes) {
        Objects.requireNonNull(mes, "El mes no puede ser nulo.");
        Map<TipoTrabajo, Integer> estadisticas = new EnumMap<>(TipoTrabajo.class);
        for (TipoTrabajo tipoTrabajo : TipoTrabajo.values()) {
            estadisticas.put(tipoTrabajo, 0);
        }

        List<Trabajo> trabajos = get();
        for (Trabajo trabajo : trabajos) {
            LocalDate fecha = trabajo.getFechaInicio();
            if (fecha.getMonthValue() == mes.getMonthValue() && fecha.getYear() == mes.getYear()) {
                TipoTrabajo tipoTrabajo = TipoTrabajo.get(trabajo);
                estadisticas.put(tipoTrabajo, estadisticas.get(tipoTrabajo) + 1);
            }
        }

        return estadisticas;
    }

    @Override
    public void insertar(Trabajo trabajo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No se puede insertar un trabajo nulo.");
        comprobarTrabajo(trabajo.getCliente(), trabajo.getVehiculo(), trabajo.getFechaInicio());
        coleccionTrabajos.insertOne(getDocumento(trabajo));
    }

    private void comprobarTrabajo(Cliente cliente, Vehiculo vehiculo, LocalDate fechaInicio) throws TallerMecanicoExcepcion {
        List<Trabajo> trabajos = get();
        for (Trabajo trabajo : trabajos) {
            if (!trabajo.estaCerrado()) {
                if (trabajo.getCliente().equals(cliente)) {
                    throw new TallerMecanicoExcepcion("El cliente tiene otro trabajo en curso.");
                } else if (trabajo.getVehiculo().equals(vehiculo)) {
                    throw new TallerMecanicoExcepcion("El vehículo está actualmente en el taller.");
                }
            } else {
                if (trabajo.getCliente().equals(cliente) && !fechaInicio.isAfter(trabajo.getFechaFin())) {
                    throw new TallerMecanicoExcepcion("El cliente tiene otro trabajo posterior.");
                } else if (trabajo.getVehiculo().equals(vehiculo) && !fechaInicio.isAfter(trabajo.getFechaFin())) {
                    throw new TallerMecanicoExcepcion("El vehículo tiene otro trabajo posterior.");
                }
            }
        }
    }

    private Document getDocumentoTrabajoAbierto(Vehiculo vehiculo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(vehiculo, "No puedo operar sobre un vehículo nulo.");
        Document documento = coleccionTrabajos.find(
                Filters.and(
                        Filters.eq(MATRICULA_VEHICULO, vehiculo.matricula()),
                        Filters.eq(FECHA_FIN, null)
                )
        ).first();

        if (documento == null) {
            throw new TallerMecanicoExcepcion("No existe ningún trabajo abierto para dicho vehículo.");
        }

        return documento;
    }

    @Override
    public Trabajo anadirHoras(Trabajo trabajo, int horas) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No puedo añadir horas a un trabajo nulo.");
        Document docAbierto = getDocumentoTrabajoAbierto(trabajo.getVehiculo());
        Trabajo trabajoEncontrado = getTrabajo(docAbierto);

        trabajoEncontrado.anadirHoras(horas);

        coleccionTrabajos.updateOne(
                Filters.eq("_id", docAbierto.getObjectId("_id")),
                Updates.set(HORAS, trabajoEncontrado.getHoras())
        );

        return trabajoEncontrado;
    }

    @Override
    public Trabajo anadirPrecioMaterial(Trabajo trabajo, float precioMaterial) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No puedo añadir precio del material a un trabajo nulo.");
        Document docAbierto = getDocumentoTrabajoAbierto(trabajo.getVehiculo());
        Trabajo trabajoEncontrado = getTrabajo(docAbierto);

        if (trabajoEncontrado instanceof Mecanico mecanico) {
            mecanico.anadirPrecioMaterial(precioMaterial);
            coleccionTrabajos.updateOne(
                    Filters.eq("_id", docAbierto.getObjectId("_id")),
                    Updates.set(PRECIO_MATERIAL, (double) mecanico.getPrecioMaterial())
            );
        } else {
            throw new TallerMecanicoExcepcion("No se puede añadir precio al material para este tipo de trabajos.");
        }

        return trabajoEncontrado;
    }

    @Override
    public Trabajo cerrar(Trabajo trabajo, LocalDate fechaFin) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No puedo cerrar un trabajo nulo.");
        Document docAbierto = getDocumentoTrabajoAbierto(trabajo.getVehiculo());
        Trabajo trabajoEncontrado = getTrabajo(docAbierto);

        trabajoEncontrado.cerrar(fechaFin);

        coleccionTrabajos.updateOne(
                Filters.eq("_id", docAbierto.getObjectId("_id")),
                Updates.set(FECHA_FIN, fechaFin.toString())
        );

        return trabajoEncontrado;
    }

    @Override
    public Trabajo buscar(Trabajo trabajo) {
        Objects.requireNonNull(trabajo, "No se puede buscar un trabajo nulo.");

        Document documento = coleccionTrabajos.find(
                Filters.and(
                        Filters.eq(DNI_CLIENTE, trabajo.getCliente().getDni()),
                        Filters.eq(MATRICULA_VEHICULO, trabajo.getVehiculo().matricula()),
                        Filters.eq(FECHA_INICIO, trabajo.getFechaInicio().toString())
                )
        ).first();

        return getTrabajo(documento);
    }

    @Override
    public void borrar(Trabajo trabajo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No se puede borrar un trabajo nulo.");
        Trabajo trabajoEncontrado = buscar(trabajo);
        if (trabajoEncontrado == null) {
            throw new TallerMecanicoExcepcion("No existe ningún trabajo igual.");
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
