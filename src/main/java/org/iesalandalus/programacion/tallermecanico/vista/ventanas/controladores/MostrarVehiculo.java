package org.iesalandalus.programacion.tallermecanico.vista.ventanas.controladores;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Vehiculo;
import org.iesalandalus.programacion.tallermecanico.vista.eventos.Evento;
import org.iesalandalus.programacion.tallermecanico.vista.ventanas.VistaVentanas;
import org.iesalandalus.programacion.tallermecanico.vista.ventanas.utilidades.Controlador;
import org.iesalandalus.programacion.tallermecanico.vista.ventanas.utilidades.Dialogos;

public class MostrarVehiculo extends Controlador {

	@FXML private TextField tfMarca;
	@FXML private TextField tfModelo;
	@FXML private TextField tfMatricula;

	public void actualizar(Vehiculo vehiculo) {
		if (vehiculo != null) {
			tfMarca.setText(vehiculo.marca());
			tfModelo.setText(vehiculo.modelo());
			tfMatricula.setText(vehiculo.matricula());
		}
	}

	public Vehiculo getVehiculo() {
		String matricula = tfMatricula.getText();

		return Vehiculo.get(matricula);
	}

	@FXML
	void aceptar() {
		getEscenario().close();
	}
	

	@FXML
	void borrar() {
		if (Dialogos.mostrarDialogoConfirmacion(Evento.BORRAR_VEHICULO.toString(), "¿Estás seguro que deseas borrar el vehículo?", getEscenario())) {
			VistaVentanas.getInstancia().getGestorEventos().notificar(Evento.BORRAR_VEHICULO);
			getEscenario().close();
		}
	}
	
	@FXML
	void listarTrabajosVehiculo() {
		VistaVentanas.getInstancia().getGestorEventos().notificar(Evento.LISTAR_TRABAJOS_VEHICULO);
	}
}
