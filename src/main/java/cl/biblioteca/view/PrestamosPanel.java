package cl.biblioteca.view;

import cl.biblioteca.controller.EstudianteController;
import cl.biblioteca.controller.LibroController;
import cl.biblioteca.controller.PrestamoController;
import cl.biblioteca.model.Estudiante;
import cl.biblioteca.model.Libro;
import cl.biblioteca.model.Prestamo;
import cl.biblioteca.model.Usuario;
import cl.biblioteca.service.BibliotecaException;
import cl.biblioteca.service.PrestamoTask;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PrestamosPanel extends PanelBase {
    private final PrestamoController ctrl = new PrestamoController();
    private final EstudianteController estCtrl = new EstudianteController();
    private final LibroController libroCtrl = new LibroController();
    private final Usuario usuario;

    private final JComboBox<Estudiante> cbEstudiante = new JComboBox<>();
    private final JComboBox<Libro> cbLibro = new JComboBox<>();
    private final JButton btnPrestar = new JButton("Registrar préstamo");
    private final JButton btnDevolver = new JButton("Registrar devolución");
    private final JCheckBox chkSoloActivos = new JCheckBox("Solo préstamos pendientes", true);
    private final JLabel lblEstado = new JLabel(" ");
    private final DefaultTableModel model = modelo("ID", "Estudiante", "Libro", "Préstamo", "Vence", "Estado");
    private final JTable tabla = new JTable(model);
    private List<Prestamo> prestamos;

    public PrestamosPanel(Usuario usuario) {
        this.usuario = usuario;

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBorder(BorderFactory.createTitledBorder("Nuevo préstamo"));
        top.add(new JLabel("Estudiante:")); top.add(cbEstudiante);
        top.add(new JLabel("Libro:")); top.add(cbLibro);
        top.add(btnPrestar);
        top.add(lblEstado);
        add(top, BorderLayout.NORTH);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sur.add(chkSoloActivos);
        sur.add(btnDevolver);
        add(sur, BorderLayout.SOUTH);

        btnPrestar.addActionListener(e -> prestar());
        btnDevolver.addActionListener(e -> devolver());
        chkSoloActivos.addActionListener(e -> cargarTabla());
        recargar();
    }

    @Override
    public void recargar() {
        try {
            cbEstudiante.removeAllItems();
            if (usuario.esBibliotecario()) {
                for (Estudiante e : estCtrl.listar()) cbEstudiante.addItem(e);
            } else {
                // El estudiante solo puede operar con su propio registro (se enlaza por RUT).
                Estudiante propio = estCtrl.buscarPorRut(usuario.getRut());
                if (propio != null) cbEstudiante.addItem(propio);
            }
            cbLibro.removeAllItems();
            for (Libro l : libroCtrl.listar()) if (l.getStock() > 0) cbLibro.addItem(l);
        } catch (BibliotecaException e) { error(e.getMessage()); }
        cargarTabla();
    }

    private void cargarTabla() {
        try {
            if (usuario.esBibliotecario()) {
                prestamos = chkSoloActivos.isSelected() ? ctrl.listarActivos() : ctrl.listar();
            } else {
                Estudiante propio = cbEstudiante.getItemCount() > 0 ? cbEstudiante.getItemAt(0) : null;
                prestamos = propio == null ? List.of() : ctrl.historialDe(propio.getId());
                if (chkSoloActivos.isSelected()) prestamos = prestamos.stream().filter(p -> !p.isDevuelto()).toList();
            }
            model.setRowCount(0);
            for (Prestamo p : prestamos) {
                String estado = p.isDevuelto() ? "Devuelto"
                        : p.estaAtrasado() ? "ATRASADO (" + p.diasAtraso() + " días)" : "Vigente";
                model.addRow(new Object[]{p.getId(), p.getEstudiante(), p.getLibro(),
                        p.getFechaPrestamo(), p.getFechaDevolucion(), estado});
            }
        } catch (BibliotecaException e) { error(e.getMessage()); }
    }

    /** El registro se ejecuta en un hilo aparte: la interfaz sigue respondiendo. */
    private void prestar() {
        Estudiante est = (Estudiante) cbEstudiante.getSelectedItem();
        Libro libro = (Libro) cbLibro.getSelectedItem();
        if (est == null || libro == null) { error("Seleccione un estudiante y un libro con stock."); return; }

        btnPrestar.setEnabled(false);
        lblEstado.setText("Registrando préstamo en segundo plano...");
        ctrl.registrarAsync(est.getId(), libro.getId(), new PrestamoTask.Listener() {
            @Override public void onExito(Prestamo p) {
                SwingUtilities.invokeLater(() -> {      // volver al hilo de Swing
                    btnPrestar.setEnabled(true);
                    lblEstado.setText(" ");
                    info("Préstamo registrado. Fecha de vencimiento: " + p.getFechaDevolucion());
                    recargar();
                });
            }
            @Override public void onError(String mensaje) {
                SwingUtilities.invokeLater(() -> {
                    btnPrestar.setEnabled(true);
                    lblEstado.setText(" ");
                    error(mensaje);
                    recargar();
                });
            }
        });
    }

    private void devolver() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { error("Seleccione un préstamo de la tabla."); return; }
        Prestamo p = prestamos.get(fila);
        if (p.isDevuelto()) { error("Ese préstamo ya fue devuelto."); return; }
        try {
            long atraso = ctrl.devolver(p.getId());
            info(atraso > 0
                    ? "Devolución registrada CON ATRASO de " + atraso + " día(s). Queda constancia en el historial."
                    : "Devolución registrada a tiempo.");
            recargar();
        } catch (BibliotecaException e) { error(e.getMessage()); }
    }
}
