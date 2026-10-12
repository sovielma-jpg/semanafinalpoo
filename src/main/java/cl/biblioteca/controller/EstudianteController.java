package cl.biblioteca.controller;

import cl.biblioteca.dao.EstudianteDAO;
import cl.biblioteca.dao.UsuarioDAO;
import cl.biblioteca.model.Estudiante;
import cl.biblioteca.model.Usuario;
import cl.biblioteca.service.BibliotecaException;

import java.sql.SQLException;
import java.util.List;

public class EstudianteController {
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public List<Estudiante> listar() throws BibliotecaException {
        try { return estudianteDAO.listar(); } catch (SQLException e) { throw error(e); }
    }

    public Estudiante buscarPorRut(String rut) throws BibliotecaException {
        try { return estudianteDAO.buscarPorRut(rut); } catch (SQLException e) { throw error(e); }
    }

    /**
     * Registra o actualiza al estudiante. Al crearlo, también se crea su usuario de acceso
     * (rol ESTUDIANTE) con contraseña inicial igual a su RUT.
     */
    public void guardar(Estudiante e) throws BibliotecaException {
        if (e.getNombre().isBlank() || e.getRut().isBlank() || e.getCurso().isBlank() || e.getCorreo().isBlank())
            throw new BibliotecaException("Todos los campos son obligatorios.");
        if (!e.getRut().matches("\\d{7,8}-[0-9kK]"))
            throw new BibliotecaException("RUT inválido. Formato esperado: 12345678-9");
        if (!e.getCorreo().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new BibliotecaException("Correo inválido.");
        try {
            if (e.getId() == 0) {
                estudianteDAO.crear(e);
                if (!usuarioDAO.existePorRut(e.getRut())) {
                    usuarioDAO.crear(new Usuario(0, e.getNombre(), e.getRut(), e.getCorreo(), e.getRut(), Usuario.ESTUDIANTE));
                }
            } else {
                estudianteDAO.actualizar(e);
            }
        } catch (SQLException ex) { throw error(ex); }
    }

    public void eliminar(int id) throws BibliotecaException {
        try { estudianteDAO.eliminar(id); }
        catch (SQLException e) { throw new BibliotecaException("No se pudo eliminar (¿tiene préstamos asociados?): " + e.getMessage(), e); }
    }

    private BibliotecaException error(SQLException e) { return new BibliotecaException("Error de base de datos: " + e.getMessage(), e); }
}
