package cl.biblioteca.dao;

import cl.biblioteca.config.DatabaseConnection;
import cl.biblioteca.model.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO implements CrudDAO<Categoria> {

    private Connection cn() throws SQLException { return DatabaseConnection.getInstance().getConnection(); }

    @Override
    public void crear(Categoria c) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("INSERT INTO categorias (nombre) VALUES (?)")) {
            ps.setString(1, c.getNombre());
            ps.executeUpdate();
        }
    }

    @Override
    public Categoria buscarPorId(int id) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("SELECT id, nombre FROM categorias WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new Categoria(rs.getInt(1), rs.getString(2)) : null;
            }
        }
    }

    @Override
    public List<Categoria> listar() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        try (Statement st = cn().createStatement();
             ResultSet rs = st.executeQuery("SELECT id, nombre FROM categorias ORDER BY nombre")) {
            while (rs.next()) lista.add(new Categoria(rs.getInt(1), rs.getString(2)));
        }
        return lista;
    }

    @Override
    public void actualizar(Categoria c) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("UPDATE categorias SET nombre = ? WHERE id = ?")) {
            ps.setString(1, c.getNombre());
            ps.setInt(2, c.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("DELETE FROM categorias WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
