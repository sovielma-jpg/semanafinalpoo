package cl.biblioteca.dao;

import cl.biblioteca.config.DatabaseConnection;
import cl.biblioteca.model.Estudiante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstudianteDAO implements CrudDAO<Estudiante> {

    private static final String SELECT = "SELECT id, nombre, rut, curso, correo FROM estudiantes ";

    private Connection cn() throws SQLException { return DatabaseConnection.getInstance().getConnection(); }

    private Estudiante mapear(ResultSet rs) throws SQLException {
        return new Estudiante(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5));
    }

    @Override
    public void crear(Estudiante e) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement(
                "INSERT INTO estudiantes (nombre, rut, curso, correo) VALUES (?,?,?,?)")) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getRut());
            ps.setString(3, e.getCurso());
            ps.setString(4, e.getCorreo());
            ps.executeUpdate();
        }
    }

    @Override
    public Estudiante buscarPorId(int id) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement(SELECT + "WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Estudiante buscarPorRut(String rut) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement(SELECT + "WHERE rut = ?")) {
            ps.setString(1, rut);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Estudiante> listar() throws SQLException {
        List<Estudiante> lista = new ArrayList<>();
        try (Statement st = cn().createStatement(); ResultSet rs = st.executeQuery(SELECT + "ORDER BY nombre")) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    @Override
    public void actualizar(Estudiante e) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement(
                "UPDATE estudiantes SET nombre=?, rut=?, curso=?, correo=? WHERE id=?")) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getRut());
            ps.setString(3, e.getCurso());
            ps.setString(4, e.getCorreo());
            ps.setInt(5, e.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("DELETE FROM estudiantes WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
