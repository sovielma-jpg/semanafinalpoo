package cl.biblioteca.service;

import cl.biblioteca.model.Prestamo;

/**
 * HILO independiente: ejecuta en segundo plano el registro del préstamo, la actualización
 * del stock y el registro del historial, para que la interfaz (hilo de eventos de Swing)
 * no se congele. El resultado se informa mediante un listener; la vista lo pasa al hilo
 * de Swing con SwingUtilities.invokeLater.
 */
public class PrestamoTask implements Runnable {

    public interface Listener {
        void onExito(Prestamo prestamo);
        void onError(String mensaje);
    }

    /** Pausa opcional (ms) para evidenciar en el video que la interfaz no se bloquea. 0 = sin pausa. */
    public static final long DEMO_DELAY_MS = Long.getLong("prestamo.delay", 800L);

    private final int idEstudiante;
    private final int idLibro;
    private final Listener listener;

    public PrestamoTask(int idEstudiante, int idLibro, Listener listener) {
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
        this.listener = listener;
    }

    /** Crea y arranca el hilo. */
    public static Thread iniciar(int idEstudiante, int idLibro, Listener listener) {
        Thread t = new Thread(new PrestamoTask(idEstudiante, idLibro, listener), "hilo-prestamo");
        t.setDaemon(true);
        t.start();
        return t;
    }

    @Override
    public void run() {
        try {
            if (DEMO_DELAY_MS > 0) Thread.sleep(DEMO_DELAY_MS);
            Prestamo p = PrestamoService.getInstance().registrarPrestamo(idEstudiante, idLibro);
            listener.onExito(p);
        } catch (BibliotecaException e) {
            listener.onError(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            listener.onError("Operación interrumpida.");
        }
    }
}
