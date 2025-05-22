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
            JOptionPane.showMessageDialog(ventana,
                    "Error al procesar la acción: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            ventana.restaurarVistaPrincipal();
        }
    }

    /**
     * Muestra el panel de stock.
     */
    private void mostrarPanelStock() {
        try {
            // Obtener panel principal
            JPanel panelPrincipal = ventana.getPanelPrincipal();
            panelPrincipal.removeAll();
            panelPrincipal.setLayout(new BorderLayout());

            // Crear instancia del panel Stock
            StockPanel panel = new StockPanel(ventana, attpId);

            // Añadir al panel principal
            panelPrincipal.add(panel, BorderLayout.CENTER);

            // Actualizar vista
            panelPrincipal.revalidate();
            panelPrincipal.repaint();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar panel de stock: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ventana.restaurarVistaPrincipal();
        }
    }

    /**
     * Muestra el panel de préstamos.
     */
    private void mostrarPanelPrestamos() {
        try {
            // Obtener panel principal
            JPanel panelPrincipal = ventana.getPanelPrincipal();
            panelPrincipal.removeAll();
            panelPrincipal.setLayout(new BorderLayout());

            // Crear instancia del panel Prestamos
            PrestamosPanel panel = new PrestamosPanel(ventana, attpId);

            // Añadir al panel principal
            panelPrincipal.add(panel, BorderLayout.CENTER);

            // Actualizar vista
            panelPrincipal.revalidate();
            panelPrincipal.repaint();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar panel de préstamos: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ventana.restaurarVistaPrincipal();
        }
    }

    /**
     * Muestra el panel de registros.
     */
    private void mostrarPanelRegistros() {
        try {
            // Obtener panel principal
            JPanel panelPrincipal = ventana.getPanelPrincipal();
            panelPrincipal.removeAll();
            panelPrincipal.setLayout(new BorderLayout());

            // Crear instancia del panel Registros
            RegistrosPanel panel = new RegistrosPanel(ventana, attpId);

            // Añadir al panel principal
            panelPrincipal.add(panel, BorderLayout.CENTER);

            // Actualizar vista
            panelPrincipal.revalidate();
            panelPrincipal.repaint();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar panel de registros: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ventana.restaurarVistaPrincipal();
        }
    }

    /**
     * Muestra el panel de registro de netbooks.
     */
    private void mostrarPanelNetbooks() {
        try {
            // Obtener panel principal
            JPanel panelPrincipal = ventana.getPanelPrincipal();
            panelPrincipal.removeAll();
            panelPrincipal.setLayout(new BorderLayout());

            // Crear instancia del panel de registro de netbooks
            NetbookRegistrationPanel panel = new NetbookRegistrationPanel(ventana, attpId);

            // Añadir al panel principal
            panelPrincipal.add(panel, BorderLayout.CENTER);

            // Actualizar vista
            panelPrincipal.revalidate();
            panelPrincipal.repaint();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(ventana,
                    "Error al cargar panel de registro de netbooks: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ventana.restaurarVistaPrincipal();
        }
    }
}
