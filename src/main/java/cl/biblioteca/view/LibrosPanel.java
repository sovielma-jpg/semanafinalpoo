package cl.biblioteca.view;

import cl.biblioteca.controller.LibroController;
import cl.biblioteca.model.Categoria;
import cl.biblioteca.model.Libro;
import cl.biblioteca.service.BibliotecaException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LibrosPanel extends PanelBase {
    private final LibroController ctrl = new LibroController();
    private final boolean puedeEditar;
    private final DefaultTableModel model = modelo("ID", "Título", "Autor", "ISBN", "Editorial", "Categoría", "Stock");
    private final JTable tabla = new JTable(model);
    private final JTextField txtBuscar = new JTextField(20);
    private final JTextField txtTitulo = new JTextField(), txtAutor = new JTextField(),
            txtIsbn = new JTextField(), txtEditorial = new JTextField();
    private final JSpinner spStock = new JSpinner(new SpinnerNumberModel(1, 0, 9999, 1));
    private final JComboBox<Categoria> cbCategoria = new JComboBox<>();
    private List<Libro> libros;
    private int idSeleccionado = 0;

    public LibrosPanel(boolean puedeEditar) {
        this.puedeEditar = puedeEditar;

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnBuscar = new JButton("Buscar");
        JButton btnTodos = new JButton("Ver todos");
        top.add(new JLabel("Buscar (título, autor o ISBN):"));
        top.add(txtBuscar);
        top.add(btnBuscar);
        top.add(btnTodos);
        add(top, BorderLayout.NORTH);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> cargar(txtBuscar.getText()));
        btnTodos.addActionListener(e -> { txtBuscar.setText(""); cargar(""); });

        if (puedeEditar) {
            JPanel form = new JPanel(new GridLayout(0, 4, 6, 6));
            form.setBorder(BorderFactory.createTitledBorder("Datos del libro (solo bibliotecario)"));
            form.add(new JLabel("Título")); form.add(txtTitulo);
            form.add(new JLabel("Autor")); form.add(txtAutor);
            form.add(new JLabel("ISBN")); form.add(txtIsbn);
            form.add(new JLabel("Editorial")); form.add(txtEditorial);
            form.add(new JLabel("Categoría")); form.add(cbCategoria);
            form.add(new JLabel("Stock")); form.add(spStock);

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
        }
        recargar();
    }

    @Override
    public void recargar() {
        try {
            if (puedeEditar) {
                cbCategoria.removeAllItems();
                for (Categoria c : ctrl.categorias()) cbCategoria.addItem(c);
            }
        } catch (BibliotecaException e) { error(e.getMessage()); }
        cargar(txtBuscar.getText());
    }

    private void cargar(String filtro) {
        try {
            libros = ctrl.buscar(filtro);
            model.setRowCount(0);
            for (Libro l : libros) {
                model.addRow(new Object[]{l.getId(), l.getTitulo(), l.getAutor(), l.getIsbn(),
                        l.getEditorial(), l.getCategoria(), l.getStock()});
            }
        } catch (BibliotecaException e) { error(e.getMessage()); }
    }

    private void seleccionar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        int id = (int) model.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        for (Libro l : libros) {
            if (l.getId() == id) {
                idSeleccionado = id;
                txtTitulo.setText(l.getTitulo()); txtAutor.setText(l.getAutor());
                txtIsbn.setText(l.getIsbn()); txtEditorial.setText(l.getEditorial());
                spStock.setValue(l.getStock());
                for (int i = 0; i < cbCategoria.getItemCount(); i++)
                    if (cbCategoria.getItemAt(i).getId() == l.getIdCategoria()) cbCategoria.setSelectedIndex(i);
            }
        }
    }

    private void guardar() {
        Categoria cat = (Categoria) cbCategoria.getSelectedItem();
        Libro l = new Libro(idSeleccionado, txtTitulo.getText().trim(), txtAutor.getText().trim(),
                txtIsbn.getText().trim(), txtEditorial.getText().trim(),
                (Integer) spStock.getValue(), cat == null ? 0 : cat.getId());
        try {
            ctrl.guardar(l);
            info(idSeleccionado == 0 ? "Libro registrado." : "Libro modificado.");
            limpiar();
            cargar("");
        } catch (BibliotecaException e) { error(e.getMessage()); }
    }

    private void eliminar() {
        if (idSeleccionado == 0) { error("Seleccione un libro de la tabla."); return; }
        if (!confirmar("¿Eliminar el libro seleccionado?")) return;
        try {
            ctrl.eliminar(idSeleccionado);
            limpiar();
            cargar("");
        } catch (BibliotecaException e) { error(e.getMessage()); }
    }

    private void limpiar() {
        idSeleccionado = 0;
        txtTitulo.setText(""); txtAutor.setText(""); txtIsbn.setText(""); txtEditorial.setText("");
        spStock.setValue(1);
        tabla.clearSelection();
    }
}
