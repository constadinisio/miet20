package main.java.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilidad para el manejo de contraseñas con cifrado bcrypt
 * Reemplaza el uso de SHA-256 para mayor seguridad y compatibilidad multiplataforma
 */
public class PasswordUtils {
    
    /**
     * Cifra una contraseña usando bcrypt
     * @param password La contraseña en texto plano
     * @return La contraseña cifrada
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    
    /**
     * Verifica si una contraseña coincide con el hash
     * @param password La contraseña en texto plano
     * @param hashedPassword El hash almacenado
     * @return true si la contraseña es correcta, false en caso contrario
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
    
    /**
     * Verifica si un hash es válido para bcrypt
     * @param hash El hash a verificar
     * @return true si es un hash bcrypt válido, false en caso contrario
     */
    public static boolean isBcryptHash(String hash) {
        return hash != null && hash.startsWith("$2a$") && hash.length() == 60;
    }
    
    /**
     * Método auxiliar para migrar desde SHA-256 a bcrypt
     * Verifica primero si es un hash bcrypt, si no, intenta con SHA-256
     * @param password La contraseña en texto plano
     * @param storedHash El hash almacenado (puede ser SHA-256 o bcrypt)
     * @return true si la contraseña es correcta
     */
    public static boolean verifyPasswordWithMigration(String password, String storedHash) {
        // Si es un hash bcrypt, usar bcrypt
        if (isBcryptHash(storedHash)) {
            return verifyPassword(password, storedHash);
        }
        
        // Si no es bcrypt, asumir que es SHA-256 (para compatibilidad durante migración)
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString().equals(storedHash);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
