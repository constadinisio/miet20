/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package main.java.views.users.Attp.menu;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import main.java.database.Conexion;
import main.java.views.users.common.VentanaInicio;

/**
 *
 * @author nico_
 */
public class NetbookRegistrationPanel extends JPanel {

    // Conexión a la base de datos
    private Connection conect;

    // Referencia a la ventana principal
    private VentanaInicio ventanaPrincipal;

    // ID del usuario ATTP
    private int attpId;

    // Componentes de la interfaz
    private javax.swing.JButton btnRegistrar;
    private javax.swing.JButton btnVolver;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JComboBox<String> comboCarrito;
    private javax.swing.JComboBox<String> comboNumeroNetbook;
    private javax.swing.JTextField txtNumeroSerie;
    private javax.swing.JTextField txtMarca;
    private javax.swing.JTextField txtModelo;
    private javax.swing.JTextField txtProcesador;
    private javax.swing.JTextField txtMemoria;
    private javax.swing.JTextField txtDiscoDuro;
    private javax.swing.JTextField txtFechaAdquisicion;
    private javax.swing.JComboBox<String> comboEstado;
    private javax.swing.JTextArea txtObservaciones;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tablaNetbooks;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblCarrito;
    private javax.swing.JLabel lblNumeroNetbook;
    private javax.swing.JLabel lblNumeroSerie;
    private javax.swing.JLabel lblMarca;
    private javax.swing.JLabel lblModelo;
    private javax.swing.JLabel lblProcesador;
    private javax.swing.JLabel lblMemoria;
    private javax.swing.JLabel lblDiscoDuro;
    private javax.swing.JLabel lblFechaAdquisicion;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblObservaciones;

    /**
     * Constructor del panel para registro de netbooks.
     *
     * @param ventanaPrincipal Referencia a la ventana principal para navegación
     * @param attpId ID del usuario ATTP
     */
    public NetbookRegistrationPanel(VentanaInicio ventanaPrincipal, int attpId) {
        this.ventanaPrincipal = ventanaPrincipal;
        this.attpId = attpId;
        initComponents();
        probar_conexion();
        cargarTablaNetbooks();
        inicializarComboBoxes();

        // Establecer fecha actual por defecto
        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        txtFechaAdquisicion.setText(fechaActual.format(formateadorFecha));
    }

    /**
     * Verifica la conexión a la base de datos.
     */
    private void probar_conexion() {
        conect = Conexion.getInstancia().verificarConexion();
        if (conect == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.");
        }
    }

    /**
     * Inicializa los combobox con sus valores.
     */
    private void inicializarComboBoxes() {
        // Inicializar combobox de carritos (letras A a Z)
        String[] carritos = new String[26];
        for (int i = 0; i < 26; i++) {
            carritos[i] = String.valueOf((char) ('A' + i));
        }
        comboCarrito.setModel(new DefaultComboBoxModel<>(carritos));

        // Inicializar combobox de números de netbook (1 a 30)
        String[] numeros = new String[30];
        for (int i = 0; i < 30; i++) {
            numeros[i] = String.valueOf(i + 1);
        }
        comboNumeroNetbook.setModel(new DefaultComboBoxModel<>(numeros));

        // Inicializar combobox de estados
        comboEstado.setModel(new DefaultComboBoxModel<>(new String[]{"En uso", "Dañada", "Hurto", "Obsoleta"}));
    }

    /**
     * Carga la tabla de netbooks desde la base de datos.
     */
    private void cargarTablaNetbooks() {
        probar_conexion();
        DefaultTableModel modeloTabla = new DefaultTableModel();
        modeloTabla.addColumn("ID");
        modeloTabla.addColumn("Carrito");
        modeloTabla.addColumn("Número");
        modeloTabla.addColumn("N° Serie");
        modeloTabla.addColumn("Marca");
        modeloTabla.addColumn("Modelo");
        modeloTabla.addColumn("Estado");

        tablaNetbooks.setModel(modeloTabla);

        Statement stmt;
        String[] datos = new String[7];

        try {
            stmt = conect.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM netbooks ORDER BY carrito, numero");

            while (rs.next()) {
                datos[0] = rs.getString("id");
                datos[1] = rs.getString("carrito");
                datos[2] = rs.getString("numero");
                datos[3] = rs.getString("numero_serie");
                datos[4] = rs.getString("marca");
                datos[5] = rs.getString("modelo");
                datos[6] = rs.getString("estado");

                modeloTabla.addRow(datos);
            }

            tablaNetbooks.setModel(modeloTabla);

        } catch (SQLException e) {
            Logger.getLogger(NetbookRegistrationPanel.class.getName()).log(Level.SEVERE, null, e);
            JOptionPane.showMessageDialog(this, "Error al cargar datos de netbooks: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
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

    /**
     * Maneja la acción del botón volver para regresar al panel principal.
     */
    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {
        ventanaPrincipal.restaurarVistaPrincipal();
    }

    /**
     * Maneja la acción del botón limpiar para resetear el formulario.
     */
    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {
        limpiarFormulario();
    }

    /**
     * Limpia todos los campos del formulario.
     */
    private void limpiarFormulario() {
        comboCarrito.setSelectedIndex(0);
        comboNumeroNetbook.setSelectedIndex(0);
        txtNumeroSerie.setText("");
        txtMarca.setText("");
        txtModelo.setText("");
        txtProcesador.setText("");
        txtMemoria.setText("");
        txtDiscoDuro.setText("");

        // Establecer fecha actual
        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        txtFechaAdquisicion.setText(fechaActual.format(formateadorFecha));

        comboEstado.setSelectedIndex(0);
        txtObservaciones.setText("");
    }

    /**
     * Maneja la acción del botón registrar para guardar una nueva netbook.
     */
    private void btnRegistrarActionPerformed(java.awt.event.ActionEvent evt) {
        // Validar que los campos obligatorios no estén vacíos
        if (txtNumeroSerie.getText().trim().isEmpty()
                || txtMarca.getText().trim().isEmpty()
                || txtModelo.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Los campos Número de Serie, Marca y Modelo son obligatorios",
                    "Error de validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verificar si ya existe una netbook con este carrito y número
        String carrito = comboCarrito.getSelectedItem().toString();
        String numero = comboNumeroNetbook.getSelectedItem().toString();

        try {
            PreparedStatement checkPs = conect.prepareStatement(
                    "SELECT COUNT(*) FROM netbooks WHERE carrito = ? AND numero = ?");
            checkPs.setString(1, carrito);
            checkPs.setString(2, numero);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                int opcion = JOptionPane.showConfirmDialog(this,
                        "Ya existe una netbook registrada con este carrito y número. ¿Desea actualizarla?",
                        "Netbook existente", JOptionPane.YES_NO_OPTION);

                if (opcion == JOptionPane.YES_OPTION) {
                    // Actualizar netbook existente
                    actualizarNetbook(carrito, numero);
                }
                return;
            }

            // Si no existe, registrar nueva netbook
            registrarNetbook();

        } catch (SQLException e) {
            Logger.getLogger(NetbookRegistrationPanel.class.getName()).log(Level.SEVERE, null, e);
            JOptionPane.showMessageDialog(this, "Error al verificar netbook existente: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Registra una nueva netbook en la base de datos.
     */
    private void registrarNetbook() {
        try {
            // Registrar en el archivo de log el estado antes de insertar
            registrarLogInicial();

            // Preparar la inserción
            PreparedStatement ps = conect.prepareStatement(
                    "INSERT INTO netbooks (carrito, numero, numero_serie, marca, modelo, "
                    + "procesador, memoria, disco_duro, fecha_adquisicion, estado, observaciones) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            ps.setString(1, comboCarrito.getSelectedItem().toString());
            ps.setString(2, comboNumeroNetbook.getSelectedItem().toString());
            ps.setString(3, txtNumeroSerie.getText().trim());
            ps.setString(4, txtMarca.getText().trim());
            ps.setString(5, txtModelo.getText().trim());
            ps.setString(6, txtProcesador.getText().trim());
            ps.setString(7, txtMemoria.getText().trim());
            ps.setString(8, txtDiscoDuro.getText().trim());
            ps.setString(9, txtFechaAdquisicion.getText().trim());
            ps.setString(10, comboEstado.getSelectedItem().toString());
            ps.setString(11, txtObservaciones.getText().trim());

            int resultado = ps.executeUpdate();

            if (resultado > 0) {
                JOptionPane.showMessageDialog(this, "Netbook registrada con éxito",
                        "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);

                // Registrar en log el estado después de insertar
                registrarLogFinal();

                // Limpiar formulario y recargar tabla
                limpiarFormulario();
                cargarTablaNetbooks();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo registrar la netbook",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            Logger.getLogger(NetbookRegistrationPanel.class.getName()).log(Level.SEVERE, null, e);
            JOptionPane.showMessageDialog(this, "Error al registrar netbook: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Actualiza una netbook existente en la base de datos.
     *
     * @param carrito Carrito al que pertenece la netbook
     * @param numero Número de la netbook dentro del carrito
     */
    private void actualizarNetbook(String carrito, String numero) {
        try {
            // Registrar en el archivo de log el estado antes de actualizar
            registrarLogInicial();

            // Preparar la actualización
            PreparedStatement ps = conect.prepareStatement(
                    "UPDATE netbooks SET numero_serie = ?, marca = ?, modelo = ?, "
                    + "procesador = ?, memoria = ?, disco_duro = ?, fecha_adquisicion = ?, "
                    + "estado = ?, observaciones = ? WHERE carrito = ? AND numero = ?");

            ps.setString(1, txtNumeroSerie.getText().trim());
            ps.setString(2, txtMarca.getText().trim());
            ps.setString(3, txtModelo.getText().trim());
            ps.setString(4, txtProcesador.getText().trim());
            ps.setString(5, txtMemoria.getText().trim());
            ps.setString(6, txtDiscoDuro.getText().trim());
            ps.setString(7, txtFechaAdquisicion.getText().trim());
            ps.setString(8, comboEstado.getSelectedItem().toString());
            ps.setString(9, txtObservaciones.getText().trim());
            ps.setString(10, carrito);
            ps.setString(11, numero);

            int resultado = ps.executeUpdate();

            if (resultado > 0) {
                JOptionPane.showMessageDialog(this, "Netbook actualizada con éxito",
                        "Actualización exitosa", JOptionPane.INFORMATION_MESSAGE);

                // Registrar en log el estado después de actualizar
                registrarLogFinal();

                // Limpiar formulario y recargar tabla
                limpiarFormulario();
                cargarTablaNetbooks();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar la netbook",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            Logger.getLogger(NetbookRegistrationPanel.class.getName()).log(Level.SEVERE, null, e);
            JOptionPane.showMessageDialog(this, "Error al actualizar netbook: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Maneja la acción del botón eliminar para borrar una netbook seleccionada.
     */
    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {
        int filaSeleccionada = tablaNetbooks.getSelectedRow();

        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una netbook de la tabla para eliminar",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar esta netbook? Esta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (opcion == JOptionPane.YES_OPTION) {
            String idNetbook = tablaNetbooks.getValueAt(filaSeleccionada, 0).toString();

            try {
                // Registrar en log antes de eliminar
                registrarLogInicial();

                // Ejecutar eliminación
                PreparedStatement ps = conect.prepareStatement("DELETE FROM netbooks WHERE id = ?");
                ps.setString(1, idNetbook);

                int resultado = ps.executeUpdate();

                if (resultado > 0) {
                    JOptionPane.showMessageDialog(this, "Netbook eliminada con éxito",
                            "Eliminación exitosa", JOptionPane.INFORMATION_MESSAGE);

                    // Registrar en log después de eliminar
                    registrarLogFinal();

                    // Recargar tabla
                    cargarTablaNetbooks();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar la netbook",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException e) {
                Logger.getLogger(NetbookRegistrationPanel.class.getName()).log(Level.SEVERE, null, e);
                JOptionPane.showMessageDialog(this, "Error al eliminar netbook: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Registra en el archivo de log el estado inicial antes de una operación.
     */
    private void registrarLogInicial() {
        try {
            Statement statement = conect.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM netbooks");

            try (FileWriter escritor = new FileWriter("Registros.txt", true)) {
                escritor.write("\n");
                escritor.write("--- ESTADO INICIAL DE NETBOOKS ANTES DE OPERACIÓN ---\n");

                while (resultSet.next()) {
                    String id = resultSet.getString("id");
                    String carrito = resultSet.getString("carrito");
                    String numero = resultSet.getString("numero");
                    String numeroSerie = resultSet.getString("numero_serie");
                    String marca = resultSet.getString("marca");
                    String modelo = resultSet.getString("modelo");
                    String estado = resultSet.getString("estado");

                    escritor.write("Netbook: " + id + "|" + carrito + "-" + numero + "|"
                            + numeroSerie + "|" + marca + "|" + modelo + "|" + estado + "\n");
                }
            }
        } catch (SQLException | IOException e) {
            Logger.getLogger(NetbookRegistrationPanel.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Registra en el archivo de log el estado final después de una operación.
     */
    private void registrarLogFinal() {
        try {
            Statement statement = conect.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM netbooks");

            try (FileWriter escritor = new FileWriter("Registros.txt", true)) {
                escritor.write("\n");
                escritor.write("--- ESTADO FINAL DE NETBOOKS DESPUÉS DE OPERACIÓN ---\n");

                while (resultSet.next()) {
                    String id = resultSet.getString("id");
                    String carrito = resultSet.getString("carrito");
                    String numero = resultSet.getString("numero");
                    String numeroSerie = resultSet.getString("numero_serie");
                    String marca = resultSet.getString("marca");
                    String modelo = resultSet.getString("modelo");
                    String estado = resultSet.getString("estado");

                    escritor.write("Netbook: " + id + "|" + carrito + "-" + numero + "|"
                            + numeroSerie + "|" + marca + "|" + modelo + "|" + estado + "\n");
                }

                escritor.write("--- FIN DE OPERACIÓN ---\n");
            }
        } catch (SQLException | IOException e) {
            Logger.getLogger(NetbookRegistrationPanel.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
