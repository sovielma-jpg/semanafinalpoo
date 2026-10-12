package cl.biblioteca.service;

/** Error de negocio (sin stock, préstamo ya devuelto, datos inválidos...). */
public class BibliotecaException extends Exception {
    public BibliotecaException(String message) { super(message); }
    public BibliotecaException(String message, Throwable cause) { super(message, cause); }
}
