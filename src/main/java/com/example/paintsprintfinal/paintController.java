package com.example.paintsprintfinal;

import com.example.paintsprintfinal.paintFunctions;
import com.example.paintsprintfinal.windowFunctions;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;

/**
 * Initializes the controller for the main paint program, initializing the classes and setting up the canvas
 */

public class paintController {
    @FXML
    public Canvas canvas;

    @FXML
    public AnchorPane anchorPane;
    public static paintFunctions paintHandler;
    public static windowFunctions windowHandler;

    public String selectedTool;

    @FXML
    public void initialize(){
        windowHandler.initialize();
        paintHandler.initialize();
        canvasResizing();
    }

    public void setSelectedTool(String tool){
        selectedTool = tool;
    }

    public void canvasResizing(){
        AnchorPane.setTopAnchor(canvas, 0.0);
        AnchorPane.setBottomAnchor(canvas, 0.0);
        AnchorPane.setLeftAnchor(canvas, 0.0);
        AnchorPane.setRightAnchor(canvas, 0.0);
        canvas.widthProperty().bind(anchorPane.widthProperty());
        canvas.heightProperty().bind(anchorPane.heightProperty());
    }
}