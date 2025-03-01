package Client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Properties;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import Ressources.Databright;
import Ressources.DataConvolution;
import Ressources.DataGray;
import Ressources.DataNoise;
import Ressources.DataResult;
import Ressources.Datainvert;
import Ressources.DataSapia;
import java.util.Iterator;

/* 
 Cette classe gère entièrement le processus de traitement d'images via un serveur, avec une interface utilisateur permettant de charger, afficher, traiter et sauvegarder des images.
 */

public class Client extends JFrame {

    private static JLabel originalImageLabel;
    private static JLabel filteredImageLabel;
    private static JComboBox<String> tasksComboBox;
    private static JFrame F;
    static BufferedImage originalImage;
    static BufferedImage filteredImage;
    static String value;
    static String task;
    static int[][] kernel = new int[3][3];

    static int MainServer_port;
    static String MainServer_host;

    public static void main(String[] args) {

        Properties prop = new Properties();
        FileInputStream ip;
        String FileConfiguration = "cfgClient.properties";
        if (args.length > 0)
            FileConfiguration = args[0];
        try {
            ip = new FileInputStream(FileConfiguration);
            prop.load(ip);
        } catch (Exception e2) {
            System.exit(0);
        }

        MainServer_port = Integer.parseInt(prop.getProperty("MainServer.port"));
        MainServer_host = prop.getProperty("MainServer.host");

        F = new JFrame();

        F.setTitle("Image Processing");
        F.setSize(1370, 703);
        F.setDefaultCloseOperation(EXIT_ON_CLOSE);
        F.setExtendedState(JFrame.MAXIMIZED_BOTH);
        F.setLayout(new BorderLayout());

        // Panel for images
        JPanel imagesPanel = new JPanel(new GridBagLayout());
        imagesPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // Add margins
        imagesPanel.setBackground(new Color(240, 240, 240)); // Light gray background

        originalImageLabel = new JLabel();
        filteredImageLabel = new JLabel();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10); // Add spacing
        imagesPanel.add(originalImageLabel, gbc);

        gbc.gridx = 1;
        imagesPanel.add(filteredImageLabel, gbc);

        F.add(imagesPanel, BorderLayout.CENTER);

        // Toolbar for buttons
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(new Color(200, 200, 200)); // Gray background
        toolBar.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add margins

        JButton chooseImageButton = new JButton("Choose Image");
        chooseImageButton.setIcon(new ImageIcon("path/to/icon.png")); // Add an icon
        chooseImageButton.setPreferredSize(new Dimension(150, 40)); // Agrandir la taille du bouton
        chooseImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(F);
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        originalImage = ImageIO.read(fileChooser.getSelectedFile());
                        int newWidth = 640;
                        int newHeight = 640;
                        Image resizedImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                        originalImageLabel.setIcon(new ImageIcon(resizedImage));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        toolBar.add(chooseImageButton);

        tasksComboBox = new JComboBox<>(
                new String[] { "CONVOLUTION FILTER", "NOISE SALT AND PEPPER", "GRAY FILTER", "SEPIA FILTER",
                        "INVERT FILTER", "BRIGHTNESS FILTER" });
        tasksComboBox.setPreferredSize(new Dimension(200, 40)); // Diminuer la longueur du bouton des filtres
        toolBar.add(tasksComboBox);

        tasksComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (originalImage == null) {
                    JOptionPane.showMessageDialog(F, "Please select an image.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                task = (String) tasksComboBox.getSelectedItem();
                if (task.equals("CONVOLUTION FILTER")) {
                    JTextField[][] textFields = new JTextField[3][3];
                    JPanel panel = new JPanel();
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

                    for (int i = 0; i < 3; i++) {
                        JPanel rowPanel = new JPanel();
                        for (int j = 0; j < 3; j++) {
                            textFields[i][j] = new JTextField(5);
                            rowPanel.add(textFields[i][j]);
                        }
                        panel.add(rowPanel);
                    }

                    int result = JOptionPane.showConfirmDialog(null, panel, "Enter Matrix",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (result == JOptionPane.OK_OPTION) {
                        for (int i = 0; i < 3; i++) {
                            for (int j = 0; j < 3; j++) {
                                kernel[i][j] = Integer.parseInt(textFields[i][j].getText());
                            }
                        }
                    }
                }

                if (task.equals("NOISE SALT AND PEPPER")) {
                    value = JOptionPane.showInputDialog(F, "Enter level of noise ex : {0.02}:");
                    System.out.println(value);
                }
                if (task.equals("BRIGHTNESS FILTER")) {
                    value = JOptionPane.showInputDialog(F, "Enter level of BRIGHTNESS ex :{100} :");
                    System.out.println(value);
                }
            }
        });

        JButton processImageButton = new JButton("Process Image");
        processImageButton.setIcon(new ImageIcon("path/to/icon.png")); // Add an icon
        processImageButton.setPreferredSize(new Dimension(150, 40)); // Agrandir la taille du bouton
        toolBar.add(processImageButton);

        // Add Download Filtered Image Button
        JButton downloadFilteredImageButton = new JButton("Download Filtered Image");
        downloadFilteredImageButton.setIcon(new ImageIcon("path/to/icon.png")); // Add an icon
        downloadFilteredImageButton.setPreferredSize(new Dimension(200, 40)); // Set button size
        toolBar.add(downloadFilteredImageButton);

        F.add(toolBar, BorderLayout.NORTH);
        F.setVisible(true);

        processImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (originalImage == null) {
                    JOptionPane.showMessageDialog(F, "Please select an image.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    Socket socket = new Socket(MainServer_host, MainServer_port);
                    ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                    String state = (String) input.readObject();
                    int[][] img = bufferedImageToIntArray(originalImage);
                    if (state.compareToIgnoreCase("active") == 0) {
                        output.writeObject(task);

                        if (task.compareToIgnoreCase("CONVOLUTION FILTER") == 0) {
                            DataConvolution dataConvolution = new DataConvolution(img, kernel);
                            output.writeObject(dataConvolution);
                        }
                        if (task.compareToIgnoreCase("NOISE SALT AND PEPPER") == 0) {
                            DataNoise dataNoise = new DataNoise(img, Double.parseDouble(value));
                            output.writeObject(dataNoise);
                        }
                        if (task.compareToIgnoreCase("GRAY FILTER") == 0) {
                            DataGray dataGray = new DataGray(img);
                            output.writeObject(dataGray);
                        }
                        if (task.compareToIgnoreCase("SEPIA FILTER") == 0) {
                            DataSapia sapil = new DataSapia(img);
                            output.writeObject(sapil);
                        }
                        if (task.compareToIgnoreCase("INVERT FILTER") == 0) {
                            Datainvert inverse = new Datainvert(img);
                            output.writeObject(inverse);
                        }
                        if (task.compareToIgnoreCase("BRIGHTNESS FILTER") == 0) {
                            Databright dataNoise = new Databright(img, Double.parseDouble(value));
                            output.writeObject(dataNoise);
                        }

                        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                        DataResult pixels = (DataResult) in.readObject();
                        filteredImage = intArrayToBufferedImage(pixels.image);
                        int newWidth = 640;
                        int newHeight = 640;
                        Image resizedImage = filteredImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                        filteredImageLabel.setIcon(new ImageIcon(resizedImage));
                    }
                    socket.close();
                } catch (Exception r) {
                    System.out.println(r.getMessage());
                }
            }
        });

        // Add ActionListener for Download Filtered Image Button

        // Modifier l'ActionListener du bouton "Download Filtered Image"
        downloadFilteredImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (filteredImage == null) {
                    JOptionPane.showMessageDialog(F, "No filtered image to download.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save Filtered Image");
                int userSelection = fileChooser.showSaveDialog(F);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    // Ajouter l'extension .jpg si elle n'est pas déjà présente
                    String filePath = fileToSave.getAbsolutePath();
                    if (!filePath.toLowerCase().endsWith(".jpg")) {
                        fileToSave = new File(filePath + ".jpg");
                    }

                    try {
                        // Obtenir un writer pour le format JPEG
                        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
                        ImageWriter writer = writers.next();

                        // Configurer les paramètres d'écriture (compression JPEG)
                        ImageWriteParam writeParam = writer.getDefaultWriteParam();
                        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                        writeParam.setCompressionQuality(1.0f); // Qualité maximale (1.0 = 100%)

                        // Écrire l'image dans le fichier
                        FileImageOutputStream output = new FileImageOutputStream(fileToSave);
                        writer.setOutput(output);
                        writer.write(null, new IIOImage(filteredImage, null, null), writeParam);

                        // Fermer les ressources
                        writer.dispose();
                        output.close();

                        JOptionPane.showMessageDialog(F, "Image saved successfully as JPG!", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(F, "Error saving image.", "Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    public static int[][] bufferedImageToIntArray(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] result = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                result[y][x] = image.getRGB(x, y);
            }
        }
        return result;
    }

    public static BufferedImage intArrayToBufferedImage(int[][] pixels) {
        int height = pixels.length;
        int width = pixels[0].length;
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                result.setRGB(x, y, pixels[y][x]);
            }
        }
        return result;
    }
}