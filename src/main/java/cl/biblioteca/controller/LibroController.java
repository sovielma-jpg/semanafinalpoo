package cl.biblioteca.controller;

import cl.biblioteca.dao.CategoriaDAO;
import cl.biblioteca.dao.LibroDAO;
import cl.biblioteca.model.Categoria;
import cl.biblioteca.model.Libro;
import cl.biblioteca.service.BibliotecaException;

import java.sql.SQLException;
import java.util.List;

public class LibroController {
    private final LibroDAO libroDAO = new LibroDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    public List<Libro> listar() throws BibliotecaException {
        try { return libroDAO.listar(); } catch (SQLException e) { throw error(e); }
    }

    public List<Libro> buscar(String texto) throws BibliotecaException {
        try { return texto == null || texto.isBlank() ? libroDAO.listar() : libroDAO.buscar(texto.trim()); }
        catch (SQLException e) { throw error(e); }
    }

    public List<Categoria> categorias() throws BibliotecaException {
        try { return categoriaDAO.listar(); } catch (SQLException e) { throw error(e); }
    }

    public void guardar(Libro l) throws BibliotecaException {
        validar(l);
        try {
            if (l.getId() == 0) libroDAO.crear(l); else libroDAO.actualizar(l);
        } catch (SQLException e) { throw error(e); }
    }

    public void eliminar(int id) throws BibliotecaException {
        try { libroDAO.eliminar(id); }
        catch (SQLException e) { throw new BibliotecaException("No se pudo eliminar (¿tiene préstamos asociados?): " + e.getMessage(), e); }
    }

    private void validar(Libro l) throws BibliotecaException {
        if (vacio(l.getTitulo()) || vacio(l.getAutor()) || vacio(l.getIsbn()) || vacio(l.getEditorial()))
            throw new BibliotecaException("Título, autor, ISBN y editorial son obligatorios.");
        if (l.getStock() < 0) throw new BibliotecaException("El stock no puede ser negativo.");
        if (l.getIdCategoria() <= 0) throw new BibliotecaException("Seleccione una categoría.");
    }

    private boolean vacio(String s) { return s == null || s.isBlank(); }
    private BibliotecaException error(SQLException e) { return new BibliotecaException("Error de base de datos: " + e.getMessage(), e); }
}
