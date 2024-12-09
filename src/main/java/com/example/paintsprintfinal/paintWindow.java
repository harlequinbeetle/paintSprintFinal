package com.example.paintsprintfinal;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;


import java.io.IOException;


/**
 * Class to initialize the paint window
 * windowLength and windowHeight - set the base dimensions of the window
 */

public class paintWindow {
    @FXML
    private AnchorPane displayPane;

    @FXML
    public Canvas canvas;

    public static int windowLength = 900;
    public static int windowHeight = 800;

    public static void initialize(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(paintWindow.class.getResource("paint.fxml"));
        Parent rootScene = fxmlLoader.load();

        Scene scene = new Scene(rootScene, windowLength, windowHeight);
        stage.setResizable(true);
        stage.setTitle("Paint");
        stage.setScene(scene);

        paintWindow controller = fxmlLoader.getController();
        AnchorPane.setTopAnchor(controller.canvas, 0.0);
        AnchorPane.setBottomAnchor(controller.canvas, 0.0);
        AnchorPane.setLeftAnchor(controller.canvas, 0.0);
        AnchorPane.setRightAnchor(controller.canvas, 0.0);

        stage.show();

    }
}
