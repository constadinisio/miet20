/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.utils;

import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * Clase de utilidad para manejar imágenes responsivas en componentes JLabel.
 * Proporciona métodos para cargar y redimensionar automáticamente imágenes
 * cuando el componente contenedor cambia de tamaño.
 */
public class ResponsiveImageUtils {
    
    /**
     * Configura una imagen en un JLabel que se redimensionará automáticamente
     * cuando el componente cambie de tamaño.
     * 
     * @param label JLabel donde se mostrará la imagen
     * @param imagePath Ruta de la imagen a cargar
     */
    public static void setResponsiveImage(JLabel label, String imagePath) {
        if (label == null) {
            System.err.println("Error: El label es nulo");
            return;
        }
        
        try {
            // Verificar que la imagen existe
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                System.err.println("Error: La imagen no existe en la ruta especificada: " + imagePath);
                return;
            }
            
            // Cargar la imagen inicial
            ImageIcon icon = new ImageIcon(imagePath);
            if (icon.getIconWidth() <= 0) {
                System.err.println("Error: No se pudo cargar la imagen: " + imagePath);
                return;
            }
            
            // Guardar la ruta de la imagen como propiedad del label para usarla más tarde
            label.putClientProperty("imagePath", imagePath);
            
            // Añadir un listener para redimensionar la imagen cuando el tamaño del label cambie
            label.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    resizeImageToFit(label);
                }
                
                @Override
                public void componentShown(ComponentEvent e) {
                    resizeImageToFit(label);
                }
            });
            
            // Hacer el redimensionado inicial
            resizeImageToFit(label);
            
        } catch (Exception e) {
            System.err.println("Error configurando imagen responsiva: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Redimensiona la imagen para que se ajuste al tamaño actual del JLabel
     * 
     * @param label JLabel que contiene la imagen
     */
    private static void resizeImageToFit(JLabel label) {
        try {
            // Obtener la ruta de la imagen guardada como propiedad
            String imagePath = (String) label.getClientProperty("imagePath");
            if (imagePath == null) {
                return;
            }
            
            // Obtener las dimensiones actuales del label
            int width = label.getWidth();
            int height = label.getHeight();
            
            // Solo redimensionar si el componente tiene tamaño válido
            if (width <= 0 || height <= 0) {
                return;
            }
            
            // Cargar la imagen original
            ImageIcon originalIcon = new ImageIcon(imagePath);
            Image originalImage = originalIcon.getImage();
            
            // Redimensionar manteniendo la proporción (si se desea)
            boolean maintainRatio = true;
            Image resizedImage;
            
            if (maintainRatio) {
                // Calcular la proporción para mantener el aspect ratio
                double widthRatio = (double) width / originalIcon.getIconWidth();
                double heightRatio = (double) height / originalIcon.getIconHeight();
                
                // Usar la proporción más pequeña para evitar distorsiones
                double ratio = Math.min(widthRatio, heightRatio);
                
                int newWidth = (int) (originalIcon.getIconWidth() * ratio);
                int newHeight = (int) (originalIcon.getIconHeight() * ratio);
                
                resizedImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            } else {
                // Redimensionar a las dimensiones exactas del label
                resizedImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            }
            
            // Actualizar el JLabel con la imagen redimensionada
            label.setIcon(new ImageIcon(resizedImage));
            
        } catch (Exception e) {
            System.err.println("Error al redimensionar la imagen: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Refresca la imagen y fuerza un redimensionado
     * 
     * @param label JLabel cuya imagen se desea refrescar
     */
    public static void refreshImage(JLabel label) {
        if (label != null) {
            resizeImageToFit(label);
        }
    }
    
    /**
     * Configura una imagen en un JLabel y establece un tamaño preferido
     * 
     * @param label JLabel donde se mostrará la imagen
     * @param imagePath Ruta de la imagen a cargar
     * @param width Ancho preferido
     * @param height Alto preferido
     */
    public static void setResponsiveImageWithSize(JLabel label, String imagePath, int width, int height) {
        // Establecer tamaño preferido
        label.setSize(width, height);
        label.setPreferredSize(new java.awt.Dimension(width, height));
        
        // Configurar imagen responsiva
        setResponsiveImage(label, imagePath);
    }
}