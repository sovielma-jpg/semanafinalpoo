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

| Criterio de la rúbrica | Dónde está |
|---|---|
| POO: herencia, polimorfismo, clase abstracta, interfaz | `Persona` (abstracta, `getTipo()` polimórfico), `Usuario`/`Estudiante`, `CrudDAO<T>`, `PanelBase` |
| Patrones MVC / DAO / Singleton | paquetes `view`-`controller`-`model`, paquete `dao`, `DatabaseConnection.getInstance()` |
| Concurrencia y sincronización | `PrestamoTask` (hilo `hilo-prestamo`), `PrestamoService` con métodos `synchronized`, `UPDATE ... WHERE stock > 0` y transacción commit/rollback |
| CRUD | Libros, Estudiantes (+ usuario) y Préstamos |
| JFrame | `LoginFrame`, `MainFrame` con menú y pestañas por rol |
| Conexión JDBC + Singleton | `DatabaseConnection` con manejo de `SQLException` |

## Reglas de negocio
- Plazo de préstamo: 7 días (`Prestamo.PLAZO_DIAS`); `fecha_devolucion` guarda la **fecha de vencimiento**.
- Atraso: se detecta comparando hoy con el vencimiento; se muestra en tablas (“ATRASADO (n días)”)
  y al devolver se informa con constancia.
- Rol estudiante: solo consulta de libros, préstamo y devolución sobre su propio registro (enlazado por RUT).
- Rol bibliotecario: acceso total, incluidos estudiantes y reportes.
- Para evidenciar en el video que la UI no se congela, el hilo espera 800 ms antes de registrar
  (`-Dprestamo.delay=0` lo desactiva).

## Nombres de columnas
Los scripts incluidos usan `titulo` y `contrasena` (sin tilde/ñ). Si tu script oficial usa `título` o `contraseña`,
cambia solo las constantes `COL_TITULO` (`LibroDAO`) y `COL_PASS` (`UsuarioDAO`).

## Prueba de concurrencia (Paso 4)
Con un libro de stock 1 (p. ej. “Veinte poemas de amor”), registra dos préstamos seguidos desde dos
estudiantes: el segundo debe rechazarse con “El libro no tiene stock disponible” y el stock nunca queda negativo.

## Guion sugerido del video (3–5 min, rostro visible)
1. (0:00) Presentación breve y caso.
2. (0:30) Login con ambos roles y diferencias de menú.
3. (1:15) CRUD de libros y estudiantes; reportes.
4. (2:15) Préstamo con hilo (interfaz activa mientras corre) y devolución con atraso.
5. (3:15) IDE: `DatabaseConnection` (Singleton), DAO, MVC, `PrestamoTask` y `synchronized`; justificación.
6. (4:30) Cierre.

## Subir a GitHub
```
git init
git add .
git commit -m "EFT S9: Biblioteca Escolar (MVC, DAO, Singleton, hilos)"
git branch -M main
git remote add origin https://github.com/<tu-usuario>/biblioteca-escolar.git
git push -u origin main
```
Se recomienda hacer varios commits descriptivos (modelo, DAO, vistas, hilos) para la trazabilidad.
