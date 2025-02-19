private void updateUserInterface(JFrame form, UserSession session) {
    try {
        PreparedStatement stmt = conect.prepareStatement(
            "SELECT nombre, apellido, anio, division, foto_url FROM usuarios WHERE mail = ?"
        );
        stmt.setString(1, session.getEmail());
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            // Obtener nombre y apellido
            String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");
            
            // Convertir rol numérico a texto
            String rolTexto;
            switch (session.getRol()) {
                case 4:
                    rolTexto = "Estudiante";
                    break;
                case 3:
                    rolTexto = "Profesor";
                    break;
                case 2:
                    rolTexto = "Preceptor";
                    break;
                case 1:
                    rolTexto = "Administrador";
                    break;
                default:
                    rolTexto = "Sin asignar";
            }
            
            // Obtener curso y división
            String cursoDiv = rs.getString("anio") + "°" + rs.getString("division");
            
            // Actualizar la interfaz
            if (form instanceof alumnos) {
                alumnos alumnoForm = (alumnos)form;
                alumnoForm.updateLabels(nombreCompleto, rolTexto, cursoDiv);
                
                // Cargar y mostrar la foto de perfil
                String fotoUrl = session.getFotoUrl();
                if (fotoUrl != null && !fotoUrl.isEmpty()) {
                    try {
                        URL url = new URL(fotoUrl);
                        Image imagen = ImageIO.read(url);
                        Image imagenRedimensionada = imagen.getScaledInstance(96, 96, Image.SCALE_SMOOTH);
                        alumnoForm.labelFotoPerfil.setIcon(new ImageIcon(imagenRedimensionada));
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Si hay error, mantener una imagen por defecto
                        alumnoForm.labelFotoPerfil.setIcon(new ImageIcon(getClass().getResource("/images/icons8-user-96.png")));
                    }
                }
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null,
            "Error al cargar datos del usuario: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
}