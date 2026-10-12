package cl.biblioteca.view;

import cl.biblioteca.controller.EstudianteController;
import cl.biblioteca.controller.PrestamoController;
import cl.biblioteca.model.Estudiante;
import cl.biblioteca.model.Prestamo;
import cl.biblioteca.service.BibliotecaException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ReportesPanel extends PanelBase {
    private final PrestamoController ctrl = new PrestamoController();
    private final EstudianteController estCtrl = new EstudianteController();
    private final JComboBox<Estudiante> cbEstudiante = new JComboBox<>();
    private final DefaultTableModel model = modelo("Columna");
    private final JTable tabla = new JTable(model);
    private final JLabel lblTitulo = new JLabel("Seleccione un reporte");

    public ReportesPanel() {
        JButton btnMasPrestados = new JButton("Libros más prestados");
        JButton btnHistorial = new JButton("Historial por estudiante");
        JButton btnEnPrestamo = new JButton("Libros actualmente en préstamo");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(btnMasPrestados);
        top.add(btnEnPrestamo);
        top.add(new JLabel("Estudiante:"));
        top.add(cbEstudiante);
        top.add(btnHistorial);
        add(top, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(4, 4));
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 14f));
        centro.add(lblTitulo, BorderLayout.NORTH);
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        btnMasPrestados.addActionListener(e -> masPrestados());
        btnEnPrestamo.addActionListener(e -> enPrestamo());
        btnHistorial.addActionListener(e -> historial());
        recargar();
    }

    @Override
    public void recargar() {
        try {
            cbEstudiante.removeAllItems();
            for (Estudiante e : estCtrl.listar()) cbEstudiante.addItem(e);
        } catch (BibliotecaException e) { error(e.getMessage()); }
    }

    private void masPrestados() {
        try {
            model.setDataVector(new Object[0][0], new Object[]{"Libro", "Veces prestado"});
            for (Object[] fila : ctrl.librosMasPrestados()) model.addRow(fila);
            lblTitulo.setText("Libros más prestados");
        } catch (BibliotecaException e) { error(e.getMessage()); }
    }

    private void enPrestamo() {
        try {
            mostrarPrestamos(ctrl.listarActivos(), "Libros actualmente en préstamo");
        } catch (BibliotecaException e) { error(e.getMessage()); }
    }

    private void historial() {
        Estudiante est = (Estudiante) cbEstudiante.getSelectedItem();
        if (est == null) { error("Seleccione un estudiante."); return; }
        try {
            mostrarPrestamos(ctrl.historialDe(est.getId()), "Historial de " + est.getNombre());
        } catch (BibliotecaException e) { error(e.getMessage()); }
    }

    private void mostrarPrestamos(List<Prestamo> lista, String titulo) {
        model.setDataVector(new Object[0][0], new Object[]{"ID", "Estudiante", "Libro", "Préstamo", "Vence", "Estado"});
        for (Prestamo p : lista) {
            String estado = p.isDevuelto() ? "Devuelto"
                    : p.estaAtrasado() ? "ATRASADO (" + p.diasAtraso() + " días)" : "Vigente";
            model.addRow(new Object[]{p.getId(), p.getEstudiante(), p.getLibro(),
                    p.getFechaPrestamo(), p.getFechaDevolucion(), estado});
        }
        lblTitulo.setText(titulo + " (" + lista.size() + ")");
    }
}
