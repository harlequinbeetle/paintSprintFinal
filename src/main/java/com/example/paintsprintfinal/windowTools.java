package com.example.paintsprintfinal;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Handles the save functions of the paint program
 */

public class windowTools {
    @FXML
    private StackPane stackPane; // Reference to StackPane from windowFunctions
    private FileChooser fileChooser; // Initialize file chooser
    private String filePath;
    private paintFunctions paintFunctions = new paintFunctions();


    public windowTools(StackPane stackPane) {
        this.stackPane = stackPane;
        this.fileChooser = new FileChooser();
    }

    @FXML
    public void selectClick() {
        Scene scene = stackPane.getScene();
        Stage stage = (Stage) scene.getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            ImageView displayFile = new ImageView(image);
            stackPane.getChildren().add(displayFile);

            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();

            displayFile.setFitWidth(imageWidth);
            displayFile.setFitHeight(imageHeight);
            stage.setWidth(imageWidth + 50);
            stage.setHeight(imageHeight + 100);
        }
        paintFunctions.logEvent(LocalDateTime.now(), "Image Selected");
    }


    @FXML
    public void saveClick() {
        if (filePath != null) {
            try {
                File file = new File(filePath);
                WritableImage writableImage = new WritableImage((int) stackPane.getWidth(), (int) stackPane.getHeight());
                stackPane.snapshot(null, writableImage);
                BufferedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(renderedImage, "png", file);
            } catch (IOException e) {
                System.out.println("Error saving file: " + e.getMessage());
            }
        } else {
            System.out.println("No file selected.");
        }
        paintFunctions.resetAutosave();
        paintFunctions.logEvent(LocalDateTime.now(), "Image Saved");
    }

    @FXML
    public void saveAsClick() {
        FileChooser saveFile = new FileChooser();
        saveFile.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
        saveFile.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG Files", "*.jpg"));
        saveFile.getExtensionFilters().add(new FileChooser.ExtensionFilter("BMP Files", "*.bmp"));

        Stage stage = (Stage) stackPane.getScene().getWindow(); // Get the stage from stackPane
        File file = saveFile.showSaveDialog(stage);
        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage((int) stackPane.getWidth(), (int) stackPane.getHeight());
                stackPane.snapshot(null, writableImage);
                BufferedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(renderedImage, "png", file); // Default to PNG
            } catch (IOException e) {
                System.out.println("Error saving file: " + e.getMessage());
            }
        }
       paintFunctions.resetAutosave();
        paintFunctions.logEvent(LocalDateTime.now(), "Image Saved As");
    }

    @FXML
    public void exitClick(Stage stage) {
        windowFunctions.isSaved(stage);
        paintFunctions.stopWebServer();
        paintFunctions.logEvent(LocalDateTime.now(), "Exit");
    }

    @FXML
    public void aboutClick() {
        Stage aboutWindow = new Stage();
        aboutWindow.setTitle("About");
        aboutWindow.show();
        paintFunctions.logEvent(LocalDateTime.now(), "About window opened");
    }

}