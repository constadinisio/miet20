package login;

public class UserSession {
    private final String nombre;
    private final String apellido;
    private final String email;
    private final int rol;
    private final String fotoUrl;

    public UserSession(String nombre, String apellido, String email, int rol, String fotoUrl) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.rol = rol;
        this.fotoUrl = fotoUrl;
    }

    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getEmail() { return email; }
    public int getRol() { return rol; }
    public String getFotoUrl() { return fotoUrl; }
}