package login;

public class UserSession {
    private final String nombre;
    private final String apellido;
    private final String email;
    private final int rol;

    public UserSession(String nombre, String apellido, String email, int rol) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.rol = rol;
    }

    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getEmail() { return email; }
    public int getRol() { return rol; }
}