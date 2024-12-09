package com.example.paintsprintfinal;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.WritableImage;
import org.junit.jupiter.api.Test;

import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the functionality of undo and redo... in the name... again
 */
public class paintUndoRedoTest {

    @FXML
    public WritableImage writableImage;

    public Stack<WritableImage> stack;

    @Test
    public void testUndoRedo(){
        TabPane tabPane = new TabPane();
        Tab newTab = new Tab("New Canvas");
        Canvas canvas = new Canvas(800, 600);
        newTab.setContent(canvas);
        tabPane.getTabs().add(newTab);

        Stack<WritableImage> undoStack = new Stack<>();
        Stack<WritableImage> redoStack = new Stack<>();

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.fillRect(50, 50, 100, 100);

        WritableImage snapshot = canvas.snapshot(null, null);
        undoStack.push(snapshot);

        WritableImage undoImage = undoStack.pop();
        redoStack.push(snapshot);

        assertFalse(undoStack.isEmpty());
        assertFalse(redoStack.isEmpty());
    }
}