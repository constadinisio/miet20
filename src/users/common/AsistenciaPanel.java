package users.common;


import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import login.Conexion;


import javax.swing.table.DefaultTableCellRenderer;

/**
 * Clase abstracta base para paneles de gestión de asistencia.
 * 
 * Características principales:
 * - Proporciona estructura común para paneles de asistencia
 * - Define métodos abstractos para cargar y guardar asistencias
 * - Gestiona colores y renderizado de estados de asistencia
 * 
 * @author [Nicolas Bogarin]
 * @version 1.0
 * @since [13/03/2025]
 */
public abstract class AsistenciaPanel extends JPanel {
   
    // Conexión a base de datos
    protected Connection conect;
    
    // Identificador del usuario
    protected int usuarioId;
    
    // Fecha de asistencia
    protected LocalDate fecha;
    
    // Tabla de asistencia
    protected JTable tablaAsistencia;
    
    // Modelo de tabla
    protected DefaultTableModel tableModel;
    
    // Mapa de colores para estados de asistencia
    protected Map<String, Color> colorEstados;
    
    /**
     * Constructor por defecto.
     * Inicializa los colores de los estados de asistencia.
     */
    public AsistenciaPanel(){
    super();
    inicializarColores();
    }
    
    /**
     * Inicializa el mapa de colores para los diferentes estados de asistencia.
     * 
     * Estados definidos:
     * - "P": Verde claro (Presente)
     * - "A": Rojo claro (Ausente)
     * - "T": Amarillo claro (Tarde)
     * - "AP": Naranja claro (Ausente con Permiso)
     * - "NC": Blanco (No Corresponde)
     */
    private void inicializarColores(){
    colorEstados = new HashMap<>();
        colorEstados.put("P", new Color(144, 238, 144)); 
        colorEstados.put("A", new Color(255, 182, 193));
        colorEstados.put("T", new Color(255, 255, 153)); 
        colorEstados.put("AP", new Color(255, 218, 185));
        colorEstados.put("NC", Color.WHITE);           
    }
    
    /**
     * Configura la estructura básica de la tabla de asistencia.
     * 
     * Pasos:
     * - Crear modelo de tabla si no existe
     * - Añadir columnas estándar: Alumno, DNI, Estado
     * - Establecer modelo en la tabla
     */
    protected void configurarTabla(){
    if (tableModel == null){
    tableModel= new DefaultTableModel();
    }
    tableModel.addColumn("Alumno");
    tableModel.addColumn("DNI");
    tableModel.addColumn("Estado");
    
    if (tablaAsistencia != null){
        tablaAsistencia.setModel(tableModel);
    }
    
    }
    /**
     * Método abstracto para cargar asistencias.
     * Debe ser implementado por las subclases.
     */
    protected abstract void cargarAsistencias();
    
    /**
     * Método abstracto para guardar asistencias.
     * Debe ser implementado por las subclases.
     */
    protected abstract void guardarAsistencias();
    
    /**
     * Método abstracto para determinar si una celda puede ser editada.
     * 
     * @param row Fila de la celda
     * @param column Columna de la celda
     * @return true si la celda puede ser editada, false en caso contrario
     */
    protected abstract boolean puedeEditarCelda(int row, int column);
    
    /**
     * Inicializa la base del panel de asistencia.
     * 
     * @param usuarioId Identificador del usuario
     */
    protected void inicializarBase(int usuarioId) {
        this.usuarioId = usuarioId;
        this.fecha = LocalDate.now();
        this.tableModel = new DefaultTableModel();
        if (tablaAsistencia != null) {
            tablaAsistencia.setModel(tableModel);
        }
        // Inicializar conexión
        this.conect = Conexion.getInstancia().getConexion();
        if (this.conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.");
            return;
        }
        inicializarColores();
        configurarTabla();
    }
    
    /**
     * Inicializa componentes básicos.
     * Obtiene conexión y inicializa colores.
     */
    private void inicializarComponentes() {
        conect = Conexion.getInstancia().getConexion();
        inicializarColores();
    }
    
    /**
     * Clase interna para renderizado personalizado de celdas de asistencia.
     * Aplica colores según el estado de asistencia.
     */
    protected class AsistenciaCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected && column >= 2) {
                String estado = value != null ? value.toString() : "NC";
                c.setBackground(colorEstados.getOrDefault(estado, Color.WHITE));
            }
            
            return c;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
