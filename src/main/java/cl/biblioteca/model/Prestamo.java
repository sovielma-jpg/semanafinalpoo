package cl.biblioteca.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Prestamo {
    public static final int PLAZO_DIAS = 7;

    private int id;
    private int idEstudiante;
    private int idLibro;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;   // fecha de vencimiento
    private boolean devuelto;
    private String estudiante;           // solo visualización
    private String libro;                // solo visualización

    public Prestamo(int id, int idEstudiante, int idLibro, LocalDate fechaPrestamo,
                    LocalDate fechaDevolucion, boolean devuelto) {
        this.id = id;
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.devuelto = devuelto;
    }

    /** Crea un préstamo nuevo calculando automáticamente el vencimiento. */
    public static Prestamo nuevo(int idEstudiante, int idLibro) {
        LocalDate hoy = LocalDate.now();
        return new Prestamo(0, idEstudiante, idLibro, hoy, hoy.plusDays(PLAZO_DIAS), false);
    }

    /** Días de atraso respecto de la fecha de vencimiento (0 si no hay atraso). */
    public long diasAtraso() {
        long d = ChronoUnit.DAYS.between(fechaDevolucion, LocalDate.now());
        return d > 0 ? d : 0;
    }

    public boolean estaAtrasado() { return !devuelto && diasAtraso() > 0; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdEstudiante() { return idEstudiante; }
    public int getIdLibro() { return idLibro; }
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public boolean isDevuelto() { return devuelto; }
    public void setDevuelto(boolean devuelto) { this.devuelto = devuelto; }
    public String getEstudiante() { return estudiante; }
    public void setEstudiante(String estudiante) { this.estudiante = estudiante; }
    public String getLibro() { return libro; }
    public void setLibro(String libro) { this.libro = libro; }
}
