package cl.biblioteca.dao;

import java.sql.SQLException;
import java.util.List;

/** Interfaz genérica CRUD implementada por cada DAO (abstracción / polimorfismo). */
public interface CrudDAO<T> {
    void crear(T entidad) throws SQLException;
    T buscarPorId(int id) throws SQLException;
    List<T> listar() throws SQLException;
    void actualizar(T entidad) throws SQLException;
    void eliminar(int id) throws SQLException;
}
