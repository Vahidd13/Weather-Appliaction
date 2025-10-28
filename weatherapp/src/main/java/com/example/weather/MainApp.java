package com.example.weather;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for the Weather Dashboard JavaFX application.
 * Loads the main FXML layout, applies styles, and shows the primary stage.
 */
public class MainApp extends Application {

    /**
     * Called by the JavaFX runtime to start the application.
     * Loads the FXML, sets up the scene and stylesheet, and shows the stage.
     *
     * @param stage the primary stage for this application
     * @throws Exception if loading the FXML or stylesheet fails
     */
    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("Starting Weather Dashboard UI…");
        Parent root = FXMLLoader.load(
            getClass().getResource("/com/example/weather/main.fxml")
        );

        // Create the scene
        Scene scene = new Scene(root, 600, 400);
        // Attach your stylesheet
        scene.getStylesheets().add(
            getClass().getResource("/styles/app.css").toExternalForm()
        );

        // Set up the stage
        stage.setTitle("Weather Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Main entry point. Launches the JavaFX application.
     *
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {
        launch();
    }
}
