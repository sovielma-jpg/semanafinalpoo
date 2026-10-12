package cl.biblioteca.view;

import cl.biblioteca.config.DatabaseConnection;
import cl.biblioteca.model.Usuario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Ventana principal (JFrame) con menú y panel de navegación por pestañas según el rol. */
public class MainFrame extends JFrame {

    public MainFrame(Usuario usuario) {
        super("Biblioteca Escolar - " + usuario.getNombre() + " [" + usuario.getRol() + "]");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(980, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Libros", new LibrosPanel(usuario.esBibliotecario()));
        if (usuario.esBibliotecario()) tabs.addTab("Estudiantes", new EstudiantesPanel());
        tabs.addTab("Préstamos y devoluciones", new PrestamosPanel(usuario));
        if (usuario.esBibliotecario()) tabs.addTab("Reportes", new ReportesPanel());
        tabs.addChangeListener(e -> ((PanelBase) tabs.getSelectedComponent()).recargar());

        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("Sistema");
        JMenuItem cerrar = new JMenuItem("Cerrar sesión");
        JMenuItem salir = new JMenuItem("Salir");
        cerrar.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });
        salir.addActionListener(e -> salir());
        menu.add(cerrar);
        menu.add(salir);
        bar.add(menu);
        setJMenuBar(bar);

        add(tabs, BorderLayout.CENTER);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { salir(); }
        });
    }

    private void salir() {
        DatabaseConnection.cerrarSiExiste();
        System.exit(0);
    }
}
