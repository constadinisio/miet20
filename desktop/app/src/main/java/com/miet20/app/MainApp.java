package com.miet20.app;

import com.miet20.ui.MainViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

/**
 * Entry point for the Miet20 JavaFX desktop application.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(resourceUrl("/com/miet20/resources/fxml/main-view.fxml"));
        loader.setControllerFactory(param -> new MainViewController());
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(resourceUrl("/com/miet20/resources/styles/theme.css").toExternalForm());

        primaryStage.setTitle("Miet20 Desktop");
        primaryStage.setScene(scene);
        loadIcon().ifPresent(image -> primaryStage.getIcons().add(image));
        primaryStage.show();
    }

    private URL resourceUrl(String path) {
        return Objects.requireNonNull(getClass().getResource(path), "Missing resource: " + path);
    }

    private Optional<Image> loadIcon() {
        URL iconUrl = getClass().getResource("/com/miet20/resources/images/app-icon.png");
        return iconUrl != null ? Optional.of(new Image(iconUrl.toExternalForm())) : Optional.empty();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
