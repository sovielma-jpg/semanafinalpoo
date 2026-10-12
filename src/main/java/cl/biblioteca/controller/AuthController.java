package cl.biblioteca.controller;

import cl.biblioteca.dao.UsuarioDAO;
import cl.biblioteca.model.Usuario;
import cl.biblioteca.service.BibliotecaException;

import java.sql.SQLException;

public class AuthController {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Usuario login(String identificador, String contrasena) throws BibliotecaException {
        if (identificador == null || identificador.isBlank() || contrasena == null || contrasena.isEmpty()) {
            throw new BibliotecaException("Ingrese su correo o RUT y su contraseña.");
        }
        try {
            Usuario u = usuarioDAO.autenticar(identificador.trim(), contrasena);
            if (u == null) throw new BibliotecaException("Credenciales incorrectas.");
            return u;
        } catch (SQLException e) {
            throw new BibliotecaException("No se pudo conectar a la base de datos: " + e.getMessage(), e);
        }
    }
}
