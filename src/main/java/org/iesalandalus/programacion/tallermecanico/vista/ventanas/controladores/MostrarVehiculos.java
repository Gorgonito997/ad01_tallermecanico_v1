package org.iesalandalus.programacion.tallermecanico.vista.ventanas.controladores;

import java.util.List;

import javafx.collections.ObservableList;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Vehiculo;
import org.iesalandalus.programacion.tallermecanico.vista.ventanas.utilidades.Controlador;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class MostrarVehiculos extends Controlador {

	@FXML private TableView<Vehiculo> tvVehiculos;
	@FXML private TableColumn<Vehiculo, String> tcMarca;
	@FXML private TableColumn<Vehiculo, String> tcModelo;
	@FXML private TableColumn<Vehiculo, String> tcMatricula;

	ObservableList<Vehiculo> vehiculos;

	@FXML
    void initialize() {
    	tcMarca.setCellValueFactory(fila -> new SimpleStringProperty(fila.getValue().marca()));
    	tcModelo.setCellValueFactory(fila -> new SimpleStringProperty(fila.getValue().modelo()));
    	tcMatricula.setCellValueFactory(fila -> new SimpleStringProperty(fila.getValue().matricula()));
	}

	@FXML
	void aceptar() {
		getEscenario().close();
	}

//
	public void actualizar(List<Vehiculo> vehiculos) {
		this.vehiculos = FXCollections.observableArrayList(vehiculos);
		tvVehiculos.setItems(FXCollections.observableArrayList(vehiculos));
		tvVehiculos.refresh();
	}

	public ObservableList<Vehiculo> getVehiculos() {
		return vehiculos;
	}
}
