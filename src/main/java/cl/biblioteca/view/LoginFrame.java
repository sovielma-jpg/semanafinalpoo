package cl.biblioteca.view;

import cl.biblioteca.controller.AuthController;
import cl.biblioteca.model.Usuario;
import cl.biblioteca.service.BibliotecaException;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final AuthController auth = new AuthController();
    private final JTextField txtUsuario = new JTextField(18);
    private final JPasswordField txtClave = new JPasswordField(18);

    public LoginFrame() {
        super("Biblioteca Escolar - Ingreso");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.anchor = GridBagConstraints.WEST;

        JLabel titulo = new JLabel("Sistema de Gestión de Biblioteca Escolar");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; form.add(titulo, g);

        g.gridwidth = 1;
        g.gridy = 1; g.gridx = 0; form.add(new JLabel("Correo o RUT:"), g);
        g.gridx = 1; form.add(txtUsuario, g);
        g.gridy = 2; g.gridx = 0; form.add(new JLabel("Contraseña:"), g);
        g.gridx = 1; form.add(txtClave, g);

        JButton btn = new JButton("Ingresar");
        g.gridy = 3; g.gridx = 1; g.anchor = GridBagConstraints.EAST; form.add(btn, g);

        btn.addActionListener(e -> ingresar());
        txtClave.addActionListener(e -> ingresar());
        getRootPane().setDefaultButton(btn);

        setContentPane(form);
        pack();
        setLocationRelativeTo(null);
    }

    private void ingresar() {
        try {
            Usuario u = auth.login(txtUsuario.getText(), new String(txtClave.getPassword()));
            new MainFrame(u).setVisible(true);
            dispose();
        } catch (BibliotecaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo ingresar", JOptionPane.ERROR_MESSAGE);
        }
    }
}
