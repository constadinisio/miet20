package main.java.tickets;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import main.java.utils.NotificationManager;

/**
 * Servicio especializado para manejar notificaciones de tickets en tiempo real
 * Se ejecuta en segundo plano para detectar nuevos tickets inmediatamente
 */
public class TicketNotificationService {
    
    private static TicketNotificationService instance;
    private final ScheduledExecutorService scheduler;
    private final TicketService ticketService;
    private boolean isRunning = false;
    
    private TicketNotificationService() {
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.ticketService = TicketService.getInstance();
    }
    
    public static synchronized TicketNotificationService getInstance() {
        if (instance == null) {
            instance = new TicketNotificationService();
        }
        return instance;
    }
    
    /**
     * Inicia el servicio de monitoreo en tiempo real
     */
    public void iniciarMonitoreo(int userId) {
        if (isRunning || !ticketService.esDeveloper(userId)) {
            return;
        }
        
        System.out.println("🚀 Iniciando monitoreo de tickets en tiempo real para desarrollador: " + userId);
        
        isRunning = true;
        
        // Monitoreo cada 10 segundos para desarrolladores
        scheduler.scheduleAtFixedRate(() -> {
            verificarYNotificarNuevosTickets();
        }, 0, 10, TimeUnit.SECONDS);
        
        System.out.println("✅ Monitoreo de tickets iniciado");
    }
    
    /**
     * Detiene el servicio de monitoreo
     */
    public void detenerMonitoreo() {
        if (!isRunning) {
            return;
        }
        
        System.out.println("⏸️ Deteniendo monitoreo de tickets...");
        
        isRunning = false;
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("✅ Monitoreo de tickets detenido");
    }
    
    /**
     * Verifica nuevos tickets y envía notificaciones inmediatas
     */
    private void verificarYNotificarNuevosTickets() {
        CompletableFuture.supplyAsync(() -> {
            try {
                // Verificar tickets creados en los últimos 15 segundos
                return ticketService.obtenerTicketsRecientes(15);
            } catch (Exception e) {
                System.err.println("Error verificando tickets recientes: " + e.getMessage());
                return null;
            }
        }).thenAccept(tickets -> {
            if (tickets != null && !tickets.isEmpty()) {
                System.out.println("🚨 Detectados " + tickets.size() + " tickets recientes, notificando...");
                
                SwingUtilities.invokeLater(() -> {
                    // Actualizar todas las campanitas visibles
                    actualizarTodasLasCampanitas();
                    
                    // Enviar notificación push si está disponible
                    enviarNotificacionPush(tickets.size());
                });
            }
        }).exceptionally(throwable -> {
            System.err.println("Error en verificación de tickets: " + throwable.getMessage());
            return null;
        });
    }
    
    /**
     * Actualiza todas las campanitas de tickets en ventanas abiertas
     */
    private void actualizarTodasLasCampanitas() {
        try {
            for (java.awt.Window window : java.awt.Window.getWindows()) {
                if (window.isVisible() && window instanceof javax.swing.JFrame) {
                    buscarYActualizarCampanitas(((javax.swing.JFrame) window).getContentPane());
                }
            }
        } catch (Exception e) {
            System.err.println("Error actualizando campanitas: " + e.getMessage());
        }
    }
    
    private void buscarYActualizarCampanitas(java.awt.Container container) {
        for (java.awt.Component comp : container.getComponents()) {
            if (comp instanceof TicketBellComponent) {
                ((TicketBellComponent) comp).forceRefresh();
            } else if (comp instanceof java.awt.Container) {
                buscarYActualizarCampanitas((java.awt.Container) comp);
            }
        }
    }
    
    /**
     * Envía notificación push del sistema
     */
    private void enviarNotificacionPush(int cantidadTickets) {
        try {
            NotificationManager notifManager = NotificationManager.getInstance();
            if (notifManager != null) {
                String mensaje = "¡" + cantidadTickets + " nuevo(s) ticket(s) recibido(s)!";
                System.out.println("📬 " + mensaje);
                
                // Aquí podrías agregar notificaciones del SO si están disponibles
                // SystemTray, etc.
            }
        } catch (Exception e) {
            System.err.println("Error enviando notificación push: " + e.getMessage());
        }
    }
    
    public boolean isRunning() {
        return isRunning;
    }
}