package cl.biblioteca.dao;

import cl.biblioteca.config.DatabaseConnection;
import cl.biblioteca.model.Libro;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibroDAO implements CrudDAO<Libro> {

    /** Nombre de la columna del título (cámbialo si tu script oficial usa "título"). */
    static final String COL_TITULO = "titulo";

    private static final String SELECT =
            "SELECT l.id, l." + COL_TITULO + ", l.autor, l.isbn, l.editorial, l.stock, l.id_categoria, c.nombre "
          + "FROM libros l JOIN categorias c ON c.id = l.id_categoria ";

    private Connection cn() throws SQLException { return DatabaseConnection.getInstance().getConnection(); }

    static Libro mapear(ResultSet rs) throws SQLException {
        Libro l = new Libro(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                rs.getString(5), rs.getInt(6), rs.getInt(7));
        l.setCategoria(rs.getString(8));
        return l;
    }

    @Override
    public void crear(Libro l) throws SQLException {
        String sql = "INSERT INTO libros (" + COL_TITULO + ", autor, isbn, editorial, stock, id_categoria) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = cn().prepareStatement(sql)) {
            ps.setString(1, l.getTitulo());
            ps.setString(2, l.getAutor());
            ps.setString(3, l.getIsbn());
            ps.setString(4, l.getEditorial());
            ps.setInt(5, l.getStock());
            ps.setInt(6, l.getIdCategoria());
            ps.executeUpdate();
        }
    }

    @Override
    public Libro buscarPorId(int id) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement(SELECT + "WHERE l.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Libro> listar() throws SQLException {
        List<Libro> lista = new ArrayList<>();
        try (Statement st = cn().createStatement();
             ResultSet rs = st.executeQuery(SELECT + "ORDER BY l." + COL_TITULO)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Libro> buscar(String texto) throws SQLException {
        List<Libro> lista = new ArrayList<>();
        String sql = SELECT + "WHERE l." + COL_TITULO + " LIKE ? OR l.autor LIKE ? OR l.isbn LIKE ? ORDER BY l." + COL_TITULO;
        try (PreparedStatement ps = cn().prepareStatement(sql)) {
            String like = "%" + texto + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public void actualizar(Libro l) throws SQLException {
        String sql = "UPDATE libros SET " + COL_TITULO + "=?, autor=?, isbn=?, editorial=?, stock=?, id_categoria=? WHERE id=?";
        try (PreparedStatement ps = cn().prepareStatement(sql)) {
            ps.setString(1, l.getTitulo());
            ps.setString(2, l.getAutor());
            ps.setString(3, l.getIsbn());
            ps.setString(4, l.getEditorial());
            ps.setInt(5, l.getStock());
            ps.setInt(6, l.getIdCategoria());
            ps.setInt(7, l.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("DELETE FROM libros WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Descuenta una unidad solo si hay stock. Devuelve false si no había stock. */
    public boolean disminuirStock(Connection con, int idLibro) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE libros SET stock = stock - 1 WHERE id = ? AND stock > 0")) {
            ps.setInt(1, idLibro);
            return ps.executeUpdate() == 1;
        }
    }

    public void aumentarStock(Connection con, int idLibro) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE libros SET stock = stock + 1 WHERE id = ?")) {
            ps.setInt(1, idLibro);
            ps.executeUpdate();
        }
    }
}
