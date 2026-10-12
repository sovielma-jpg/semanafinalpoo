package cl.biblioteca.controller;

import cl.biblioteca.dao.PrestamoDAO;
import cl.biblioteca.model.Prestamo;
import cl.biblioteca.service.BibliotecaException;
import cl.biblioteca.service.PrestamoService;
import cl.biblioteca.service.PrestamoTask;

import java.sql.SQLException;
import java.util.List;

public class PrestamoController {
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();

    /** Lanza el préstamo en un hilo independiente; la vista recibe el resultado por el listener. */
    public void registrarAsync(int idEstudiante, int idLibro, PrestamoTask.Listener listener) {
        PrestamoTask.iniciar(idEstudiante, idLibro, listener);
    }

    public long devolver(int idPrestamo) throws BibliotecaException {
        return PrestamoService.getInstance().registrarDevolucion(idPrestamo);
    }

    public List<Prestamo> listar() throws BibliotecaException {
        try { return prestamoDAO.listar(); } catch (SQLException e) { throw error(e); }
    }

    public List<Prestamo> listarActivos() throws BibliotecaException {
        try { return prestamoDAO.listarActivos(); } catch (SQLException e) { throw error(e); }
    }

    public List<Prestamo> historialDe(int idEstudiante) throws BibliotecaException {
        try { return prestamoDAO.listarPorEstudiante(idEstudiante); } catch (SQLException e) { throw error(e); }
    }

    public List<Object[]> librosMasPrestados() throws BibliotecaException {
        try { return prestamoDAO.librosMasPrestados(); } catch (SQLException e) { throw error(e); }
    }

    private BibliotecaException error(SQLException e) { return new BibliotecaException("Error de base de datos: " + e.getMessage(), e); }
}
