package steam;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import steam.Steam;
import steam.SteamAdminLogin;
import steam.Usuario;

public class Main extends JFrame {

    private Steam steam;
    private Usuario loggedInUser;

    public Main(Usuario loggedInUser) {
        this.loggedInUser = loggedInUser;

        try {
            steam = new Steam();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al inicializar Steam: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        setTitle("Catálogo de Juegos - Steam");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel();
        JButton btnAddGame = new JButton("Agregar Juego");
        JButton btnPrintGames = new JButton("Ver Juegos");
        JButton btnDownloadGame = new JButton("Descargar Juego");
        JButton btnUpdatePrice = new JButton("Actualizar Precio");
        JButton btnClientReport = new JButton("Reporte de Cliente");
        JButton btnExit = new JButton("Salir"); 
        JButton btnProfile = new JButton("Mi Perfil");

        topPanel.add(btnAddGame);
        topPanel.add(btnPrintGames);
        topPanel.add(btnDownloadGame);
        topPanel.add(btnUpdatePrice);
        topPanel.add(btnClientReport);
        topPanel.add(btnProfile);
        topPanel.add(btnExit);

        panel.add(topPanel, BorderLayout.NORTH);

        JPanel imagesPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        panel.add(imagesPanel, BorderLayout.CENTER);

        add(panel);

        if (!loggedInUser.getNombreUsuario().equals("admin")) {
            btnAddGame.setVisible(false);
            btnUpdatePrice.setVisible(false);
            btnClientReport.setVisible(false);
        }

        btnExit.addActionListener(e -> {
            dispose();
            new SteamAdminLogin().setVisible(true);
        });

        btnAddGame.addActionListener(e -> {
            try {
                String title = JOptionPane.showInputDialog("Ingrese el título del juego:");
                if (title == null || title.trim().isEmpty()) {
                    return;
                }

                String os = JOptionPane.showInputDialog("Ingrese el sistema operativo (W para Windows, M para Mac, L para Linux):");
                if (os == null || os.trim().isEmpty()) {
                    return;
                }

                int minAge = Integer.parseInt(JOptionPane.showInputDialog("Ingrese la edad mínima:"));
                double price = Double.parseDouble(JOptionPane.showInputDialog("Ingrese el precio:"));
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Selecciona una imagen para el juego");
                int result = fileChooser.showOpenDialog(this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                }
                steam.addGame(title, os.charAt(0), minAge, price, null);
                JOptionPane.showMessageDialog(this, "Juego agregado correctamente.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al agregar el juego: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPrintGames.addActionListener(e -> {
            try {
                imagesPanel.setLayout(new GridLayout(2, 3, 10, 10));
                steam.printGames(imagesPanel);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al listar los juegos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDownloadGame.addActionListener(e -> {
            try {
                int gameCode = Integer.parseInt(JOptionPane.showInputDialog("Ingrese el código del juego:"));
                int clientCode = loggedInUser.getNombreUsuario().equals("admin") ? Integer.parseInt(JOptionPane.showInputDialog("Ingrese el código del cliente:")) : loggedInUser.getCodigo();
                String os = JOptionPane.showInputDialog("Ingrese el sistema operativo (W para Windows, M para Mac, L para Linux):");

                boolean success = steam.downloadGame(gameCode, clientCode, os.charAt(0));
                if (success) {
                    JOptionPane.showMessageDialog(this, "Juego descargado correctamente.");
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo descargar el juego. Verifique los datos.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al descargar el juego: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnUpdatePrice.addActionListener(e -> {
            try {
                int gameCode = Integer.parseInt(JOptionPane.showInputDialog("Ingrese el código del juego:"));
                double newPrice = Double.parseDouble(JOptionPane.showInputDialog("Ingrese el nuevo precio:"));

                steam.updatePriceFor(gameCode, newPrice);
                JOptionPane.showMessageDialog(this, "Precio actualizado correctamente.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al actualizar el precio: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnClientReport.addActionListener(e -> {
            try {
                int clientCode = Integer.parseInt(JOptionPane.showInputDialog("Ingrese el código del cliente:"));
                String filePath = JOptionPane.showInputDialog("Ingrese la ruta del archivo de reporte (incluya .txt):");

                steam.reportForClient(clientCode, filePath);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al generar el reporte: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnProfile.addActionListener(e -> {
            try {
                List<String> downloadedGames = steam.getDownloadedGames(loggedInUser);
                JOptionPane.showMessageDialog(this, "Juegos descargados: " + String.join(", ", downloadedGames));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar el perfil: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SteamAdminLogin loginFrame = new SteamAdminLogin();
            loginFrame.setVisible(true);
        });
    }
}
