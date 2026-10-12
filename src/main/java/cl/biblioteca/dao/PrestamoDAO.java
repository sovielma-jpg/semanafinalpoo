package cl.biblioteca.dao;

import cl.biblioteca.config.DatabaseConnection;
import cl.biblioteca.model.Prestamo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAO implements CrudDAO<Prestamo> {

    private static final String SELECT =
            "SELECT p.id, p.id_estudiante, p.id_libro, p.fecha_prestamo, p.fecha_devolucion, p.devuelto, "
          + "e.nombre, l." + LibroDAO.COL_TITULO + " "
          + "FROM prestamos p JOIN estudiantes e ON e.id = p.id_estudiante "
          + "JOIN libros l ON l.id = p.id_libro ";

    private Connection cn() throws SQLException { return DatabaseConnection.getInstance().getConnection(); }

    private Prestamo mapear(ResultSet rs) throws SQLException {
        Prestamo p = new Prestamo(rs.getInt(1), rs.getInt(2), rs.getInt(3),
                rs.getDate(4).toLocalDate(), rs.getDate(5).toLocalDate(), rs.getBoolean(6));
        p.setEstudiante(rs.getString(7));
        p.setLibro(rs.getString(8));
        return p;
    }

    /** Inserta usando una conexión recibida (para participar en una transacción). */
    public void crear(Connection con, Prestamo p) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO prestamos (id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto) VALUES (?,?,?,?,?)")) {
            ps.setInt(1, p.getIdEstudiante());
            ps.setInt(2, p.getIdLibro());
            ps.setDate(3, Date.valueOf(p.getFechaPrestamo()));
            ps.setDate(4, Date.valueOf(p.getFechaDevolucion()));
            ps.setBoolean(5, p.isDevuelto());
            ps.executeUpdate();
        }
    }

    @Override
    public void crear(Prestamo p) throws SQLException { crear(cn(), p); }

    @Override
    public Prestamo buscarPorId(int id) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement(SELECT + "WHERE p.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Prestamo> listar() throws SQLException {
        return consultar(SELECT + "ORDER BY p.fecha_prestamo DESC, p.id DESC");
    }

    public List<Prestamo> listarActivos() throws SQLException {
        return consultar(SELECT + "WHERE p.devuelto = FALSE ORDER BY p.fecha_devolucion");
    }

    public List<Prestamo> listarPorEstudiante(int idEstudiante) throws SQLException {
        List<Prestamo> lista = new ArrayList<>();
        try (PreparedStatement ps = cn().prepareStatement(SELECT + "WHERE p.id_estudiante = ? ORDER BY p.fecha_prestamo DESC, p.id DESC")) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private List<Prestamo> consultar(String sql) throws SQLException {
        List<Prestamo> lista = new ArrayList<>();
        try (Statement st = cn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    /** Reporte: libros más prestados -> {titulo, cantidad}. */
    public List<Object[]> librosMasPrestados() throws SQLException {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT l." + LibroDAO.COL_TITULO + ", COUNT(*) AS total FROM prestamos p "
                   + "JOIN libros l ON l.id = p.id_libro GROUP BY l.id, l." + LibroDAO.COL_TITULO
                   + " ORDER BY total DESC, l." + LibroDAO.COL_TITULO;
        try (Statement st = cn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) filas.add(new Object[]{rs.getString(1), rs.getInt(2)});
        }
        return filas;
    }

    @Override
    public void actualizar(Prestamo p) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement(
                "UPDATE prestamos SET id_estudiante=?, id_libro=?, fecha_prestamo=?, fecha_devolucion=?, devuelto=? WHERE id=?")) {
            ps.setInt(1, p.getIdEstudiante());
            ps.setInt(2, p.getIdLibro());
            ps.setDate(3, Date.valueOf(p.getFechaPrestamo()));
            ps.setDate(4, Date.valueOf(p.getFechaDevolucion()));
            ps.setBoolean(5, p.isDevuelto());
            ps.setInt(6, p.getId());
            ps.executeUpdate();
        }
    }

    /** Marca como devuelto dentro de una transacción. */
    public boolean marcarDevuelto(Connection con, int idPrestamo) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE prestamos SET devuelto = TRUE WHERE id = ? AND devuelto = FALSE")) {
            ps.setInt(1, idPrestamo);
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("DELETE FROM prestamos WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
