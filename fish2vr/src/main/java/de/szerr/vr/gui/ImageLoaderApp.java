package de.szerr.vr.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DnDConstants;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.List;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

public class ImageLoaderApp extends JFrame {

    private JLabel imageLabel1 = new JLabel();
    private JLabel imageLabel2 = new JLabel();
    private JTextArea imageInfo1 = new JTextArea();
    private JTextArea imageInfo2 = new JTextArea();

    public ImageLoaderApp() {
        setTitle("Image Loader");
        setSize(800, 600);
        setLayout(new GridLayout(1, 2));
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel1 = new JPanel(new BorderLayout());
        panel1.add(imageLabel1, BorderLayout.CENTER);
        panel1.add(new JScrollPane(imageInfo1), BorderLayout.SOUTH);
        
        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(imageLabel2, BorderLayout.CENTER);
        panel2.add(new JScrollPane(imageInfo2), BorderLayout.SOUTH);

        add(panel1);
        add(panel2);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem openLeftItem = new JMenuItem("Load Left Image");
        openLeftItem.addActionListener(e -> loadImage(imageLabel1, imageInfo1));
        
        JMenuItem openRightItem = new JMenuItem("Load Right Image");
        openRightItem.addActionListener(e -> loadImage(imageLabel2, imageInfo2));
        
        fileMenu.add(openLeftItem);
        fileMenu.add(openRightItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        
        enableDragAndDrop(imageLabel1, imageInfo1);
        enableDragAndDrop(imageLabel2, imageInfo2);
    }

    private void enableDragAndDrop(JLabel imageLabel, JTextArea imageInfo) {
        new DropTarget(imageLabel, new DropTargetListener() {
            @Override
            public void drop(DropTargetDropEvent event) {
                try {
                    event.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!droppedFiles.isEmpty()) {
                        File file = droppedFiles.get(0); // Only take the first file
                        displayImageAndInfo(file, imageLabel, imageInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Implement other necessary methods with empty bodies
            @Override public void dragEnter(java.awt.dnd.DropTargetDragEvent dtde) {}
            @Override public void dragOver(java.awt.dnd.DropTargetDragEvent dtde) {}
            @Override public void dropActionChanged(java.awt.dnd.DropTargetDragEvent dtde) {}
            @Override public void dragExit(java.awt.dnd.DropTargetEvent dte) {}
        });
    }
    private void loadImage(JLabel imageLabel, JTextArea imageInfo) {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            displayImageAndInfo(file, imageLabel, imageInfo);
        }
    }
    private void displayImageAndInfo(File file, JLabel imageLabel, JTextArea imageInfo) {
        try {
            BufferedImage image = ImageIO.read(file);
            ImageIcon icon = new ImageIcon(image.getScaledInstance(400, -1, Image.SCALE_SMOOTH));
            imageLabel.setIcon(icon);

            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            Metadata metadata;
			try {
				metadata = ImageMetadataReader.readMetadata(file);
				 ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
				 StringBuilder info = new StringBuilder();
				 info.append("Name: ").append(file).append("\n");
		            info.append("Size: ").append(file.length() / 1024).append(" KB\n");
		            info.append("Time: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(attrs.lastModifiedTime().toMillis())).append("\n");
		            info.append("Resolution: ").append(image.getWidth()).append(" x ").append(image.getHeight()).append("\n");

		            if (directory != null) {
		                directory.getTags().forEach(tag -> info.append(tag.getTagName()).append(": ").append(tag.getDescription()).append("\n"));
		            }
		            imageInfo.setText(info.toString());
			} catch (ImageProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
           

           

          
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ImageLoaderApp().setVisible(true));
    }
}
