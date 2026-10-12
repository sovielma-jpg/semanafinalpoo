package cl.biblioteca.service;

import cl.biblioteca.config.DatabaseConnection;
import cl.biblioteca.dao.LibroDAO;
import cl.biblioteca.dao.PrestamoDAO;
import cl.biblioteca.model.Prestamo;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Lógica crítica de préstamos y devoluciones.
 *
 * SINCRONIZACIÓN: los métodos que modifican el stock son {@code synchronized} sobre la
 * instancia única del servicio, de modo que dos hilos no pueden leer/modificar el stock
 * al mismo tiempo (evita condiciones de carrera). Además, el UPDATE usa "stock > 0"
 * y toda la operación corre en una transacción JDBC (commit / rollback).
 */
public class PrestamoService {

    private static final PrestamoService INSTANCE = new PrestamoService();

    private final LibroDAO libroDAO = new LibroDAO();
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();

    private PrestamoService() { }

    public static PrestamoService getInstance() { return INSTANCE; }

    /** Registra el préstamo, descuenta stock y deja el historial del estudiante (tabla prestamos). */
    public synchronized Prestamo registrarPrestamo(int idEstudiante, int idLibro) throws BibliotecaException {
        Connection con = null;
        boolean autoCommitOriginal = true;
        try {
            con = DatabaseConnection.getInstance().getConnection();
            autoCommitOriginal = con.getAutoCommit();
            con.setAutoCommit(false);

            if (!libroDAO.disminuirStock(con, idLibro)) {
                con.rollback();
                throw new BibliotecaException("El libro no tiene stock disponible.");
            }
            Prestamo p = Prestamo.nuevo(idEstudiante, idLibro);
            prestamoDAO.crear(con, p);
            con.commit();
            return p;
        } catch (SQLException e) {
            rollback(con);
            throw new BibliotecaException("Error de base de datos al registrar el préstamo: " + e.getMessage(), e);
        } finally {
            restaurar(con, autoCommitOriginal);
        }
    }

    /** Registra la devolución, repone stock y devuelve los días de atraso (0 = a tiempo). */
    public synchronized long registrarDevolucion(int idPrestamo) throws BibliotecaException {
        Connection con = null;
        boolean autoCommitOriginal = true;
        try {
            Prestamo p = prestamoDAO.buscarPorId(idPrestamo);
            if (p == null) throw new BibliotecaException("El préstamo no existe.");
            if (p.isDevuelto()) throw new BibliotecaException("El préstamo ya fue devuelto.");

            con = DatabaseConnection.getInstance().getConnection();
            autoCommitOriginal = con.getAutoCommit();
            con.setAutoCommit(false);

            if (!prestamoDAO.marcarDevuelto(con, idPrestamo)) {
                con.rollback();
                throw new BibliotecaException("No fue posible marcar el préstamo como devuelto.");
            }
            libroDAO.aumentarStock(con, p.getIdLibro());
            con.commit();
            return p.diasAtraso();
        } catch (SQLException e) {
            rollback(con);
            throw new BibliotecaException("Error de base de datos al registrar la devolución: " + e.getMessage(), e);
        } finally {
            restaurar(con, autoCommitOriginal);
        }
    }

    private void rollback(Connection con) {
        try { if (con != null) con.rollback(); } catch (SQLException ignored) { }
    }

    private void restaurar(Connection con, boolean autoCommit) {
        try { if (con != null) con.setAutoCommit(autoCommit); } catch (SQLException ignored) { }
    }
}
