package main.java.utils;

import java.sql.*;
import java.io.File;
import java.time.LocalDate;
import javax.swing.JOptionPane;
import main.java.database.Conexion;

/**
 * Clase para inicializar y configurar el sistema de boletines
 */
public class InicializadorBoletines {
    
    /**
     * Configura el sistema de boletines por primera vez
     */
    public static boolean configurarSistemaBoletines(String rutaServidor, String rutaPlantilla) {
        try {
            System.out.println("=== CONFIGURANDO SISTEMA DE BOLETINES ===");
            
            // 1. Configurar servidor
            GestorBoletines.configurarRutaServidor(rutaServidor);
            System.out.println("✅ Servidor configurado: " + rutaServidor);
            
            // 2. Configurar plantilla
            PlantillaBoletinUtility.configurarRutaPlantilla(rutaPlantilla);
            System.out.println("✅ Plantilla configurada: " + rutaPlantilla);
            
            // 3. Verificar que la plantilla existe
            File plantilla = new File(rutaPlantilla);
            if (!plantilla.exists()) {
                System.err.println("⚠️ La plantilla no existe en la ruta especificada");
                return false;
            }
            
            // 4. Generar estructura de carpetas para el año actual
            int anioActual = LocalDate.now().getYear();
            boolean estructuraCreada = GestorBoletines.generarEstructuraCompleta(anioActual);
            
            if (estructuraCreada) {
                System.out.println("✅ Estructura de carpetas creada para " + anioActual);
            } else {
                System.err.println("⚠️ Problemas creando estructura de carpetas");
            }
            
            // 5. Ejecutar verificación completa
            PlantillaBoletinUtility.verificarConfiguracion();
            
            System.out.println("=== CONFIGURACIÓN COMPLETADA ===");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error configurando sistema: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Verifica si el sistema está correctamente configurado
     */
    public static boolean verificarSistemaConfigurado() {
        try {
            // Verificar servidor
            String servidor = GestorBoletines.obtenerRutaServidor();
            if (servidor == null || servidor.isEmpty()) {
                return false;
            }
            
            // Verificar plantilla
            String plantilla = PlantillaBoletinUtility.obtenerRutaPlantilla();
            if (plantilla == null || !new File(plantilla).exists()) {
                return false;
            }
            
            // Verificar conexión BD
            Connection conect = Conexion.getInstancia().verificarConexion();
            if (conect == null) {
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Configura el sistema con interfaz gráfica
     */
    public static void configurarConInterfaz(javax.swing.JComponent parentComponent) {
        try {
            // Panel de configuración
            javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.GridBagLayout());
            java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
            gbc.insets = new java.awt.Insets(5, 5, 5, 5);
            gbc.anchor = java.awt.GridBagConstraints.WEST;
            
            // Ruta del servidor
            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(new javax.swing.JLabel("Ruta del servidor:"), gbc);
            
            javax.swing.JTextField txtServidor = new javax.swing.JTextField(40);
            txtServidor.setText(GestorBoletines.obtenerRutaServidor());
            gbc.gridx = 1;
            panel.add(txtServidor, gbc);
            
            javax.swing.JButton btnExaminarServidor = new javax.swing.JButton("Examinar...");
            gbc.gridx = 2;
            panel.add(btnExaminarServidor, gbc);
            
            // Ruta de la plantilla
            gbc.gridx = 0; gbc.gridy = 1;
            panel.add(new javax.swing.JLabel("Plantilla de boletín:"), gbc);
            
            javax.swing.JTextField txtPlantilla = new javax.swing.JTextField(40);
            txtPlantilla.setText(PlantillaBoletinUtility.obtenerRutaPlantilla());
            gbc.gridx = 1;
            panel.add(txtPlantilla, gbc);
            
            javax.swing.JButton btnExaminarPlantilla = new javax.swing.JButton("Examinar...");
            gbc.gridx = 2;
            panel.add(btnExaminarPlantilla, gbc);
            
            // Listeners para examinar
            btnExaminarServidor.addActionListener(e -> {
                javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
                fileChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setDialogTitle("Seleccionar carpeta del servidor");
                
                if (fileChooser.showOpenDialog(parentComponent) == javax.swing.JFileChooser.APPROVE_OPTION) {
                    txtServidor.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            });
            
            btnExaminarPlantilla.addActionListener(e -> {
                javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos Excel (*.xlsx)", "xlsx"));
                fileChooser.setDialogTitle("Seleccionar plantilla de boletín");
                
                if (fileChooser.showOpenDialog(parentComponent) == javax.swing.JFileChooser.APPROVE_OPTION) {
                    txtPlantilla.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            });
            
            // Mostrar diálogo
            int result = JOptionPane.showConfirmDialog(parentComponent, panel,
                    "Configuración Inicial del Sistema de Boletines",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                String servidor = txtServidor.getText().trim();
                String plantilla = txtPlantilla.getText().trim();
                
                if (!servidor.isEmpty() && !plantilla.isEmpty()) {
                    boolean exito = configurarSistemaBoletines(servidor, plantilla);
                    
                    String mensaje = exito ? 
                        "Sistema de boletines configurado exitosamente" :
                        "Hubo problemas en la configuración";
                    
                    JOptionPane.showMessageDialog(parentComponent,
                            mensaje,
                            "Configuración",
                            exito ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(parentComponent,
                            "Debe especificar tanto la ruta del servidor como la plantilla",
                            "Datos incompletos",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentComponent,
                    "Error en la configuración: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}