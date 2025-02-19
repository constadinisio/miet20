package users.Alumnos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static Conexion instancia;
    private Connection conexion;

    // Constructor privado para evitar instanciación externa
    Conexion() {
        try {
            String url = "jdbc:mysql://localhost/et20plataforma"; // Cambia por tu URL
            String usuario = "root";  // Cambia por tu usuario
            String contrasena = ""; // Cambia por tu contraseña
            conexion = DriverManager.getConnection(url, usuario, contrasena);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para obtener la instancia única de la conexión
    public static Conexion getInstancia() {
        if (instancia == null) {
            instancia = new Conexion();
        }
        return instancia;
    }

    // Método para obtener la conexión
    public Connection getConexion() {
        return conexion;
    }
}
