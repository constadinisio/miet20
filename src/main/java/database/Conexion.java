package main.java.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase que implementa el patrón Singleton para gestionar la conexión a la base de datos.
 * Proporciona un punto centralizado para obtener y gestionar la conexión de base de datos.
 * 
 * @author [Nicolas Bogarin]
 * @author [Constantino Di Nisio]
 * @version 1.0
 * @since [12/03/2025]
 */
public class Conexion {
    // Instancia única de la clase (Patrón Singleton)
    private static Conexion instancia;
    
    // Conexión a la base de datos
    private Connection conexion;
    
    /**
     * Constructor privado para implementar el patrón Singleton.
     * Establece la conexión con la base de datos MySQL.
     */
    Conexion() {
        try {
            // Parámetros de conexión a la base de datos
            String url = "jdbc:mysql://localhost:3306/et20plataforma";
            String usuario = "root";
            String contrasena = "";
            
            // Establecer conexión
            conexion = DriverManager.getConnection(url, usuario, contrasena);
        } catch (SQLException e) {
            // Manejo de errores de conexión
            System.err.println("Error al establecer conexión con la base de datos");
            e.printStackTrace();
        }
    }
    
    /**
     * Método para obtener la instancia única de la conexión (Patrón Singleton).
     * Si no existe una instancia, crea una nueva.
     * 
     * @return Instancia única de Conexion
     */
    public static Conexion getInstancia() {
        if (instancia == null) {
            instancia = new Conexion();
        }
        return instancia;
    }
    
    /**
    * Verifica si la conexión está cerrada y la restablece si es necesario.
    * 
    * @return Conexión activa a la base de datos
    */
    public Connection verificarConexion() {
        try {
            // Si la conexión está cerrada o es nula, crear una nueva
            if (conexion == null || conexion.isClosed()) {
                String url = "jdbc:mysql://localhost:3306/et20plataforma";
                String usuario = "root";
                String contrasena = "";

                // Establecer nueva conexión
                conexion = DriverManager.getConnection(url, usuario, contrasena);
                System.out.println("Conexión restablecida correctamente");
            }
            return conexion;
        } catch (SQLException e) {
            System.err.println("Error al verificar/restablecer la conexión: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Cierra la conexión actual a la base de datos.
     * Verifica que la conexión no esté ya cerrada antes de intentar cerrarla.
     */
    public void closeConexion() {
        try {
            // Verificar que la conexión exista y no esté cerrada
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("Conexión a la base de datos cerrada correctamente");
            }
        } catch (SQLException e) {
            // Manejo de errores al cerrar la conexión
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
            e.printStackTrace();
        }
    }
}