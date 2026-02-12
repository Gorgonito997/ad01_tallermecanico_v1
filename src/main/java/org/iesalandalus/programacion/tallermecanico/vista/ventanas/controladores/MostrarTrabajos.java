package org.iesalandalus.programacion.tallermecanico.vista.ventanas.controladores;

import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Mecanico;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.TipoTrabajo;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Trabajo;
import org.iesalandalus.programacion.tallermecanico.vista.eventos.Evento;
import org.iesalandalus.programacion.tallermecanico.vista.ventanas.VistaVentanas;
import org.iesalandalus.programacion.tallermecanico.vista.ventanas.utilidades.Controlador;
import org.iesalandalus.programacion.tallermecanico.vista.ventanas.utilidades.Controladores;
import org.iesalandalus.programacion.tallermecanico.vista.ventanas.utilidades.Controles.FormateadorCeldaFecha;
import org.iesalandalus.programacion.tallermecanico.vista.ventanas.utilidades.Dialogos;

import java.time.LocalDate;
import java.util.List;

public class MostrarTrabajos extends Controlador {

	@FXML private Button btBorrar;
	@FXML private Button btAnadirHoras;
	@FXML private Button btAnadirPrecioMaterial;
	@FXML private Button btCerrar;
	@FXML private TableView<Trabajo> tvTrabajos;
	@FXML private TableColumn<Trabajo, LocalDate> tcFechaAlquiler;
	@FXML private TableColumn<Trabajo, LocalDate> tcFechaDevolucion;
	@FXML private TableColumn<Trabajo, String> tcNombre;
	@FXML private TableColumn<Trabajo, String> tcDni;
	@FXML private TableColumn<Trabajo, String> tcTelefono;
	@FXML private TableColumn<Trabajo, String> tcMarca;
	@FXML private TableColumn<Trabajo, String> tcModelo;
	@FXML private TableColumn<Trabajo, String> tcMatricula;
	@FXML private TableColumn<Trabajo, String> tcTipo;
	@FXML private TableColumn<Trabajo, Integer> tcHoras;
	@FXML private TableColumn<Trabajo, String> tcPrecioMaterial;
	@FXML private TableColumn<Trabajo, Float> tcPrecio;


	private ObservableList<Trabajo> trabajos;
	private int horas;
	private float precioMaterial;

	@FXML
	void initialize() {
		tcFechaAlquiler.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));
		tcFechaAlquiler.setCellFactory(columna -> new FormateadorCeldaFecha<>());
		tcFechaDevolucion.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));
		tcFechaDevolucion.setCellFactory(columna -> new FormateadorCeldaFecha<>());
		tcNombre.setCellValueFactory(fila -> new SimpleStringProperty(fila.getValue().getCliente().getNombre()));
		tcDni.setCellValueFactory(fila -> new SimpleStringProperty(fila.getValue().getCliente().getDni()));
		tcTelefono.setCellValueFactory(fila -> new SimpleStringProperty(fila.getValue().getCliente().getTelefono()));
		tcPrecio.setCellValueFactory(fila -> new SimpleFloatProperty(fila.getValue().getPrecio()).asObject());
		tcMarca.setCellValueFactory(fila -> new SimpleStringProperty(fila.getValue().getVehiculo().marca()));
		tcModelo.setCellValueFactory(fila -> new SimpleStringProperty(fila.getValue().getVehiculo().modelo()));
		tcMatricula.setCellValueFactory(fila -> new SimpleStringProperty(fila.getValue().getVehiculo().matricula()));
		tcTipo.setCellValueFactory(fila -> new SimpleStringProperty(TipoTrabajo.get(fila.getValue()).toString()));
		tcHoras.setCellValueFactory(new PropertyValueFactory<>("horas"));
		tcPrecioMaterial.setCellValueFactory(fila -> {
			String cadenaPrecioMaterial = "";
			if (fila.getValue() instanceof Mecanico mecanico) {
				cadenaPrecioMaterial = Float.toString(mecanico.getPrecioMaterial());
			}
			return new SimpleStringProperty(cadenaPrecioMaterial);
		});
		tcPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
		tvTrabajos.getSelectionModel().selectedItemProperty().addListener((ob, ov, nv) -> revisarEstadoBotones(nv));
	}

	@FXML
	void aceptar() {
		getEscenario().close();
	}

	@FXML
	void anadirHoras() {
		String cadenaHoras = Dialogos.mostrarDialogoTexto("Añadir horas", "Introduce las horas:", getEscenario(), "\\d+");
		if (cadenaHoras != null) {
			horas = 0;
			try {
				horas = Integer.parseInt(cadenaHoras);
			} catch (NumberFormatException nfe) {/**/}
			VistaVentanas.getInstancia().getGestorEventos().notificar(Evento.ANADIR_HORAS_TRABAJO);
		}
	}

	@FXML
	void anadirPrecioMaterial() {
		String cadenaPrecioMaterial = Dialogos.mostrarDialogoTexto("Añadir precio material", "Introduce el precio del material:", getEscenario(), "\\d*(\\.\\d+)?");
		if (cadenaPrecioMaterial != null) {
			precioMaterial = 0;
			try {
				precioMaterial = Float.parseFloat(cadenaPrecioMaterial);
			} catch (NumberFormatException nfe) {/**/}
			VistaVentanas.getInstancia().getGestorEventos().notificar(Evento.ANADIR_PRECIO_MATERIAL_TRABAJO);
		}
	}

	@FXML
	void cerrar() {
		LeerFechaFin leerFechaFin = (LeerFechaFin) Controladores.get("/vistas/LeerFechaFin.fxml", "Leer fecha fin", getEscenario());
		leerFechaFin.limpiar();
		leerFechaFin.getEscenario().showAndWait();
	}

	@FXML
	void anadir() {
		LeerTrabajo leerTrabajo = (LeerTrabajo) Controladores.get("/vistas/LeerTrabajo.fxml", "Añadir Trabajo", getEscenario());
		leerTrabajo.setClientes(VistaVentanas.getInstancia().getClientes());
		leerTrabajo.setVehiculos(VistaVentanas.getInstancia().getVehiculos());
		leerTrabajo.limpiar();
		MostrarCliente mostrarCliente = (MostrarCliente) Controladores.get("/vistas/MostrarCliente.fxml", "Mostrar cliente", getEscenario());
		MostrarVehiculo mostrarVehiculo = (MostrarVehiculo) Controladores.get("/vistas/MostrarVehiculo.fxml", "Mostrar vehiculo", getEscenario());
		if (mostrarCliente.getEscenario().isShowing()) {
			leerTrabajo.setCliente(mostrarCliente.getCliente());
			//AQUI ESTABA EL ERROR!
		} else if(mostrarVehiculo.getEscenario().isShowing()){
			leerTrabajo.setVehiculo(mostrarVehiculo.getVehiculo());
		}
		leerTrabajo.getEscenario().showAndWait();
	}

	@FXML
	void borrar() {
		if (Dialogos.mostrarDialogoConfirmacion(Evento.BORRAR_TRABAJO.toString(), "¿Estás seguro que deseas borrar el trabajo?", getEscenario())) {
			VistaVentanas.getInstancia().getGestorEventos().notificar(Evento.BORRAR_TRABAJO);
		}
	}

	public Trabajo getTrabajo() {
		LeerTrabajo leerTrabajo = (LeerTrabajo) Controladores.get("/vistas/LeerTrabajo.fxml", "Añadir Trabajo", getEscenario());
		return (leerTrabajo.getEscenario().isShowing()) ? leerTrabajo.getTrabajo() : tvTrabajos.getSelectionModel().getSelectedItem();
	}

	public int getHoras() {
		return horas;
	}

	public float getPrecioMaterial() {
		return precioMaterial;
	}

	private void revisarEstadoBotones(Trabajo trabajo) {
		btBorrar.setDisable(tvTrabajos.getSelectionModel().getSelectedItem() == null);
		btCerrar.setDisable(trabajo == null || trabajo.getFechaFin() != null);
		btAnadirHoras.setDisable(trabajo == null || trabajo.getFechaFin() != null);
		btAnadirPrecioMaterial.setDisable(!(trabajo instanceof Mecanico) || trabajo.getFechaFin() != null);
	}

	public void actualizar(List<Trabajo> trabajos) {
		this.trabajos = FXCollections.observableArrayList(trabajos);
		tvTrabajos.setItems(this.trabajos);
		tvTrabajos.refresh();
    }

	public void actualizar(Trabajo trabajo) {
		int indiceTrabajo = trabajos.indexOf(trabajo);
		if (indiceTrabajo != -1) {
			trabajos.set(indiceTrabajo, trabajo);
		}
	}

	public ObservableList<Trabajo> getTrabajos() {
		return trabajos;
	}

}
