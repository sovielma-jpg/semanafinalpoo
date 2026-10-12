package cl.biblioteca.model;

public class Usuario extends Persona {
    public static final String BIBLIOTECARIO = "BIBLIOTECARIO";
    public static final String ESTUDIANTE = "ESTUDIANTE";

    private String contrasena;
    private String rol;

    public Usuario(int id, String nombre, String rut, String correo, String contrasena, String rol) {
        super(id, nombre, rut, correo);
        this.contrasena = contrasena;
        this.rol = rol;
    }

    @Override
    public String getTipo() { return "Usuario (" + rol + ")"; }

    public boolean esBibliotecario() { return BIBLIOTECARIO.equalsIgnoreCase(rol); }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
