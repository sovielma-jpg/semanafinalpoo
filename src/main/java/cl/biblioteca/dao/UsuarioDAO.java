package cl.biblioteca.dao;

import cl.biblioteca.config.DatabaseConnection;
import cl.biblioteca.model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO implements CrudDAO<Usuario> {

    /** Nombre de la columna de la contraseña (cámbialo si tu script oficial usa "contraseña"). */
    static final String COL_PASS = "contraseña";

    private static final String SELECT = "SELECT id, nombre, rut, correo, " + COL_PASS + ", rol FROM usuarios ";

    private Connection cn() throws SQLException { return DatabaseConnection.getInstance().getConnection(); }

    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                rs.getString(5), rs.getString(6));
    }

    /** Valida credenciales (correo o RUT + contraseña). Devuelve null si no existe. */
    public Usuario autenticar(String identificador, String contraseña) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement(
                SELECT + "WHERE (correo = ? OR rut = ?) AND " + COL_PASS + " = ?")) {
            ps.setString(1, identificador);
            ps.setString(2, identificador);
            ps.setString(3, contraseña);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public void crear(Usuario u) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement(
                "INSERT INTO usuarios (nombre, rut, correo, " + COL_PASS + ", rol) VALUES (?,?,?,?,?)")) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getRut());
            ps.setString(3, u.getCorreo());
            ps.setString(4, u.getContrasena());
            ps.setString(5, u.getRol());
            ps.executeUpdate();
        }
    }

    @Override
    public Usuario buscarPorId(int id) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement(SELECT + "WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public boolean existePorRut(String rut) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("SELECT 1 FROM usuarios WHERE rut = ?")) {
            ps.setString(1, rut);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    @Override
    public List<Usuario> listar() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        try (Statement st = cn().createStatement(); ResultSet rs = st.executeQuery(SELECT + "ORDER BY nombre")) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    @Override
    public void actualizar(Usuario u) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement(
                "UPDATE usuarios SET nombre=?, rut=?, correo=?, " + COL_PASS + "=?, rol=? WHERE id=?")) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getRut());
            ps.setString(3, u.getCorreo());
            ps.setString(4, u.getContrasena());
            ps.setString(5, u.getRol());
            ps.setInt(6, u.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("DELETE FROM usuarios WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
