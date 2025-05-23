package main.java.views.login;

/**
 * Clase que representa la sesión de un usuario en la aplicación. Almacena
 * información básica del usuario después de la autenticación.
 *
 * @author [Nicolas Bogarin]
 * @author [Constantino Di Nisio]
 * @version 1.1
 * @since [12/03/2025]
 */
public class UserSession {

    // Atributos privados y finales para garantizar inmutabilidad
    private final String nombre;      // Nombre del usuario
    private final String apellido;    // Apellido del usuario
    private final String email;       // Correo electrónico del usuario
    private final int rol;            // Rol o nivel de acceso del usuario
    private final String fotoUrl;     // URL de la foto de perfil del usuario
    private final int userId;         // ID del usuario en la base de datos

    /**
     * Constructor para crear una instancia de UserSession.
     *
     * @param nombre Nombre del usuario
     * @param apellido Apellido del usuario
     * @param email Correo electrónico del usuario
     * @param rol Rol o nivel de acceso del usuario
     * @param fotoUrl URL de la foto de perfil del usuario
     */
    public UserSession(String nombre, String apellido, String email, int rol, String fotoUrl) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.rol = rol;
        this.fotoUrl = fotoUrl;
        this.userId = -1; // Valor por defecto cuando no se proporciona ID
    }

    /**
     * Constructor extendido que incluye el ID del usuario.
     *
     * @param userId ID del usuario en la base de datos
     * @param nombre Nombre del usuario
     * @param apellido Apellido del usuario
     * @param email Correo electrónico del usuario
     * @param rol Rol o nivel de acceso del usuario
     * @param fotoUrl URL de la foto de perfil del usuario
     */
    public UserSession(int userId, String nombre, String apellido, String email, int rol, String fotoUrl) {
        this.userId = userId;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.rol = rol;
        this.fotoUrl = fotoUrl;
    }

    /**
     * Obtiene el nombre del usuario.
     *
     * @return Nombre del usuario
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene el apellido del usuario.
     *
     * @return Apellido del usuario
     */
    public String getApellido() {
        return apellido;
    }

    /**
     * Obtiene el correo electrónico del usuario.
     *
     * @return Correo electrónico del usuario
     */
    public String getEmail() {
        return email;
    }

    /**
     * Obtiene el rol del usuario.
     *
     * @return Rol o nivel de acceso del usuario
     */
    public int getRol() {
        return rol;
    }

    /**
     * Obtiene la URL de la foto de perfil del usuario.
     *
     * @return URL de la foto de perfil
     */
    public String getFotoUrl() {
        return fotoUrl;
    }

    /**
     * Obtiene el ID del usuario en la base de datos.
     *
     * @return ID del usuario
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Devuelve una cadena con el nombre completo del usuario.
     *
     * @return Nombre y apellido del usuario
     */
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
