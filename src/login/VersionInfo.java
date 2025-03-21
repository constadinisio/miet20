package login;

public class VersionInfo {
    private String version;
    private String downloadUrl;
    private String[] cambios;
    private boolean requiereReinicio;
    
    public VersionInfo(String version, String downloadUrl, String[] cambios, boolean requiereReinicio) {
        this.version = version;
        this.downloadUrl = downloadUrl;
        this.cambios = cambios;
        this.requiereReinicio = requiereReinicio;
    }
    
    // Getters y setters
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    
    public String[] getCambios() { return cambios; }
    public void setCambios(String[] cambios) { this.cambios = cambios; }
    
    public boolean isRequiereReinicio() { return requiereReinicio; }
    public void setRequiereReinicio(boolean requiereReinicio) { this.requiereReinicio = requiereReinicio; }
}