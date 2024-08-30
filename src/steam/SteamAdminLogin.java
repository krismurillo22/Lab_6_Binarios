package steam;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import steam.Usuario;

public class SteamAdminLogin extends JFrame {

    private JTextField userTextField;
    private JPasswordField passwordField;
    private static ArrayList<Usuario> usuarios = new ArrayList<>();
    private Steam steam;

    public SteamAdminLogin() {
        try {
            steam = new Steam();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al inicializar Steam: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        try {
            usuarios.add(new Usuario(steam.getNextPlayerCode(), "admin", "hello", "Admin", new Date(), "admin"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al agregar el usuario admin: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        setTitle("Admin Login - Steam");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2, 10, 10));
        add(panel);

        JLabel userLabel = new JLabel("Usuario:");
        userTextField = new JTextField();
        JLabel passwordLabel = new JLabel("Contraseña:");
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("Iniciar sesión");
        loginButton.addActionListener(new LoginButtonListener());

        JButton registerButton = new JButton("Registrarse");
        registerButton.addActionListener(new RegisterButtonListener());

        JButton exitButton = new JButton("Salir");
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(userLabel);
        panel.add(userTextField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);
        panel.add(exitButton);
    }

    private class LoginButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String user = userTextField.getText();
            String password = new String(passwordField.getPassword());

            if (autenticarUsuario(user, password) || steam.existePlayer(user, password)) {
                Usuario loggedInUser = getUsuario(user);
                JOptionPane.showMessageDialog(null, "Inicio de sesión exitoso", "Bienvenido", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new Main(loggedInUser).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "Usuario o contraseña incorrectos", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private boolean autenticarUsuario(String user, String password) {
            for (Usuario usuario : usuarios) {
                if (usuario.getNombreUsuario().equals(user) && usuario.getContrasena().equals(password)) {
                    return true;
                }
            }
            return false;
        }

        private Usuario getUsuario(String username) {
            for (Usuario usuario : usuarios) {
                if (usuario.getNombreUsuario().equals(username)) {
                    return usuario;
                }
            }
            return null;
        }
    }

    private class RegisterButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            dispose(); // Cerrar la ventana de login
            new RegisterUserFrame(SteamAdminLogin.this, steam);
        }
    }

    private class RegisterUserFrame extends JFrame {

        private JTextField newUserTextField;
        private JPasswordField newPasswordField;
        private JTextField nameTextField;
        private JSpinner birthdateSpinner;

        private Steam steam;
        public RegisterUserFrame(JFrame loginFrame, Steam steam) {
            this.steam = steam;
            setTitle("Registro de Usuario");
            setSize(300, 240);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(6, 2, 10, 10));
            add(panel);

            JLabel newUserLabel = new JLabel("Usuario:");
            newUserTextField = new JTextField();
            JLabel newPasswordLabel = new JLabel("Contraseña:");
            newPasswordField = new JPasswordField();
            JLabel nameLabel = new JLabel("Nombre:");
            nameTextField = new JTextField();
            JLabel birthdateLabel = new JLabel("Fecha de Nacimiento:");
            birthdateSpinner = new JSpinner(new SpinnerDateModel());
            JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(birthdateSpinner, "dd/MM/yyyy");
            birthdateSpinner.setEditor(dateEditor);
            JLabel imagen = new JLabel("Selecciona tu foto:");
            JButton imagenboton = new JButton("Agregar");
            
            imagenboton.addActionListener(ex -> {
                JFileChooser fileChooser = new JFileChooser();
                int option = fileChooser.showOpenDialog(this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    ImageIcon profilePicture = new ImageIcon(file.getPath());
                }
            });
            JButton registerButton = new JButton("Registrar");
            registerButton.addActionListener(new RegisterUserListener());

            JButton backButton = new JButton("Regresar");
            backButton.addActionListener(e -> {
                dispose();
                loginFrame.setVisible(true);
            });

            panel.add(newUserLabel);
            panel.add(newUserTextField);
            panel.add(newPasswordLabel);
            panel.add(newPasswordField);
            panel.add(nameLabel);
            panel.add(nameTextField);
            panel.add(birthdateLabel);
            panel.add(birthdateSpinner);
            panel.add(imagen);
            panel.add(imagenboton);
            panel.add(registerButton);
            panel.add(backButton);

            setVisible(true);
        }

        private class RegisterUserListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                String newUser = newUserTextField.getText();
                String newPassword = new String(newPasswordField.getPassword());
                String name = nameTextField.getText();
                Date birthdate = (Date) birthdateSpinner.getValue();

                if (newUser.isEmpty() || newPassword.isEmpty() || name.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Todos los campos son obligatorios", "Error", JOptionPane.ERROR_MESSAGE);
                } else if (usuarioExiste(newUser)) {
                    JOptionPane.showMessageDialog(null, "El usuario ya existe", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        usuarios.add(new Usuario(steam.getNextPlayerCode(), newUser, newPassword, name, birthdate, "standard"));
                        steam.addPlayer(newUser, newPassword, name, "standard");
                        JOptionPane.showMessageDialog(null, "Registro exitoso", "Usuario registrado", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                        new SteamAdminLogin().setVisible(true);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Error al registrar el usuario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    
                }
            }

            private boolean usuarioExiste(String username) {
                for (Usuario usuario : usuarios) {
                    if (usuario.getNombreUsuario().equals(username)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SteamAdminLogin loginFrame = new SteamAdminLogin();
            loginFrame.setVisible(true);
        });
    }
}
