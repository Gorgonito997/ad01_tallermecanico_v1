package org.iesalandalus.programacion.tallermecanico.modelo.negocio.mongodb;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MongoDb {

    public static final String SERVIDOR = "cluster0.r0ww460.mongodb.net";
    public static final int PUERTO = 27017;
    public static final String BD = "tallerMecanico";
    public static final String USUARIO = "taller";
    public static final String CONTRASENA = "taller-2025";

    private MongoClient conexion;

    public MongoDb() {
    }

    public MongoDatabase getBD() {
        if (conexion == null) {establecerConexion();}
        return conexion.getDatabase(BD);
    }

    public void establecerConexion() {
        String pass = URLEncoder.encode(CONTRASENA, StandardCharsets.UTF_8);
        String uri = "mongodb+srv://" + USUARIO + ":" + pass + "@" + SERVIDOR + "/" + BD + "?retryWrites=true&w=majority";
        conexion = MongoClients.create(uri);
    }

    public void cerrarConexion() {
        if (conexion != null) {
            conexion.close();
        }
    }
}