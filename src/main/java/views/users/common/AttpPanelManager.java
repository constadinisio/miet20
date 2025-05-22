/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.views.users.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import main.java.views.users.Attp.menu.NetbookRegistrationPanel;
import main.java.views.users.Attp.menu.PrestamosPanel;
import main.java.views.users.Attp.menu.RegistrosPanel;
import main.java.views.users.Attp.menu.StockPanel;

/**
 * Gestor de paneles específico para el rol de ATTP.
 * Versión actualizada que usa el sistema responsive.
 */
public class AttpPanelManager implements RolPanelManager {

    private final VentanaInicio ventana;
    private final int attpId;

    /**
     * Constructor del gestor de paneles para ATTP.
     *
     * @param ventana Ventana principal
     * @param userId ID del usuario (ATTP)
     */
    public AttpPanelManager(VentanaInicio ventana, int userId) {
        this.ventana = ventana;
        this.attpId = userId;
    }

    @Override
    public JComponent[] createButtons() {
        // Crear botones específicos para el rol de ATTP
        JButton btnStock = createStyledButton("STOCK", "stock");
        JButton btnPrestamos = createStyledButton("PRÉSTAMOS", "prestamos");
        JButton btnRegistros = createStyledButton("REGISTROS", "registros");
        JButton btnNetbooks = createStyledButton("NETBOOKS", "netbooks");

        // Retornar array de botones
        return new JComponent[]{
            btnStock,
            btnPrestamos,
            btnRegistros,
            btnNetbooks
        };
    }

    /**
     * Crea un botón con el estilo estándar de la aplicación.
     *
     * @param text Texto del botón
     * @param actionCommand Comando de acción para identificar el botón
     * @return Botón configurado
     */
    private JButton createStyledButton(String text, String actionCommand) {
        JButton button = new JButton(text);
        button.setBackground(new Color(51, 153, 255));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setActionCommand(actionCommand);
        button.addActionListener(e -> handleButtonAction(e.getActionCommand()));

        // Establecer dimensiones preferidas
        button.setMaximumSize(new Dimension(195, 40));
        button.setPreferredSize(new Dimension(195, 40));

        return button;
    }

    @Override
    public void handleButtonAction(String actionCommand) {
        try {
            System.out.println("Acción ATTP: " + actionCommand);
            
            switch (actionCommand) {
                case "stock":
                    mostrarPanelStock();
                    break;
                case "prestamos":
                    mostrarPanelPrestamos();
                    break;
                case "registros":
                    mostrarPanelRegistros();
                    break;
                case "netbooks":
                    mostrarPanelNetbooks();
                    break;
                default:
                    ventana.restaurarVistaPrincipal();
                    break;
            }
        } catch (Exception ex) {
            System.err.println("Error en handleButtonAction ATTP: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al procesar la acción: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ventana.restaurarVistaPrincipal();
        }
    }

    /**
     * Muestra el panel de stock usando el sistema responsive.
     */
    private void mostrarPanelStock() {
        try {
            System.out.println("Creando StockPanel...");
            StockPanel panel = new StockPanel(ventana, attpId);
            
            // Usar el sistema responsive automático
            JPanel panelPrincipal = ventana.getPanelPrincipal();
            panelPrincipal.removeAll();
            panelPrincipal.add(panel, BorderLayout.CENTER);
            panelPrincipal.revalidate();
            panelPrincipal.repaint();
            
            System.out.println("StockPanel cargado exitosamente");

        } catch (Exception ex) {
            System.err.println("Error al cargar StockPanel: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar panel de stock: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ventana.restaurarVistaPrincipal();
        }
    }

    /**
     * Muestra el panel de préstamos usando el sistema responsive.
     */
    private void mostrarPanelPrestamos() {
        try {
            System.out.println("Creando PrestamosPanel...");
            PrestamosPanel panel = new PrestamosPanel(ventana, attpId);
            
            // Usar el sistema responsive automático
            JPanel panelPrincipal = ventana.getPanelPrincipal();
            panelPrincipal.removeAll();
            panelPrincipal.add(panel, BorderLayout.CENTER);
            panelPrincipal.revalidate();
            panelPrincipal.repaint();
            
            System.out.println("PrestamosPanel cargado exitosamente");

        } catch (Exception ex) {
            System.err.println("Error al cargar PrestamosPanel: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar panel de préstamos: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ventana.restaurarVistaPrincipal();
        }
    }

    /**
     * Muestra el panel de registros usando el sistema responsive.
     */
    private void mostrarPanelRegistros() {
        try {
            System.out.println("Creando RegistrosPanel...");
            RegistrosPanel panel = new RegistrosPanel(ventana, attpId);
            
            // Usar el sistema responsive automático
            JPanel panelPrincipal = ventana.getPanelPrincipal();
            panelPrincipal.removeAll();
            panelPrincipal.add(panel, BorderLayout.CENTER);
            panelPrincipal.revalidate();
            panelPrincipal.repaint();
            
            System.out.println("RegistrosPanel cargado exitosamente");

        } catch (Exception ex) {
            System.err.println("Error al cargar RegistrosPanel: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar panel de registros: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ventana.restaurarVistaPrincipal();
        }
    }

    /**
     * Muestra el panel de registro de netbooks usando el sistema responsive.
     */
    private void mostrarPanelNetbooks() {
        try {
            System.out.println("Creando NetbookRegistrationPanel...");
            NetbookRegistrationPanel panel = new NetbookRegistrationPanel(ventana, attpId);
            
            // Usar el sistema responsive automático
            JPanel panelPrincipal = ventana.getPanelPrincipal();
            panelPrincipal.removeAll();
            panelPrincipal.add(panel, BorderLayout.CENTER);
            panelPrincipal.revalidate();
            panelPrincipal.repaint();
            
            System.out.println("NetbookRegistrationPanel cargado exitosamente");

        } catch (Exception ex) {
            System.err.println("Error al cargar NetbookRegistrationPanel: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar panel de registro de netbooks: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ventana.restaurarVistaPrincipal();
        }
    }
}