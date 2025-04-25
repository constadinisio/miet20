/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.utils;

import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class ResponsiveImageLabel extends JLabel {

    private String imagePath;
    private Image originalImage;

    public ResponsiveImageLabel(String path) {
        super();
        this.imagePath = path;
        loadImage();

        // Añadir un listener de componente para detectar cambios de tamaño
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeImage();
            }
        });
    }

    private void loadImage() {
        try {
            originalImage = ImageIO.read(new File(imagePath));
            resizeImage();
        } catch (IOException e) {
            System.err.println("Error cargando imagen: " + e.getMessage());
        }
    }

    public void refreshImage() {
        loadImage();
    }

    private void resizeImage() {
        if (originalImage != null && getWidth() > 0 && getHeight() > 0) {
            Image scaled = originalImage.getScaledInstance(
                    getWidth(), getHeight(), Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(scaled));
        }
    }
}
