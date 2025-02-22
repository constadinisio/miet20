package login;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static Conexion instancia;
    private Connection conexion;
    
    Conexion() {
        try {
            String url = "jdbc:mysql://localhost:3306/et20plataforma"; // Cambia por tu URL
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
