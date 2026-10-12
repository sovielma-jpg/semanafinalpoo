# Sistema de Gestión de Biblioteca Escolar

EFT Semana 9 – Desarrollo Orientado a Objetos II (PRY2203).
Aplicación de escritorio en **Java 17 + Swing (JFrame) + JDBC + MySQL**.

## Requisitos
- JDK 17 o superior, IntelliJ IDEA, MySQL 8
- MySQL Connector/J 8.x (lo descarga Maven automáticamente desde `pom.xml`)

## Puesta en marcha
1. En MySQL ejecuta, en este orden:
   - `sql/Script_crea_tablas_biblioteca.sql`
   - `sql/Script_poblado_tablas_biblioteca.sql`
   (si usas los scripts oficiales del AVA, revisa la sección *Nombres de columnas*).
2. Abre la carpeta en IntelliJ IDEA (**File > Open** sobre `pom.xml`, *Open as Project*).
3. Ajusta usuario/clave de MySQL en `config/DatabaseConnection.java`
   o al ejecutar con `-Ddb.user=... -Ddb.pass=... -Ddb.url=...`.
4. Ejecuta la clase `cl.biblioteca.Main`.
5. Ejecutable: `mvn package` genera `target/biblioteca-escolar-1.0-jar-with-dependencies.jar`
   → `java -jar target/biblioteca-escolar-1.0-jar-with-dependencies.jar`

## Usuarios de prueba
| Rol | Usuario (correo o RUT) | Contraseña |
|---|---|---|
| Bibliotecario | cmunoz@colegio.cl / 11111111-1 | admin123 |
| Estudiante | asoto@colegio.cl / 33333333-3 | est123 |

Los estudiantes nuevos se crean con usuario propio (rol ESTUDIANTE) y clave inicial = su RUT.

## Arquitectura
```
cl.biblioteca
├── config/      DatabaseConnection   -> Singleton (JDBC, DriverManager.getConnection)
├── model/       Persona (abstracta) <- Usuario, Estudiante ; Libro, Categoria, Prestamo
├── dao/         CrudDAO<T> + CategoriaDAO, LibroDAO, EstudianteDAO, UsuarioDAO, PrestamoDAO
├── service/     PrestamoService (synchronized + transacción), PrestamoTask (hilo)
├── controller/  Auth, Libro, Estudiante, Prestamo (intermediarios vista-modelo)
└── view/        PanelBase (abstracta), LoginFrame, MainFrame, Libros/Estudiantes/Prestamos/Reportes
```

