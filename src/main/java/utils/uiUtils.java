/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.utils;


import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class uiUtils {

    private static final String APP_TITLE = "MiET20";
    private static final String ICON_PATH = "/main/resources/images/logo_et20_max.png";

    public static void configurarVentana(JFrame frame) {
        frame.setTitle(APP_TITLE);

        URL url = uiUtils.class.getResource(ICON_PATH);
        if (url != null) {
            Image icon = Toolkit.getDefaultToolkit().getImage(url);
            frame.setIconImage(icon);
        } else {
            System.err.println("No se encontró el ícono en " + ICON_PATH);
        }
    }
}
