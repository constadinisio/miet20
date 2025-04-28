package main.java.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * Panel que muestra una imagen de banner que se ajusta automáticamente
 * al ancho de la pantalla.
 */
public class ResponsiveBannerPanel extends JPanel {
    private Image bannerImage;
    private int preferredHeight = 30; // Altura por defecto del banner
    
    /**
     * Constructor que crea un panel con una imagen que se redimensiona automáticamente.
     * 
     * @param imagePath Ruta de la imagen a mostrar
     */
    public ResponsiveBannerPanel(String imagePath) {
        setLayout(new BorderLayout());
        loadImage(imagePath);
        
        // Establecer la altura preferida
        setPreferredSize(new Dimension(0, preferredHeight));
        
        // Agregar listener de redimensionamiento
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repaint();
            }
        });
    }
    
    /**
     * Carga la imagen desde la ruta especificada.
     * 
     * @param imagePath Ruta de la imagen
     */
    private void loadImage(String imagePath) {
        try {
            // Cargar imagen original
            ImageIcon icon = new ImageIcon(imagePath);
            bannerImage = icon.getImage();
            
            // Si la imagen no tiene la altura deseada, ajustar la altura preferida
            if (icon.getIconHeight() > 0) {
                preferredHeight = icon.getIconHeight();
                setPreferredSize(new Dimension(0, preferredHeight));
            }
        } catch (Exception e) {
            System.err.println("Error cargando imagen de banner: " + e.getMessage());
        }
    }
    
    /**
     * Establece una nueva imagen para el banner.
     * 
     * @param imagePath Ruta de la nueva imagen
     */
    public void setImage(String imagePath) {
        loadImage(imagePath);
        repaint();
    }
    
    /**
     * Establece la altura preferida del banner.
     * 
     * @param height Altura en píxeles
     */
    public void setPreferredHeight(int height) {
        this.preferredHeight = height;
        setPreferredSize(new Dimension(0, preferredHeight));
        revalidate();
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (bannerImage != null) {
            // Dibujar la imagen escalada para que cubra todo el ancho
            g.drawImage(bannerImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}