package steam;

import javax.swing.*;
import java.io.*;
import java.awt.Image;
import java.text.SimpleDateFormat;
import java.util.*;
import steam.Usuario;

public class Steam {

    private RandomAccessFile codesFile;
    private RandomAccessFile gamesFile;
    private RandomAccessFile playersFile;

    public Steam() {
        try {
            File steamFolder = new File("steam");
            if (!steamFolder.exists()) {
                steamFolder.mkdir();
            }

            File downloadsFolder = new File(steamFolder, "downloads");
            if (!downloadsFolder.exists()) {
                downloadsFolder.mkdir();
            }

            codesFile = new RandomAccessFile(new File(steamFolder, "codes.stm"), "rw");
            gamesFile = new RandomAccessFile(new File(steamFolder, "games.stm"), "rw");
            playersFile = new RandomAccessFile(new File(steamFolder, "player.stm"), "rw");

            if (codesFile.length() == 0) {
                codesFile.writeInt(1); // Código para juegos
                codesFile.writeInt(1); // Código para clientes
                codesFile.writeInt(1); // Código para downloads
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getNextCode(int offset) throws IOException {
        codesFile.seek(offset);
        int nextCode = codesFile.readInt();
        codesFile.seek(offset);
        codesFile.writeInt(nextCode + 1);
        return nextCode;
    }

    public int getNextGameCode() throws IOException {
        return getNextCode(0);
    }

    public int getNextPlayerCode() throws IOException {
        return getNextCode(4);
    }

    public int getNextDownloadCode() throws IOException {
        return getNextCode(8);
    }

    public void addGame(String title, char os, int minAge, double price, Image gameImage) throws IOException {
        int code = getNextGameCode(); 
        gamesFile.seek(gamesFile.length()); 
        gamesFile.writeInt(code);
        gamesFile.writeUTF(title);
        gamesFile.writeChar(os);
        gamesFile.writeInt(minAge);
        gamesFile.writeDouble(price);
        gamesFile.writeInt(0);

        byte[] imageBytes = convertImageToBytes(gameImage);
        gamesFile.writeInt(imageBytes.length);
        gamesFile.write(imageBytes);
    }

    public void addPlayer(String username, String password, String name, String userType) throws IOException {
        int code = getNextPlayerCode(); 

        Calendar birthDate = getBirthDateFromUser();
        if (birthDate == null) {
            JOptionPane.showMessageDialog(null, "Fecha de nacimiento no seleccionada, operación cancelada", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        playersFile.seek(playersFile.length());
        playersFile.writeInt(code);
        playersFile.writeUTF(username);
        playersFile.writeUTF(password);
        playersFile.writeUTF(name);
        playersFile.writeLong(birthDate.getTimeInMillis()); 
        playersFile.writeInt(0);
        playersFile.writeUTF(userType);
    }

    public boolean existePlayer(String user, String password){
        try {
            playersFile.seek(0);
            while (playersFile.getFilePointer() < playersFile.length()) {
                int code = playersFile.readInt();
                String storedUsername = playersFile.readUTF();
                String storedPassword = playersFile.readUTF();
                playersFile.readUTF();
                playersFile.readLong();
                playersFile.readInt();
                playersFile.readUTF();

                if (storedUsername.equals(user) && storedPassword.equals(password)) {
                    return true; 
                }
            }
        } catch (IOException e) {
            System.out.println("No busca en archivo");
        }
        return false;
    }

    private Calendar getBirthDateFromUser() {
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy"));

        int option = JOptionPane.showOptionDialog(null, dateSpinner, "Seleccione la fecha de nacimiento",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

        if (option == JOptionPane.OK_OPTION) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime((java.util.Date) dateSpinner.getValue());
            return calendar;
        } else {
            return null; 
        }
    }

    private byte[] convertImageToBytes(Image image) {
        return new byte[0];
    }

    public void closeFiles() throws IOException {
        if (codesFile != null) {
            codesFile.close();
        }
        if (gamesFile != null) {
            gamesFile.close();
        }
        if (playersFile != null) {
            playersFile.close();
        }
    }

    public boolean downloadGame(int gameCode, int clientCode, char os) throws IOException {
        gamesFile.seek(0);
        boolean gameFound = false;
        String gameTitle = null;
        char gameOS = ' ';
        int minAge = 0;
        double price = 0.0;
        int downloadCount = 0;
        byte[] gameImageBytes = null;

        while (gamesFile.getFilePointer() < gamesFile.length()) {
            int currentCode = gamesFile.readInt();
            if (currentCode == gameCode) {
                gameTitle = gamesFile.readUTF();
                gameOS = gamesFile.readChar();
                minAge = gamesFile.readInt();
                price = gamesFile.readDouble();
                downloadCount = gamesFile.readInt();
                int imageLength = gamesFile.readInt();
                gameImageBytes = new byte[imageLength];
                gamesFile.read(gameImageBytes);
                gameFound = true;
                break;
            } else {
                gamesFile.skipBytes(2 + gamesFile.readUTF().length() + 4 + 8 + 4 + gamesFile.readInt());
            }
        }

        if (!gameFound || gameOS != os) {
            return false; 
        }

        playersFile.seek(0);
        boolean clientFound = false;
        String clientName = null;
        long birthDateMillis = 0;
        int clientDownloadCount = 0;

        while (playersFile.getFilePointer() < playersFile.length()) {
            int currentCode = playersFile.readInt();
            if (currentCode == clientCode) {
                clientName = playersFile.readUTF();
                playersFile.skipBytes(playersFile.readUTF().length()); 
                playersFile.skipBytes(playersFile.readUTF().length()); 
                birthDateMillis = playersFile.readLong();
                clientDownloadCount = playersFile.readInt();
                clientFound = true;
                break;
            } else {
                playersFile.skipBytes(2 + playersFile.readUTF().length() + playersFile.readUTF().length() + 8 + 4 + playersFile.readInt());
            }
        }

        if (!clientFound) {
            return false; 
        }

        Calendar birthDate = Calendar.getInstance();
        birthDate.setTimeInMillis(birthDateMillis);
        int clientAge = calculateAge(birthDate);
        if (clientAge < minAge) {
            return false; 
        }

        int downloadCode = getNextDownloadCode();
        File downloadFile = new File("steam/downloads/download_" + downloadCode + ".stm");
        try (FileWriter writer = new FileWriter(downloadFile)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String downloadDate = dateFormat.format(new Date());

            writer.write("[FECHA DE DOWNLOAD: " + downloadDate + "]\n");
            writer.write("IMAGE GAME: [Imagen del juego en bytes]\n");
            writer.write("Download #" + downloadCode + "\n");
            writer.write(clientName + " has bajado " + gameTitle + " a un precio de $ " + price + "\n");
        }

        gamesFile.seek(gamesFile.getFilePointer() - 4); 
        gamesFile.writeInt(downloadCount + 1);

        playersFile.seek(playersFile.getFilePointer() - 4); 
        playersFile.writeInt(clientDownloadCount + 1);

        return true;
    }

    private int calculateAge(Calendar birthDate) {
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }

    public void updatePriceFor(int gameCode, double newPrice) throws IOException {
        gamesFile.seek(0);
        while (gamesFile.getFilePointer() < gamesFile.length()) {
            int currentCode = gamesFile.readInt();
            if (currentCode == gameCode) {
                gamesFile.skipBytes(2 + gamesFile.readUTF().length()); 
                gamesFile.skipBytes(4);
                gamesFile.writeDouble(newPrice); 
                System.out.println("Precio actualizado");
                return;
            } else {
                gamesFile.skipBytes(2 + gamesFile.readUTF().length() + 4 + 8 + 4 + gamesFile.readInt());
            }
        }
        System.out.println("Juego no encontrado");
    }

    public void reportForClient(int codeClient, String txtFile) {
        try {
            playersFile.seek(0);
            boolean clientFound = false;
            String username = null;
            String password = null;
            String name = null;
            long birthDateMillis = 0;
            int downloadCount = 0;
            String userType = null;

            while (playersFile.getFilePointer() < playersFile.length()) {
                int currentCode = playersFile.readInt();
                if (currentCode == codeClient) {
                    username = playersFile.readUTF();
                    password = playersFile.readUTF();
                    name = playersFile.readUTF();
                    birthDateMillis = playersFile.readLong();
                    downloadCount = playersFile.readInt();
                    int imageLength = playersFile.readInt();
                    playersFile.skipBytes(imageLength); 
                    userType = playersFile.readUTF();
                    clientFound = true;
                    break;
                } else {
                    playersFile.skipBytes(2 + playersFile.readUTF().length() + playersFile.readUTF().length() + 8 + 4 + playersFile.readInt());
                }
            }

            if (!clientFound) {
                System.out.println("NO SE PUEDE CREAR REPORTE");
                return;
            }

            File report = new File(txtFile);
            try (FileWriter writer = new FileWriter(report, false)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                String birthDate = dateFormat.format(birthDateMillis);

                writer.write("Código: " + codeClient + "\n");
                writer.write("Usuario: " + username + "\n");
                writer.write("Contraseña: " + password + "\n");
                writer.write("Nombre: " + name + "\n");
                writer.write("Fecha de Nacimiento: " + birthDate + "\n");
                writer.write("Descargas: " + downloadCount + "\n");
                writer.write("Tipo de Usuario: " + userType + "\n");

                System.out.println("REPORTE CREADO");
            }
        } catch (IOException e) {
            System.out.println("NO SE PUEDE CREAR REPORTE");
        }
    }

    public void printGames(JPanel imagesPanel) throws IOException {
        imagesPanel.removeAll(); 

        String[] imagePaths = {
            Ruta.FORTNITE,
            Ruta.MINECRAFT,
            Ruta.PLANTS,
            Ruta.SIMS
        };

        for (String path : imagePaths) {
            ImageIcon imageIcon = new ImageIcon(getClass().getResource(path));
            Image image = imageIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(image));
            imagesPanel.add(imageLabel);
        }

        imagesPanel.revalidate();
        imagesPanel.repaint();
    }

    public List<String> getDownloadedGames(Usuario usuario) throws IOException {
        List<String> downloadedGames = new ArrayList<>();

        playersFile.seek(0);
        while (playersFile.getFilePointer() < playersFile.length()) {
            int currentCode = playersFile.readInt();
            String username = playersFile.readUTF();
            playersFile.readUTF(); 
            playersFile.readUTF(); 
            playersFile.readLong(); 
            int downloadCount = playersFile.readInt(); 

            if (username.equals(usuario.getNombreUsuario())) {
                gamesFile.seek(0);
                while (gamesFile.getFilePointer() < gamesFile.length()) {
                    int gameCode = gamesFile.readInt();
                    String gameTitle = gamesFile.readUTF();
                    gamesFile.readChar(); 
                    gamesFile.readInt();
                    gamesFile.readDouble();
                    int gameDownloadCount = gamesFile.readInt();

                    if (gameDownloadCount > 0) {
                        downloadedGames.add(gameTitle);
                    }

                    int imageLength = gamesFile.readInt();
                    gamesFile.skipBytes(imageLength); 
                }
                break;
            } else {
                playersFile.skipBytes(playersFile.readUTF().length() * 2 + 8 + 4 + playersFile.readInt());
            }
        }

        return downloadedGames;
    }
}
