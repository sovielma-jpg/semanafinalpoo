package cl.biblioteca.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Patrón SINGLETON: garantiza una única instancia que administra la conexión JDBC.
 * Los datos de conexión se pueden sobrescribir con -Ddb.url, -Ddb.user y -Ddb.pass.
 */
public final class DatabaseConnection {

    private static final String URL = System.getProperty("db.url",
            "jdbc:mysql://localhost:3306/biblioteca?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Santiago");
    private static final String USER = System.getProperty("db.user", "root");
    private static final String PASS = System.getProperty("db.pass", "1234");

    private static DatabaseConnection instance;   // única instancia
    private Connection connection;

    private DatabaseConnection() throws SQLException {
        connect();
    }

    private void connect() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró MySQL Connector/J en el classpath", e);
        }
        connection = DriverManager.getConnection(URL, USER, PASS);
    }

    /** Acceso global y seguro entre hilos a la única instancia. */
    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /** Devuelve la conexión; si se cerró o cayó, la restablece. */
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(2)) {
            connect();
        }
        return connection;
    }

    /** Cierra la conexión si la instancia existe (usado al salir de la aplicación). */
    public static synchronized void cerrarSiExiste() {
        if (instance != null) instance.close();
    }

    public synchronized void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException ignored) { }
    }
}
