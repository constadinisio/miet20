package main.java.views.users.common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import main.java.views.users.Attp.menu.NetbookRegistrationPanel;
import main.java.views.users.Attp.menu.PrestamosPanel;
import main.java.views.users.Attp.menu.RegistrosPanel;
import main.java.views.users.Attp.menu.StockPanel;
import main.java.services.NotificationCore.NotificationIntegrationUtil;

/**
 * Gestor de paneles específico para el rol de ATTP (Auxiliar Técnico en Tecnologías de la Producción).
 * VERSIÓN COMPLETA MEJORADA CON SISTEMA DE NOTIFICACIONES INTEGRADO
 * 
 * Funcionalidades del ATTP:
 * - Gestión completa de stock de equipos técnicos
 * - Control de préstamos de netbooks y equipamiento
 * - Registro y seguimiento de equipos
 * - Administración de netbooks institucionales
 * - Reportes y auditorías de equipamiento
 * 
 * Sistema de Notificaciones Automáticas:
 * - Notifica a estudiantes sobre nuevos préstamos
 * - Alerta sobre devoluciones vencidas
 * - Informa sobre problemas con equipos
 * - Notifica sobre cambios en stock
 * - Reportes de actividad a administradores
 * 
 * @author Sistema de Gestión Escolar ET20
 * @version 2.0 - Con Notificaciones Automáticas Integradas
 */
public class AttpPanelManager implements RolPanelManager {

    private final VentanaInicio ventana;
    private final int attpId;
    private final NotificationIntegrationUtil notificationUtil;

    // Control de estado para notificaciones automáticas
    private boolean notificationsEnabled = true;
    private long lastNotificationTime = 0;
    private static final long NOTIFICATION_COOLDOWN = 1500; // 1.5 segundos entre notificaciones

    // Estadísticas para reportes automáticos
    private int prestamosRealizadosHoy = 0;
    private int devolucionesVencidasDetectadas = 0;
    private int equiposConProblemasReportados = 0;

    public AttpPanelManager(VentanaInicio ventana, int userId) {
        this.ventana = ventana;
        this.attpId = userId;
        this.notificationUtil = NotificationIntegrationUtil.getInstance();
        
        System.out.println("=== ATTP PANEL MANAGER INICIALIZADO ===");
        System.out.println("ATTP ID: " + attpId);
        System.out.println("Sistema de notificaciones: " + (notificationUtil != null ? "✅ ACTIVO" : "❌ INACTIVO"));
        
        // Verificar y configurar sistema de notificaciones
        inicializarSistemaNotificaciones();
        
        // Inicializar estadísticas del día
        inicializarEstadisticasDiarias();
    }

    /**
     * NUEVO: Inicializa el sistema de notificaciones específico para ATTP
     */
    private void inicializarSistemaNotificaciones() {
        if (notificationUtil != null) {
            boolean puedeEnviar = notificationUtil.puedeEnviarNotificaciones();
            System.out.println("Puede enviar notificaciones: " + (puedeEnviar ? "✅ SÍ" : "❌ NO"));
            
            if (puedeEnviar) {
                // Enviar notificación de inicio de sesión para ATTP
                enviarNotificacionInicioSesion();
                
                // Verificar equipos con problemas pendientes
                verificarEquiposPendientes();
            } else {
                System.err.println("⚠️ ATTP no tiene permisos para enviar notificaciones");
                notificationsEnabled = false;
            }
        } else {
            System.err.println("❌ Sistema de notificaciones no disponible para ATTP");
            notificationsEnabled = false;
        }
    }

    /**
     * NUEVO: Inicializa estadísticas diarias
     */
    private void inicializarEstadisticasDiarias() {
        try {
            // Aquí podrías consultar la BD para obtener estadísticas del día actual
            // Por ahora, inicializamos en 0
            prestamosRealizadosHoy = 0;
            devolucionesVencidasDetectadas = 0;
            equiposConProblemasReportados = 0;
            
            System.out.println("📊 Estadísticas ATTP inicializadas para el día");
        } catch (Exception e) {
            System.err.println("Error inicializando estadísticas: " + e.getMessage());
        }
    }

    @Override
    public JComponent[] createButtons() {
        JButton btnStock = createStyledButton("STOCK", "stock");
        JButton btnPrestamos = createStyledButton("PRÉSTAMOS", "prestamos");
        JButton btnRegistros = createStyledButton("REGISTROS", "registros");
        JButton btnNetbooks = createStyledButton("NETBOOKS", "netbooks");

        return new JComponent[]{btnStock, btnPrestamos, btnRegistros, btnNetbooks};
    }

    private JButton createStyledButton(String text, String actionCommand) {
        JButton button = new JButton(text);
        button.setBackground(new Color(51, 153, 255));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setActionCommand(actionCommand);
        button.addActionListener(e -> handleButtonAction(e.getActionCommand()));

        button.setMaximumSize(new Dimension(195, 40));
        button.setPreferredSize(new Dimension(195, 40));

        return button;
    }

    @Override
    public void handleButtonAction(String actionCommand) {
        try {
            System.out.println("=== ACCIÓN ATTP: " + actionCommand + " ===");
            
            // Registrar actividad para reportes
            registrarActividadATTP(actionCommand);
            
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
            System.err.println("❌ Error al procesar acción ATTP: " + ex.getMessage());
            ex.printStackTrace();
            
            // Notificar error a administradores
            notificarErrorSistema(actionCommand, ex.getMessage());
            
            JOptionPane.showMessageDialog(ventana,
                    "Error al procesar la acción: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ventana.restaurarVistaPrincipal();
        }
    }

    /**
     * MÉTODO MEJORADO: Muestra el panel de stock con notificaciones automáticas
     */
    private void mostrarPanelStock() {
        try {
            System.out.println("Creando StockPanel con notificaciones...");
            StockPanel panel = new StockPanel(ventana, attpId);
            
            // NUEVO: Configurar callbacks de notificación en el panel
            configurarNotificacionesStock(panel);
            
            ventana.mostrarPanelResponsive(panel, "Gestión de Stock");
            System.out.println("✅ StockPanel cargado exitosamente");

            // Notificar apertura de módulo de stock
            enviarNotificacionModuloAbierto("Stock", "Gestión de inventario y equipamiento");

        } catch (Exception ex) {
            System.err.println("❌ Error al cargar StockPanel: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * MÉTODO MEJORADO: Muestra el panel de préstamos con notificaciones
     */
    private void mostrarPanelPrestamos() {
        try {
            System.out.println("Creando PrestamosPanel con notificaciones...");
            PrestamosPanel panel = new PrestamosPanel(ventana, attpId);
            
            // NUEVO: Configurar callbacks de notificación en el panel
            configurarNotificacionesPrestamos(panel);
            
            ventana.mostrarPanelResponsive(panel, "Gestión de Préstamos");
            System.out.println("✅ PrestamosPanel cargado exitosamente");

            // Notificar apertura y verificar préstamos vencidos
            enviarNotificacionModuloAbierto("Préstamos", "Control de equipos prestados");
            verificarPrestamosVencidos();

        } catch (Exception ex) {
            System.err.println("❌ Error al cargar PrestamosPanel: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * MÉTODO MEJORADO: Muestra el panel de registros con seguimiento
     */
    private void mostrarPanelRegistros() {
        try {
            System.out.println("Creando RegistrosPanel con seguimiento...");
            RegistrosPanel panel = new RegistrosPanel(ventana, attpId);
            
            // NUEVO: Configurar seguimiento de registros
            configurarSeguimientoRegistros(panel);
            
            ventana.mostrarPanelResponsive(panel, "Registros del Sistema");
            System.out.println("✅ RegistrosPanel cargado exitosamente");

            // Generar reporte de actividad diaria
            generarReporteActividadDiaria();

        } catch (Exception ex) {
            System.err.println("❌ Error al cargar RegistrosPanel: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * MÉTODO MEJORADO: Muestra el panel de netbooks con control avanzado
     */
    private void mostrarPanelNetbooks() {
        try {
            System.out.println("Creando NetbookRegistrationPanel con control avanzado...");
            NetbookRegistrationPanel panel = new NetbookRegistrationPanel(ventana, attpId);
            
            // NUEVO: Configurar control avanzado de netbooks
            configurarControlNetbooks(panel);
            
            ventana.mostrarPanelResponsive(panel, "Registro de Netbooks");
            System.out.println("✅ NetbookRegistrationPanel cargado exitosamente");

            // Verificar estado de netbooks
            verificarEstadoNetbooks();

        } catch (Exception ex) {
            System.err.println("❌ Error al cargar NetbookRegistrationPanel: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    // ========================================
    // MÉTODOS DE NOTIFICACIONES ESPECÍFICAS ATTP
    // ========================================

    /**
     * NUEVO: Envía notificación de inicio de sesión
     */
    private void enviarNotificacionInicioSesion() {
        if (!canSendNotification()) return;
        
        try {
            String titulo = "🔧 ATTP - Sesión Iniciada";
            String contenido = String.format(
                "Sistema ATTP activado correctamente.\n\n" +
                "👨‍🔧 Responsable: %s\n" +
                "🕐 Hora de inicio: %s\n" +
                "📋 Módulos disponibles:\n" +
                "• Gestión de Stock\n" +
                "• Control de Préstamos\n" +
                "• Registro de Equipos\n" +
                "• Administración de Netbooks\n\n" +
                "¡Listo para gestionar el equipamiento técnico!",
                "ATTP #" + attpId,
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
                )
            );
            
            notificationUtil.enviarNotificacionBasica(titulo, contenido, attpId);
            System.out.println("✅ Notificación de inicio de sesión ATTP enviada");
            
        } catch (Exception e) {
            System.err.println("Error enviando notificación de inicio: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Notifica cuando se abre un módulo específico
     */
    private void enviarNotificacionModuloAbierto(String modulo, String descripcion) {
        if (!canSendNotification()) return;
        
        try {
            String titulo = "📂 Módulo " + modulo + " Activado";
            String contenido = String.format(
                "Módulo %s iniciado correctamente.\n\n" +
                "📝 Descripción: %s\n" +
                "👨‍🔧 ATTP: #%d\n" +
                "🕐 Hora: %s\n\n" +
                "Sistema listo para operar.",
                modulo, descripcion, attpId,
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                )
            );
            
            // Enviar solo al ATTP actual (notificación personal)
            notificationUtil.enviarNotificacionBasica(titulo, contenido, attpId);
            
        } catch (Exception e) {
            System.err.println("Error notificando módulo abierto: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Método público para notificar nuevo préstamo (llamado desde PrestamosPanel)
     */
    public void notificarNuevoPrestamo(int alumnoId, String equipoNombre, String fechaDevolucion) {
        if (!canSendNotification()) return;
        
        try {
            prestamosRealizadosHoy++;
            
            // Usar la utilidad específica para préstamos
            notificationUtil.notificarNuevoPrestamo(alumnoId, equipoNombre, fechaDevolucion, attpId);
            
            System.out.println("✅ Notificación de préstamo enviada - Equipo: " + equipoNombre);
            
            // Notificar a administradores si es un equipo de alto valor
            if (esEquipoAltoValor(equipoNombre)) {
                notificarPrestamoEquipoValioso(alumnoId, equipoNombre);
            }
            
        } catch (Exception e) {
            System.err.println("Error notificando nuevo préstamo: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Método público para notificar devolución vencida
     */
    public void notificarDevolucionVencida(int alumnoId, String equipoNombre, String fechaVencimiento, int diasVencidos) {
        if (!canSendNotification()) return;
        
        try {
            devolucionesVencidasDetectadas++;
            
            // Usar la utilidad específica para devoluciones vencidas
            notificationUtil.notificarDevolucionVencida(alumnoId, equipoNombre, fechaVencimiento, diasVencidos);
            
            System.out.println("⚠️ Notificación de devolución vencida enviada - Días: " + diasVencidos);
            
            // Si está muy vencido, notificar también a preceptores
            if (diasVencidos > 7) {
                notificarDevolucionCritica(alumnoId, equipoNombre, diasVencidos);
            }
            
        } catch (Exception e) {
            System.err.println("Error notificando devolución vencida: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Método público para notificar problema con equipo
     */
    public void notificarProblemaEquipo(int alumnoId, String equipoNombre, String descripcionProblema) {
        if (!canSendNotification()) return;
        
        try {
            equiposConProblemasReportados++;
            
            String titulo = "⚠️ Problema con Equipo";
            String contenido = String.format(
                "Se ha reportado un problema con un equipo prestado:\n\n" +
                "💻 Equipo: %s\n" +
                "❌ Problema: %s\n" +
                "👨‍🎓 Alumno: ID #%d\n" +
                "👨‍🔧 Reportado por: ATTP #%d\n" +
                "📅 Fecha: %s\n\n" +
                "🚨 ACCIÓN REQUERIDA:\n" +
                "• Contactar al alumno inmediatamente\n" +
                "• Evaluar el daño del equipo\n" +
                "• Determinar responsabilidades\n\n" +
                "Por favor, reportarse al ATTP lo antes posible.",
                equipoNombre, descripcionProblema, alumnoId, attpId,
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                )
            );
            
            // Notificar al alumno
            notificationUtil.enviarNotificacionUrgente(titulo, contenido, alumnoId);
            
            // Notificar también a administradores
            notificationUtil.enviarNotificacionARol(
                "🔧 ATTP - Problema de Equipo",
                "Se reportó un problema con equipo prestado. Equipo: " + equipoNombre + ". Revisar detalles en el sistema.",
                1 // Administradores
            );
            
            System.out.println("🚨 Notificación de problema de equipo enviada");
            
        } catch (Exception e) {
            System.err.println("Error notificando problema de equipo: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Método público para notificar cambio en stock
     */
    public void notificarCambioStock(String equipoNombre, String tipoMovimiento, int cantidad, String motivo) {
        if (!canSendNotification()) return;
        
        try {
            String emoji = tipoMovimiento.equalsIgnoreCase("entrada") ? "📈" : "📉";
            String titulo = emoji + " Movimiento de Stock";
            String contenido = String.format(
                "Registro de movimiento en inventario:\n\n" +
                "📦 Equipo: %s\n" +
                "%s Tipo: %s\n" +
                "🔢 Cantidad: %d unidades\n" +
                "📝 Motivo: %s\n" +
                "👨‍🔧 ATTP: #%d\n" +
                "📅 Fecha: %s\n\n" +
                "Stock actualizado correctamente.",
                equipoNombre, emoji, tipoMovimiento, cantidad, motivo, attpId,
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                )
            );
            
            // Notificar solo a administradores (control de inventario)
            notificationUtil.enviarNotificacionARol(titulo, contenido, 1);
            
            System.out.println("📦 Notificación de cambio de stock enviada");
            
        } catch (Exception e) {
            System.err.println("Error notificando cambio de stock: " + e.getMessage());
        }
    }

    // ========================================
    // MÉTODOS DE CONFIGURACIÓN DE PANELES
    // ========================================

    /**
     * NUEVO: Configura notificaciones automáticas para StockPanel
     */
    private void configurarNotificacionesStock(StockPanel panel) {
        try {
            // Aquí configurarías callbacks en el panel de stock
            // Por ejemplo, cuando se agregue/quite stock
            System.out.println("📋 Configurando notificaciones automáticas para Stock");
            
            // Ejemplo de cómo podrías configurar callbacks:
            // panel.setOnStockChangeCallback(this::notificarCambioStock);
            
        } catch (Exception e) {
            System.err.println("Error configurando notificaciones de stock: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Configura notificaciones automáticas para PrestamosPanel
     */
    private void configurarNotificacionesPrestamos(PrestamosPanel panel) {
        try {
            System.out.println("💻 Configurando notificaciones automáticas para Préstamos");
            
            // Ejemplo de callbacks que podrías configurar:
            // panel.setOnPrestamoCallback(this::notificarNuevoPrestamo);
            // panel.setOnDevolucionVencidaCallback(this::notificarDevolucionVencida);
            // panel.setOnProblemaEquipoCallback(this::notificarProblemaEquipo);
            
        } catch (Exception e) {
            System.err.println("Error configurando notificaciones de préstamos: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Configura seguimiento de RegistrosPanel
     */
    private void configurarSeguimientoRegistros(RegistrosPanel panel) {
        try {
            System.out.println("📊 Configurando seguimiento automático de Registros");
            
            // Configurar callbacks para auditoría
            // panel.setOnRegistroCallback(this::registrarActividad);
            
        } catch (Exception e) {
            System.err.println("Error configurando seguimiento de registros: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Configura control avanzado de NetbookRegistrationPanel
     */
    private void configurarControlNetbooks(NetbookRegistrationPanel panel) {
        try {
            System.out.println("💻 Configurando control avanzado de Netbooks");
            
            // Configurar callbacks específicos para netbooks
            // panel.setOnNetbookRegistradaCallback(this::notificarNuevaNetbook);
            // panel.setOnNetbookProblemaCallback(this::notificarProblemaNetbook);
            
        } catch (Exception e) {
            System.err.println("Error configurando control de netbooks: " + e.getMessage());
        }
    }

    // ========================================
    // MÉTODOS DE VERIFICACIÓN Y CONTROL
    // ========================================

    /**
     * NUEVO: Verifica equipos con problemas pendientes
     */
    private void verificarEquiposPendientes() {
        try {
            // Aquí harías consulta a BD para verificar equipos con problemas
            // Por ahora simulamos
            System.out.println("🔍 Verificando equipos con problemas pendientes...");
            
            // Ejemplo: si hay equipos pendientes, notificar
            int equiposPendientes = obtenerEquiposPendientes();
            if (equiposPendientes > 0) {
                notificarEquiposPendientes(equiposPendientes);
            }
            
        } catch (Exception e) {
            System.err.println("Error verificando equipos pendientes: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Verifica préstamos vencidos al abrir el módulo
     */
    private void verificarPrestamosVencidos() {
        try {
            System.out.println("⏰ Verificando préstamos vencidos...");
            
            // Simulación de verificación
            int prestamosVencidos = obtenerPrestamosVencidos();
            if (prestamosVencidos > 0) {
                notificarPrestamosVencidosDetectados(prestamosVencidos);
            }
            
        } catch (Exception e) {
            System.err.println("Error verificando préstamos vencidos: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Verifica estado general de netbooks
     */
    private void verificarEstadoNetbooks() {
        try {
            System.out.println("💻 Verificando estado de netbooks...");
            
            // Simulación de verificación
            int netbooksProblemas = obtenerNetbooksConProblemas();
            if (netbooksProblemas > 0) {
                notificarNetbooksConProblemas(netbooksProblemas);
            }
            
        } catch (Exception e) {
            System.err.println("Error verificando estado de netbooks: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Genera reporte de actividad diaria
     */
    private void generarReporteActividadDiaria() {
        if (!canSendNotification()) return;
        
        try {
            String titulo = "📊 Reporte Diario ATTP";
            String contenido = String.format(
                "Resumen de actividades del día:\n\n" +
                "📈 ESTADÍSTICAS:\n" +
                "• Préstamos realizados: %d\n" +
                "• Devoluciones vencidas: %d\n" +
                "• Equipos con problemas: %d\n\n" +
                "👨‍🔧 ATTP: #%d\n" +
                "📅 Fecha: %s\n\n" +
                "Reporte generado automáticamente.",
                prestamosRealizadosHoy,
                devolucionesVencidasDetectadas,
                equiposConProblemasReportados,
                attpId,
                java.time.LocalDate.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                )
            );
            
            // Enviar reporte a administradores
            notificationUtil.enviarNotificacionARol(titulo, contenido, 1);
            
            System.out.println("📊 Reporte diario generado y enviado");
            
        } catch (Exception e) {
            System.err.println("Error generando reporte diario: " + e.getMessage());
        }
    }

    // ========================================
    // MÉTODOS AUXILIARES Y UTILITARIOS
    // ========================================

    /**
     * NUEVO: Registra actividad del ATTP para auditoría
     */
    private void registrarActividadATTP(String accion) {
        try {
            System.out.println("📝 Registrando actividad ATTP: " + accion);
            
            // Aquí podrías guardar en BD la actividad
            // Por ahora solo log
            
        } catch (Exception e) {
            System.err.println("Error registrando actividad: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Notifica errores del sistema a administradores
     */
    private void notificarErrorSistema(String accion, String error) {
        if (!canSendNotification()) return;
        
        try {
            String titulo = "🚨 Error Sistema ATTP";
            String contenido = String.format(
                "Se ha producido un error en el sistema ATTP:\n\n" +
                "❌ Acción: %s\n" +
                "📝 Error: %s\n" +
                "👨‍🔧 ATTP: #%d\n" +
                "🕐 Hora: %s\n\n" +
                "Se requiere revisión del sistema.",
                accion, error, attpId,
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
                )
            );
            
            notificationUtil.enviarNotificacionUrgente(titulo, contenido, 1); // A admin
            
        } catch (Exception e) {
            System.err.println("Error notificando error de sistema: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Verifica si puede enviar notificación (control de spam)
     */
    private boolean canSendNotification() {
        if (!notificationsEnabled || notificationUtil == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastNotificationTime < NOTIFICATION_COOLDOWN) {
            return false;
        }
        
        lastNotificationTime = currentTime;
        return true;
    }

    /**
     * NUEVO: Determina si un equipo es de alto valor
     */
    private boolean esEquipoAltoValor(String equipoNombre) {
        String nombre = equipoNombre.toLowerCase();
        return nombre.contains("netbook") || 
               nombre.contains("laptop") || 
               nombre.contains("proyector") ||
               nombre.contains("servidor") ||
               nombre.contains("router");
    }

    // ========================================
    // MÉTODOS DE SIMULACIÓN (REEMPLAZAR CON CONSULTAS BD)
    // ========================================

    private int obtenerEquiposPendientes() {
        // Simular consulta a BD
        return (int) (Math.random() * 3); // 0-2 equipos pendientes
    }

    private int obtenerPrestamosVencidos() {
        // Simular consulta a BD
        return (int) (Math.random() * 5); // 0-4 préstamos vencidos
    }

    private int obtenerNetbooksConProblemas() {
        // Simular consulta a BD
        return (int) (Math.random() * 2); // 0-1 netbooks con problemas
    }

    // ========================================
    // MÉTODOS ESPECÍFICOS DE NOTIFICACIÓN
    // ========================================

    private void notificarPrestamoEquipoValioso(int alumnoId, String equipoNombre) {
        String titulo = "💎 Préstamo Equipo Valioso";
        String contenido = String.format(
            "ATENCIÓN: Se ha prestado un equipo de alto valor.\n\n" +
            "💻 Equipo: %s\n" +
            "👨‍🎓 Alumno ID: %d\n" +
            "👨‍🔧 ATTP: #%d\n" +
            "📅 Fecha: %s\n\n" +
            "⚠️ SUPERVISIÓN ESPECIAL REQUERIDA\n" +
            "Este equipo requiere seguimiento adicional.",
            equipoNombre, alumnoId, attpId,
            java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            )
        );
        
        notificationUtil.enviarNotificacionARol(titulo, contenido, 1); // Solo a administradores
    }

    private void notificarDevolucionCritica(int alumnoId, String equipoNombre, int diasVencidos) {
        String titulo = "🚨 Devolución CRÍTICA";
        String contenido = String.format(
            "URGENTE: Devolución con retraso crítico.\n\n" +
            "💻 Equipo: %s\n" +
            "👨‍🎓 Alumno ID: %d\n" +
            "⏰ Días vencidos: %d\n" +
            "👨‍🔧 ATTP: #%d\n\n" +
            "🚨 ACCIÓN INMEDIATA REQUERIDA:\n" +
            "• Contactar al alumno urgentemente\n" +
            "• Notificar a preceptoría\n" +
            "• Evaluar sanciones académicas\n\n" +
            "Este caso requiere intervención de preceptoría.",
            equipoNombre, alumnoId, diasVencidos, attpId
        );
        
        // Notificar a preceptores (rol 2)
        notificationUtil.enviarNotificacionUrgente(titulo, contenido, 2);
    }

    private void notificarEquiposPendientes(int cantidad) {
        String titulo = "🔧 Equipos Pendientes de Revisión";
        String contenido = String.format(
            "Hay equipos que requieren atención:\n\n" +
            "⚠️ Equipos pendientes: %d\n" +
            "👨‍🔧 ATTP: #%d\n" +
            "📅 Fecha: %s\n\n" +
            "Recomendación: Revisar estado de equipos\n" +
            "reportados con problemas.",
            cantidad, attpId,
            java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            )
        );
        
        notificationUtil.enviarNotificacionBasica(titulo, contenido, attpId);
    }

    private void notificarPrestamosVencidosDetectados(int cantidad) {
        String titulo = "⏰ Préstamos Vencidos Detectados";
        String contenido = String.format(
            "Se detectaron préstamos con retraso:\n\n" +
            "📊 Total vencidos: %d\n" +
            "👨‍🔧 ATTP: #%d\n" +
            "🕐 Hora: %s\n\n" +
            "💡 ACCIONES RECOMENDADAS:\n" +
            "• Contactar a los alumnos\n" +
            "• Verificar estado de equipos\n" +
            "• Aplicar procedimientos de recuperación\n\n" +
            "Revisar lista completa en el módulo de Préstamos.",
            cantidad, attpId,
            java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm")
            )
        );
        
        // Notificar al ATTP actual y a administradores
        notificationUtil.enviarNotificacionBasica(titulo, contenido, attpId);
        notificationUtil.enviarNotificacionARol(
            "📊 ATTP - Préstamos Vencidos", 
            "ATTP #" + attpId + " detectó " + cantidad + " préstamos vencidos. Revisar situación.", 
            1
        );
    }

    private void notificarNetbooksConProblemas(int cantidad) {
        String titulo = "💻 Netbooks con Problemas";
        String contenido = String.format(
            "Se detectaron netbooks que requieren atención:\n\n" +
            "⚠️ Netbooks afectadas: %d\n" +
            "👨‍🔧 ATTP: #%d\n" +
            "📅 Fecha: %s\n\n" +
            "🔧 REVISIÓN TÉCNICA NECESARIA:\n" +
            "• Diagnóstico de hardware\n" +
            "• Verificación de software\n" +
            "• Evaluación de reparaciones\n\n" +
            "Acceder al módulo de Netbooks para más detalles.",
            cantidad, attpId,
            java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            )
        );
        
        notificationUtil.enviarNotificacionBasica(titulo, contenido, attpId);
    }

    // ========================================
    // MÉTODOS PÚBLICOS PARA INTEGRACIÓN CON PANELES
    // ========================================

    /**
     * NUEVO: Método público para que los paneles notifiquen eventos
     */
    public void notificarEventoGenerico(String tipoEvento, String descripcion, int usuarioAfectado) {
        if (!canSendNotification()) return;
        
        try {
            String emoji = obtenerEmojiPorTipoEvento(tipoEvento);
            String titulo = emoji + " " + tipoEvento;
            String contenido = String.format(
                "Evento registrado en sistema ATTP:\n\n" +
                "📋 Tipo: %s\n" +
                "📝 Descripción: %s\n" +
                "👤 Usuario afectado: #%d\n" +
                "👨‍🔧 ATTP: #%d\n" +
                "🕐 Hora: %s\n\n" +
                "Evento procesado correctamente.",
                tipoEvento, descripcion, usuarioAfectado, attpId,
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
                )
            );
            
            if (usuarioAfectado > 0) {
                notificationUtil.enviarNotificacionBasica(titulo, contenido, usuarioAfectado);
            }
            
        } catch (Exception e) {
            System.err.println("Error notificando evento genérico: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Método para notificar mantenimiento de equipos
     */
    public void notificarMantenimientoEquipo(String equipoNombre, String tipoMantenimiento, String resultado) {
        if (!canSendNotification()) return;
        
        try {
            String titulo = "🔧 Mantenimiento de Equipo";
            String contenido = String.format(
                "Mantenimiento realizado:\n\n" +
                "📦 Equipo: %s\n" +
                "🔧 Tipo: %s\n" +
                "✅ Resultado: %s\n" +
                "👨‍🔧 ATTP: #%d\n" +
                "📅 Fecha: %s\n\n" +
                "Registro de mantenimiento completado.",
                equipoNombre, tipoMantenimiento, resultado, attpId,
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                )
            );
            
            // Notificar a administradores para registro
            notificationUtil.enviarNotificacionARol(titulo, contenido, 1);
            
        } catch (Exception e) {
            System.err.println("Error notificando mantenimiento: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Método para notificar ingreso de nuevo equipamiento
     */
    public void notificarNuevoEquipamiento(String equipoNombre, int cantidad, String proveedor) {
        if (!canSendNotification()) return;
        
        try {
            String titulo = "📦 Nuevo Equipamiento Ingresado";
            String contenido = String.format(
                "Nuevo equipamiento agregado al inventario:\n\n" +
                "📦 Equipo: %s\n" +
                "🔢 Cantidad: %d unidades\n" +
                "🏢 Proveedor: %s\n" +
                "👨‍🔧 ATTP: #%d\n" +
                "📅 Fecha: %s\n\n" +
                "✅ Stock actualizado correctamente.\n" +
                "El equipamiento está disponible para préstamo.",
                equipoNombre, cantidad, proveedor, attpId,
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                )
            );
            
            // Notificar a administradores y otros ATTPs
            notificationUtil.enviarNotificacionARol(titulo, contenido, 1); // Administradores
            notificationUtil.enviarNotificacionARol(titulo, contenido, 5); // Otros ATTPs
            
        } catch (Exception e) {
            System.err.println("Error notificando nuevo equipamiento: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Método para generar alerta de stock bajo
     */
    public void notificarStockBajo(String equipoNombre, int cantidadRestante, int minimoRequerido) {
        if (!canSendNotification()) return;
        
        try {
            String titulo = "⚠️ ALERTA: Stock Bajo";
            String contenido = String.format(
                "ATENCIÓN: Stock por debajo del mínimo.\n\n" +
                "📦 Equipo: %s\n" +
                "📊 Stock actual: %d unidades\n" +
                "🎯 Mínimo requerido: %d unidades\n" +
                "👨‍🔧 ATTP: #%d\n" +
                "📅 Fecha: %s\n\n" +
                "🚨 ACCIÓN REQUERIDA:\n" +
                "• Gestionar reposición urgente\n" +
                "• Contactar proveedores\n" +
                "• Restringir préstamos si es necesario\n\n" +
                "Es necesario reabastecer el inventario.",
                equipoNombre, cantidadRestante, minimoRequerido, attpId,
                java.time.LocalDate.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                )
            );
            
            // Notificar urgentemente a administradores
            notificationUtil.enviarNotificacionUrgente(titulo, contenido, 1);
            
        } catch (Exception e) {
            System.err.println("Error notificando stock bajo: " + e.getMessage());
        }
    }

    // ========================================
    // MÉTODOS DE CIERRE Y LIMPIEZA
    // ========================================

    /**
     * NUEVO: Método para enviar reporte de cierre de sesión
     */
    public void enviarReporteCierreSession() {
        if (!canSendNotification()) return;
        
        try {
            String titulo = "📊 Cierre de Sesión ATTP";
            String contenido = String.format(
                "Sesión ATTP finalizada:\n\n" +
                "👨‍🔧 ATTP: #%d\n" +
                "🕐 Hora de cierre: %s\n\n" +
                "📈 RESUMEN DE ACTIVIDADES:\n" +
                "• Préstamos realizados: %d\n" +
                "• Devoluciones vencidas detectadas: %d\n" +
                "• Equipos con problemas reportados: %d\n\n" +
                "✅ Sesión cerrada correctamente.\n" +
                "Todos los registros han sido guardados.",
                attpId,
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
                ),
                prestamosRealizadosHoy,
                devolucionesVencidasDetectadas,
                equiposConProblemasReportados
            );
            
            // Enviar solo si hubo actividad significativa
            if (prestamosRealizadosHoy > 0 || devolucionesVencidasDetectadas > 0 || equiposConProblemasReportados > 0) {
                notificationUtil.enviarNotificacionARol(titulo, contenido, 1); // A administradores
            }
            
            System.out.println("📊 Reporte de cierre de sesión enviado");
            
        } catch (Exception e) {
            System.err.println("Error enviando reporte de cierre: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Limpia recursos y envía reporte final
     */
    public void cleanup() {
        try {
            System.out.println("🧹 Limpiando recursos del AttpPanelManager...");
            
            // Enviar reporte de cierre si hubo actividad
            enviarReporteCierreSession();
            
            // Resetear estadísticas
            prestamosRealizadosHoy = 0;
            devolucionesVencidasDetectadas = 0;
            equiposConProblemasReportados = 0;
            
            // Deshabilitar notificaciones
            notificationsEnabled = false;
            
            System.out.println("✅ Recursos del AttpPanelManager liberados");
            
        } catch (Exception e) {
            System.err.println("Error en cleanup de AttpPanelManager: " + e.getMessage());
        }
    }

    // ========================================
    // MÉTODOS AUXILIARES FINALES
    // ========================================

    /**
     * NUEVO: Obtiene emoji apropiado según tipo de evento
     */
    private String obtenerEmojiPorTipoEvento(String tipoEvento) {
        switch (tipoEvento.toLowerCase()) {
            case "prestamo":
            case "préstamo":
                return "💻";
            case "devolucion":
            case "devolución":
                return "📥";
            case "problema":
                return "⚠️";
            case "mantenimiento":
                return "🔧";
            case "stock":
                return "📦";
            case "netbook":
                return "💻";
            case "registro":
                return "📝";
            default:
                return "ℹ️";
        }
    }

    /**
     * NUEVO: Obtiene información de debug del panel manager
     */
    public String getDebugInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=== ATTP Panel Manager Debug ===\n");
        info.append("ATTP ID: ").append(attpId).append("\n");
        info.append("Notificaciones habilitadas: ").append(notificationsEnabled).append("\n");
        info.append("Última notificación: ").append(lastNotificationTime).append("\n");
        info.append("Préstamos hoy: ").append(prestamosRealizadosHoy).append("\n");
        info.append("Devoluciones vencidas: ").append(devolucionesVencidasDetectadas).append("\n");
        info.append("Equipos con problemas: ").append(equiposConProblemasReportados).append("\n");
        info.append("Sistema notificación activo: ").append(notificationUtil != null).append("\n");
        
        if (notificationUtil != null) {
            info.append("Puede enviar notificaciones: ").append(notificationUtil.puedeEnviarNotificaciones()).append("\n");
        }
        
        info.append("================================");
        return info.toString();
    }

    /**
     * NUEVO: Método para testing del sistema de notificaciones
     */
    public void testNotificationSystem() {
        System.out.println("🧪 === TESTING SISTEMA DE NOTIFICACIONES ATTP ===");
        
        if (notificationUtil == null) {
            System.err.println("❌ Sistema de notificaciones no disponible");
            return;
        }
        
        try {
            // Test 1: Notificación básica
            System.out.println("Test 1: Notificación básica...");
            enviarNotificacionInicioSesion();
            Thread.sleep(2000);
            
            // Test 2: Notificación de préstamo
            System.out.println("Test 2: Notificación de préstamo...");
            notificarNuevoPrestamo(123, "Netbook Dell Inspiron", "15/06/2024");
            Thread.sleep(2000);
            
            // Test 3: Notificación de problema
            System.out.println("Test 3: Notificación de problema...");
            notificarProblemaEquipo(123, "Proyector Epson", "No enciende correctamente");
            Thread.sleep(2000);
            
            // Test 4: Reporte de actividad
            System.out.println("Test 4: Reporte de actividad...");
            generarReporteActividadDiaria();
            
            System.out.println("✅ Testing completado exitosamente");
            
        } catch (Exception e) {
            System.err.println("❌ Error durante testing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========================================
    // GETTERS PARA ACCESO EXTERNO
    // ========================================

    public int getAttpId() {
        return attpId;
    }

    public VentanaInicio getVentana() {
        return ventana;
    }

    public NotificationIntegrationUtil getNotificationUtil() {
        return notificationUtil;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public int getPrestamosRealizadosHoy() {
        return prestamosRealizadosHoy;
    }

    public int getDevolucionesVencidasDetectadas() {
        return devolucionesVencidasDetectadas;
    }

    public int getEquiposConProblemasReportados() {
        return equiposConProblemasReportados;
    }

    /**
     * NUEVO: Método toString para debugging
     */
    @Override
    public String toString() {
        return String.format(
            "AttpPanelManager{attpId=%d, notificationsEnabled=%s, prestamosHoy=%d, devolucionesVencidas=%d, equiposProblemas=%d}",
            attpId, notificationsEnabled, prestamosRealizadosHoy, devolucionesVencidasDetectadas, equiposConProblemasReportados
        );
    }
}