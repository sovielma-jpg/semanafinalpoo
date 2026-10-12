package cl.biblioteca.view;

import cl.biblioteca.controller.EstudianteController;
import cl.biblioteca.model.Estudiante;
import cl.biblioteca.service.BibliotecaException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EstudiantesPanel extends PanelBase {
    private final EstudianteController ctrl = new EstudianteController();
    private final DefaultTableModel model = modelo("ID", "Nombre", "RUT", "Curso", "Correo");
    private final JTable tabla = new JTable(model);
    private final JTextField txtNombre = new JTextField(), txtRut = new JTextField(),
            txtCurso = new JTextField(), txtCorreo = new JTextField();
    private List<Estudiante> estudiantes;
    private int idSeleccionado = 0;

    public EstudiantesPanel() {
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(0, 4, 6, 6));
        form.setBorder(BorderFactory.createTitledBorder("Datos del estudiante (se crea su usuario con clave = RUT)"));
        form.add(new JLabel("Nombre")); form.add(txtNombre);
        form.add(new JLabel("RUT (12345678-9)")); form.add(txtRut);
        form.add(new JLabel("Curso")); form.add(txtCurso);
        form.add(new JLabel("Correo")); form.add(txtCorreo);

        JButton btnGuardar = new JButton("Guardar (nuevo / modificar)");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnLimpiar); botones.add(btnEliminar); botones.add(btnGuardar);
        JPanel sur = new JPanel(new BorderLayout());
        sur.add(form, BorderLayout.CENTER);
        sur.add(botones, BorderLayout.SOUTH);
        add(sur, BorderLayout.SOUTH);

        tabla.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) seleccionar(); });
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        recargar();
    }

    @Override
    public void recargar() {
        try {
            estudiantes = ctrl.listar();
            model.setRowCount(0);
            for (Estudiante e : estudiantes)
                model.addRow(new Object[]{e.getId(), e.getNombre(), e.getRut(), e.getCurso(), e.getCorreo()});
        } catch (BibliotecaException ex) { error(ex.getMessage()); }
    }

    private void seleccionar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        Estudiante e = estudiantes.get(fila);
        idSeleccionado = e.getId();
        txtNombre.setText(e.getNombre()); txtRut.setText(e.getRut());
        txtCurso.setText(e.getCurso()); txtCorreo.setText(e.getCorreo());
    }

    private void guardar() {
        Estudiante e = new Estudiante(idSeleccionado, txtNombre.getText().trim(), txtRut.getText().trim(),
                txtCurso.getText().trim(), txtCorreo.getText().trim());
        try {
            ctrl.guardar(e);
            info(idSeleccionado == 0 ? "Estudiante registrado." : "Estudiante modificado.");
            limpiar();
            recargar();
        } catch (BibliotecaException ex) { error(ex.getMessage()); }
    }

    private void eliminar() {
        if (idSeleccionado == 0) { error("Seleccione un estudiante de la tabla."); return; }
        if (!confirmar("¿Eliminar al estudiante seleccionado?")) return;
        try {
            ctrl.eliminar(idSeleccionado);
            limpiar();
            recargar();
        } catch (BibliotecaException ex) { error(ex.getMessage()); }
    }

    private void limpiar() {
        idSeleccionado = 0;
        txtNombre.setText(""); txtRut.setText(""); txtCurso.setText(""); txtCorreo.setText("");
        tabla.clearSelection();
    }
}
