package com.example.paintsprintfinal;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.control.Tooltip;

import java.util.Optional;

public class windowFunctions {
    @FXML
    public Button exitButton;

    @FXML
    public MenuItem selectFile;

    @FXML
    public MenuItem saveButton;

    @FXML
    public MenuItem saveAsButton;

    @FXML
    public MenuItem aboutButton;

    @FXML
    public Button undoButton;

    @FXML
    public Button redoButton;

    @FXML
    public StackPane stackPane;

    @FXML
    public Button eyeDropper;

    @FXML
    public Button clearButton;

    @FXML
    public Button newCanvas;


    public windowTools windowTools;

    public GraphicsContext graphicsContext;

    public paintFunctions paintFunctions;

    @FXML
    public Canvas canvas;

    public static boolean canvasSaved = false;

    Tooltip saveAs = new Tooltip("Save a file to a specific format");

    @FXML
    public void initialize(){;
        paintFunctions.initialize();
        saveFunctions();
        miscFunctions();
    }

    public void saveFunctions() {
        saveAsButton.setOnAction((actionEvent) -> {
            windowTools.saveAsClick();
            canvasSaved = true;
        });

        saveButton.setOnAction((actionEvent -> {
            windowTools.saveClick();
            canvasSaved = true;
        }));
    }
    public void miscFunctions(){
        selectFile.setOnAction((actionEvent) -> {
            windowTools.selectClick();
        });
        exitButton.setTooltip(new Tooltip("Exit"));
        exitButton.setOnAction((actionEvent) -> {
            Stage stage = (Stage) exitButton.getScene().getWindow();
            windowTools.exitClick(stage);
        });
        aboutButton.setOnAction((actionEvent -> {
            windowTools.aboutClick();
        }));
        undoButton.setTooltip(new Tooltip("Undoes the previous action"));
        undoButton.setOnAction((actionEvent -> {
            paintFunctions.undoClick();
        }));
        redoButton.setTooltip(new Tooltip("Redoes the previous action"));
        redoButton.setOnAction((actionEvent -> {
            paintFunctions.redoClick();
        }));
        clearButton.setTooltip(new Tooltip("Clears the canvas"));
        clearButton.setOnAction((actionEvent -> {
            paintFunctions.clearCanvas();
        }));
        newCanvas.setTooltip(new Tooltip("Creates a new canvas"));
        newCanvas.setOnAction((actionEvent -> {
            paintFunctions.newCanvas();
        }));
    }

    @FXML
    public static void isSaved(Stage stage) {
        if (!canvasSaved) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(stage);
            alert.setTitle("Unsaved Changes");
            alert.setHeaderText("You have unsaved changes. Do you want to save?");

            ButtonType saveButton = new ButtonType("Save");
            ButtonType dontSaveButton = new ButtonType("Exit Without Saving");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(saveButton, dontSaveButton, cancelButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == saveButton) {
            } else if (result.get() == dontSaveButton) {
                stage.close();
            } else {
            }
        } else {
            stage.close();
        }
    }


}
