package com.miet20.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the base placeholder view.
 */
public class MainViewController {

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        statusLabel.setText("Bienvenido a Miet20 Desktop (JavaFX)");
    }
}
