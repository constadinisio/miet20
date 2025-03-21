package utils;

import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ResponsiveImageLabel extends JLabel {
    private BufferedImage originalImage;
    private String imagePath;
    private boolean maintainAspectRatio = true;
    
    public ResponsiveImageLabel(String imagePath) {
        this.imagePath = imagePath;
        loadImage();
        
        // Añadir un listener para el cambio de tamaño
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                resizeImage();
            }
        });
    }
    
    private void loadImage() {
        try {
            originalImage = ImageIO.read(new File(imagePath));
            setIcon(new ImageIcon(originalImage));
        } catch (IOException e) {
            System.err.println("Error cargando imagen: " + e.getMessage());
        }
    }
    
    public void setMaintainAspectRatio(boolean maintain) {
        this.maintainAspectRatio = maintain;
        resizeImage();
    }
    
    private void resizeImage() {
        if (originalImage == null || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        
        int width = getWidth();
        int height = getHeight();
        
        // Mantener la relación de aspecto si es necesario
        if (maintainAspectRatio) {
            double ratio = (double) originalImage.getWidth() / originalImage.getHeight();
            if (width / ratio > height) {
                width = (int) (height * ratio);
            } else {
                height = (int) (width / ratio);
            }
        }
        
        // Crear la imagen escalada
        Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        setIcon(new ImageIcon(scaledImage));
        repaint();
    }
}