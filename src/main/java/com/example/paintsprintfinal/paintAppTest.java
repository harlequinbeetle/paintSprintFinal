package com.example.paintsprintfinal;

import javafx.scene.canvas.Canvas;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.testng.annotations.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the initialization of the canvas... it's in the name
 */
public class paintAppTest {

    @Test
    public void testCanvas(){
        TabPane tabPane = new TabPane();
        Tab newTab = new Tab("Canvas");
        Canvas canvas = new Canvas(500, 500);
        newTab.setContent(canvas);
        tabPane.getTabs().add(newTab);
        assertNotNull(newTab.getContent());
        assertTrue(newTab.getContent() instanceof Canvas);

        Canvas createdCanvas = (Canvas) newTab.getContent();
        assertEquals(500, createdCanvas.getWidth());
        assertEquals(500, createdCanvas.getHeight());
    }


}
