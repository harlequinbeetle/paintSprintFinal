package com.example.paintsprintfinal;

// importing an unholy amount of stuff
import fi.iki.elonen.NanoHTTPD;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.Tooltip;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Big Boy
 * Contains all of the functions for drawing, autosaving, creating new canvases
 *
 */

public class paintFunctions extends com.example.paintsprintfinal.paintWindow {
    @FXML
    public CheckBox dashToggle;

    @FXML
    public TabPane tabPane;

    @FXML
    public Pane displayPane;

    @FXML
    public StackPane stackPane;

    @FXML
    public Slider widthSlider;

    @FXML
    public Text widthText;

    @FXML
    public ColorPicker colorPicker;

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
    public ImageView displayFile;

    @FXML
    public CheckBox autoSaveToggle;

    @FXML
    private GraphicsContext graphicsContext;
    @FXML
    public Button lineSelect;

    @FXML
    public Button rectangleSelect;

    @FXML
    public Button straightSelect;

    @FXML
    public Button circleSelect;

    @FXML
    public Button ellipseSelect;

    @FXML
    public Button starSelect;

    @FXML
    public Button triangleSelect;

    @FXML
    public Button polygonSelect;

    @FXML
    public Button textSelect;

    @FXML
    public Button selectTool;

    @FXML
    public Button squareSelect;

    @FXML
    public Button horizontalFlip;

    @FXML
    public Button verticalFlip;

    @FXML
    public Button redoButton;
    @FXML
    public Button undoButton;
    @FXML
    public MenuItem clearButton;
    @FXML
    public MenuItem newCanvas;
    @FXML
    public Button eyeDropper;

    @FXML
    public Canvas canvas;

    @FXML
    public Label timerLabel;

    private Stack<Canvas> canvasStack = new Stack<>();

    private Stack<Canvas> redoStack = new Stack<>();


    private double beginningX, beginningY;

    private windowTools windowTools;

    private double selectBeginX, selectBeginY, selectEndX, selectEndY;

    private WritableImage selectedImage = null;

    private WritableImage clipboardImage = null;

    private Timeline autoSaveTimer;

    private webServer webServer;

    Canvas previewCanvas = new Canvas();

    GraphicsContext previewGraphics = previewCanvas.getGraphicsContext2D();

    public windowFunctions windowFunctions = new windowFunctions();

    public ExecutorService logicExecutor = Executors.newSingleThreadExecutor();

    private static int countdown = 300;

    @FXML
    public void undoClick() {
        if (!canvasStack.isEmpty()) {
            Canvas undoCanvas = canvasStack.pop();
            redoStack.push(undoCanvas);

            graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

            if (!canvasStack.isEmpty()) {
                Canvas prevCanvas = canvasStack.peek();
                graphicsContext.drawImage(prevCanvas.snapshot(null, null), 0, 0);
            }
        } else {
            System.out.println("ERR");
        }
        logEvent(LocalDateTime.now(), "Undo clicked.");
    }

    @FXML
    public void redoClick(){
        if (!redoStack.isEmpty()) {
            Canvas redoCanvas = redoStack.pop();
            canvasStack.push(redoCanvas);

            graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

            graphicsContext.drawImage(redoCanvas.snapshot(null, null), 0, 0);
        } else {
            System.out.println("ERR");
        }
        logEvent(LocalDateTime.now(), "Redo clicked.");
    }

    @FXML
    public void clearCanvas(){
        clear();
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    @FXML
    public void saveCanvasState(){
        if (canvas.getWidth() > 0 && canvas.getHeight() > 0) {
            WritableImage snapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, snapshot);
            Canvas newCanvas = new Canvas(canvas.getWidth(), canvas.getHeight());
            GraphicsContext newCanvasContext = newCanvas.getGraphicsContext2D();
            newCanvasContext.drawImage(snapshot, 0, 0);
            canvasStack.push(newCanvas);
        } else {
            System.out.println("Canvas dimensions are not positive, cannot save state");
        }
    }

    enum shapeType{
        LINE, RECTANGLE, SQUARE, TRIANGLE, CIRCLE, ELLIPSE, STRAIGHTLINE, STAR, POLYGON, TEXT, COLORPICKER, SELECT
    }
    private shapeType currentShape = shapeType.LINE;


    @FXML
    public void initialize() {
        windowTools = new windowTools(stackPane);
        this.graphicsContext = canvas.getGraphicsContext2D();
        paintInitialization(canvas);
        canvasInitialize();
        mouseFunctions();
        initializeWindowTools();
        initializeShapeSelector();
        dashInitializer();
        initializeAutoSave();
        startWebServer(7070);
        canvas.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.C) {
                copySelectedArea();
            }
            if (event.isControlDown() && event.getCode() == KeyCode.V) {
                pasteSelectedArea(100, 100);
            }
            if (event.isControlDown() && event.getCode() == KeyCode.R) {
                redoClick();
            }
            if (event.isControlDown() && event.getCode() == KeyCode.S) {
                windowTools.saveClick();
            }
            if (event.isControlDown() && event.getCode() == KeyCode.U) {
                undoClick();
            }
        });
    }

    private void dashInitializer(){
        dashToggle.setOnAction(e -> dashToggle());
    }

    private void dashToggle(){
        boolean dashedLine = dashToggle.isSelected();
        if (dashedLine) {
            graphicsContext.setLineDashes(10); // Set a more reasonable dash length
            logEvent(LocalDateTime.now(), "Dashed line selected.");
        } else {
            graphicsContext.setLineDashes(0); // Reset to solid line
            logEvent(LocalDateTime.now(), "Solid line selected.");
        }
    }

    @FXML
    private void initializeWindowTools() {
        saveButton.setOnAction(e -> windowTools.saveClick());
        saveAsButton.setOnAction(e -> windowTools.saveAsClick());
        selectFile.setOnAction(e -> windowTools.selectClick());
        exitButton.setOnAction(e -> windowTools.exitClick((Stage) exitButton.getScene().getWindow()));
        aboutButton.setOnAction(e -> windowTools.aboutClick());
        horizontalFlip.setOnAction(e -> flipCanvasHorizontally(graphicsContext, canvas));
        verticalFlip.setOnAction(e -> flipCanvasVertically(graphicsContext, canvas));
    }

    @FXML
    public void paintInitialization(Canvas canvas) {
        canvas.setWidth(displayPane.getPrefWidth()); // sets the canvas measurements to the displaypane's
        canvas.setHeight(displayPane.getPrefHeight());
        graphicsContext.setStroke(Color.BLACK); // initial pen color is black
        graphicsContext.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        widthSlider.valueProperty().addListener((observable, oldWidth, newWidth) -> { // creates a listener for the slider
            graphicsContext.setLineWidth(newWidth.doubleValue()); // changes the width of the pen depending on the slider
            widthText.setText(newWidth.intValue() + "px");
        });
        colorPicker.valueProperty().addListener((observable, oldColor, newColor) -> {
            graphicsContext.setStroke(newColor);
        });
    }

    private void startWebServer(int port) {
        try {
            webServer = new webServer(port, this);
            webServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            System.out.println("Web server started on port " + port);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to start web server.");
        }
    }

    public void stopWebServer() {
        if (webServer != null) {
            webServer.stop();
            System.out.println("Web server stopped.");
        }
    }

    @FXML
    public void canvasInitialize(){
        canvas.setStyle("-fx-background-color: white;");
        canvas.widthProperty().bind(displayPane.widthProperty());
        canvas.heightProperty().bind(displayPane.heightProperty());
        previewCanvas.widthProperty().bind(canvas.widthProperty());
        previewCanvas.heightProperty().bind(canvas.heightProperty());
    }

    @FXML
    private void initializeShapeSelector() {
        lineSelect.setOnAction(e -> currentShape = shapeType.LINE);
        lineSelect.setTooltip(new Tooltip("Line tool"));
        rectangleSelect.setOnAction(e -> currentShape = shapeType.RECTANGLE);
        rectangleSelect.setTooltip(new Tooltip("Rectangle tool"));
        squareSelect.setOnAction(e -> currentShape = shapeType.SQUARE);
        squareSelect.setTooltip(new Tooltip("Square tool"));
        circleSelect.setOnAction(e -> currentShape = shapeType.CIRCLE);
        circleSelect.setTooltip(new Tooltip("Circle tool"));
        ellipseSelect.setOnAction(e -> currentShape = shapeType.ELLIPSE);
        ellipseSelect.setTooltip(new Tooltip("Ellipse tool"));
        triangleSelect.setOnAction(e -> currentShape = shapeType.TRIANGLE);
        triangleSelect.setTooltip(new Tooltip("Triangle tool"));
        straightSelect.setOnAction(e -> currentShape = shapeType.STRAIGHTLINE);
        straightSelect.setTooltip(new Tooltip("Straight Line tool"));
        starSelect.setOnAction(e -> {
            currentShape = shapeType.STAR;
            selectSides();
        });
        starSelect.setTooltip(new Tooltip("Star tool"));
        polygonSelect.setOnAction(e ->{
            currentShape = shapeType.POLYGON;
            selectSides();
        });
        polygonSelect.setTooltip(new Tooltip("Polygon tool"));
        textSelect.setOnAction(e-> currentShape = shapeType.TEXT);
        textSelect.setTooltip(new Tooltip("Text tool"));
        eyeDropper.setOnAction(e -> currentShape = shapeType.COLORPICKER);
        eyeDropper.setTooltip(new Tooltip("Eye Dropper tool"));
        selectTool.setOnAction(e -> currentShape = shapeType.SELECT);
        selectTool.setTooltip(new Tooltip("Select tool"));
    }

    public void mouseFunctions() {
        canvas.setOnMousePressed((this::mousePressed));
        canvas.setOnMouseDragged((this::mouseDragged));
        canvas.setOnMouseReleased((this::mouseReleased));
    }

    // line drawing
    public void mousePressed(MouseEvent mouseEvent) {
        beginningX = mouseEvent.getX();
        beginningY = mouseEvent.getY(); // Start of the line
        if (currentShape == shapeType.SELECT) {
            selectBeginX = beginningX;
            selectBeginY = beginningY;
            selectedImage = null;
        }
        logEvent(LocalDateTime.now(), "Mouse pressed.");
    }

    public void mouseDragged(MouseEvent mouseEvent) {
        double movingX = mouseEvent.getX();
        double movingY = mouseEvent.getY();

        if (currentShape == shapeType.SELECT) {
            graphicsContext.clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
            drawSelectArea(graphicsContext, selectBeginX, selectBeginY, movingX, movingY);
            if (selectedImage != null) {
                double offsetX = (selectEndX - selectBeginX) / 2;
                double offsetY = (selectEndY - selectBeginY) / 2;
                graphicsContext.drawImage(selectedImage, movingX - offsetX, movingY - offsetY);
            }
        }
        else if (currentShape == shapeType.LINE) {
            graphicsContext.strokeLine(beginningX, beginningY, movingX, movingY);
            beginningX = movingX;
            beginningY = movingY;
        }
        else {
            drawShape(previewGraphics, beginningX, beginningY, movingX - beginningX, movingY - beginningY);
        }
        logEvent(LocalDateTime.now(), "Mouse Dragged with shape: " + currentShape);
    }

    public void mouseReleased(MouseEvent mouseEvent) {
        double endX = mouseEvent.getX();
        double endY = mouseEvent.getY();

        double rectWidth = Math.abs(selectEndX - selectBeginX);
        double rectHeight = Math.abs(selectEndY - selectBeginY);

        double x = Math.min(selectBeginX, selectEndX);
        double y = Math.min(selectBeginY, selectEndY);

        if (currentShape == shapeType.SELECT) {
            selectEndX = endX;
            selectEndY = endY;
            SnapshotParameters params = new SnapshotParameters();
            params.setViewport(new Rectangle2D(x, y, rectWidth, rectHeight));
            selectedImage = canvas.snapshot(params, null);

            graphicsContext.clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
        } else if (currentShape == shapeType.LINE) {
            graphicsContext.strokeLine(beginningX, beginningY, endX, endY);        }
        else {
            drawShape(graphicsContext, beginningX, beginningY, endX - beginningX, endY - beginningY);
        }
        previewGraphics.clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());

        saveCanvasState();

        logEvent(LocalDateTime.now(), "Mouse Released with shape: " + currentShape);
    }

    private int numberOfSides = 6;

    private void selectSides(){
        TextInputDialog textDialog = new TextInputDialog("x");
        textDialog.setTitle("Shape Sides");
        textDialog.setHeaderText("Please specify the number of sides for the shape.");
        textDialog.setContentText("# of Sides:");

        Optional<String> sides = textDialog.showAndWait();
        sides.ifPresent(input -> {
            try{
                int polySides = Integer.parseInt(input);
                if(polySides > 3){
                    numberOfSides = polySides;
                }
                else{
                    sidesAlert("Invalid Input", "Number of sides must be greater than 3.");
                }
            } catch (NumberFormatException e){
                sidesAlert("No Input", "Please enter an integer.");
            }
        });
        logEvent(LocalDateTime.now(), "Sides selected.");
    }

    private void sidesAlert(String title, String errorMessage){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }

    @FXML
    public void clear() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Canvas");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("This will erase the entire canvas.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
        logEvent(LocalDateTime.now(), "Canvas cleared.");
    }

    private void drawShape(GraphicsContext shapeContext, double x, double y, double width, double height) {
        switch (currentShape) {
            case STAR:
                drawStar(shapeContext, x + width / 2, y + height / 2, Math.abs(width), Math.abs(height), 5);
                break;
            case RECTANGLE:
                drawRectangle(shapeContext, x, y, width, height);
                break;
            case SQUARE:
                drawSquare(shapeContext, x, y, Math.min(width, height));
                break;
            case TRIANGLE:
                drawTriangle(shapeContext, x, y, x + width, y, x + width / 2, y - height);
                break;
            case CIRCLE:
                drawCircle(shapeContext, x + width / 2, y + height / 2, Math.max(width, height) / 2);
                break;
            case ELLIPSE:
                drawEllipse(shapeContext, x, y, width, height);
                break;
            case STRAIGHTLINE:
                drawStraightLine(shapeContext, beginningX, beginningY, beginningX + width, beginningY + height);
                break;
            case POLYGON:
                drawPolygon(shapeContext, x, y, width, height);
                break;
            case COLORPICKER:
                colorSelect();
            case SELECT:
                drawSelectArea(shapeContext, selectBeginX, selectBeginY,selectEndX, selectEndY);
        }
    }

    public void drawRectangle(GraphicsContext gc, double x, double y, double width, double height) {
        if (width < 0) {
            width = -width;
            x = x - width;
        }
        if (height < 0) {
            height = -height;
            y = y - height;
        }
        gc.strokeRect(x, y, width, height);
        logEvent(LocalDateTime.now(), "Rectangle drawn.");
    }

    public void drawSquare(GraphicsContext gc, double x, double y, double size) {
        if (size < 0) {
            size = -size;
            x = x - size;
        }
        if (size < 0) {
            size = -size;
            y = y - size;
        }
        gc.strokeRect(x, y, size, size);
        logEvent(LocalDateTime.now(), "Square drawn.");
    }

    public void drawTriangle(GraphicsContext gc, double x1, double y1, double x2, double y2, double x3, double y3) {
        gc.strokePolygon(new double[]{x1, x2, x3}, new double[]{y1, y2, y3}, 3);
        logEvent(LocalDateTime.now(), "Triangle drawn.");
    }

    public void drawCircle(GraphicsContext gc, double x, double y, double radius) {
        if (radius < 0) {
            radius= -radius;
            x = x - radius;
        }
        if (radius < 0) {
            radius = -radius;
            y = y - radius;
        }
        gc.strokeOval(x - radius, y - radius, 2 * radius, 2 * radius);
        logEvent(LocalDateTime.now(), "Circle drawn.");
    }

    public void drawEllipse(GraphicsContext gc, double x, double y, double width, double height) {
        if (width < 0) {
            width = -width;
            x = x - width;
        }
        if (height < 0) {
            height = -height;
            y = y - height;
        }
        gc.strokeOval(x, y, width, height);
        logEvent(LocalDateTime.now(), "Ellipse drawn.");
    }

    public void drawStraightLine(GraphicsContext gc, double x1, double y1, double x2, double y2) {
        gc.strokeLine(x1, y1, x2, y2);
        logEvent(LocalDateTime.now(), "Straight line drawn.");
    }

    public void drawStar(GraphicsContext gc, double centerX, double centerY, double width, double height, int numPoints) {
        numPoints = numberOfSides;
        double outerRadius = Math.max(width, height) / 2;
        double innerRadius = outerRadius / 2.5;

        double[] xPoints = new double[numPoints * 2];
        double[] yPoints = new double[numPoints * 2];

        double angleStep = Math.PI / numPoints;

        for (int i = 0; i < numPoints * 2; i++) {
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            double angle = i * angleStep;

            xPoints[i] = centerX + radius * Math.cos(angle);
            yPoints[i] = centerY - radius * Math.sin(angle);
        }
        gc.strokePolygon(xPoints, yPoints, numPoints * 2);
        logEvent(LocalDateTime.now(), "Star drawn.");
    }

    public void drawPolygon(GraphicsContext gc, double x, double y, double width, double height) {
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        double radius = Math.min(width, height) / 2;

        double angleStep = 2 * Math.PI / numberOfSides;
        double[] xPoints = new double[numberOfSides];
        double[] yPoints = new double[numberOfSides];

        for (int i = 0; i < numberOfSides; i++) {
            xPoints[i] = centerX + radius * Math.cos(i * angleStep - Math.PI / 2);
            yPoints[i] = centerY + radius * Math.sin(i * angleStep - Math.PI / 2);
        }

        gc.strokePolygon(xPoints, yPoints, numberOfSides);
        logEvent(LocalDateTime.now(), "Polygon drawn.");
    }

    public void colorSelect(){

    }

    public void drawSelectArea(GraphicsContext graphicsContext, double startX, double startY, double endX, double endY){
        graphicsContext.setLineDashes(10);
        graphicsContext.setStroke(Color.BLUE);
        graphicsContext.strokeRect(Math.min(startX, endX), Math.min(startY, endY), Math.abs(endX - startX), Math.abs(endY - startY));
        graphicsContext.setLineDashes(0);
        graphicsContext.setStroke(Color.BLACK);
        logEvent(LocalDateTime.now(), "Selection drawn.");
    }

    public void copySelectedArea(){
        if (selectedImage != null) {
            clipboardImage = new WritableImage(
                    selectedImage.getPixelReader(),
                    (int) selectedImage.getWidth(),
                    (int) selectedImage.getHeight()
            );
            System.out.println("Image copied to clipboard.");
        } else {
            System.out.println("No area selected to copy.");
        }
        logEvent(LocalDateTime.now(), "Selection copied.");
    }

    public void pasteSelectedArea(double x, double y){
        if (clipboardImage != null) {
            graphicsContext.drawImage(clipboardImage, x, y);
            saveCanvasState();
            System.out.println("Image pasted.");
        } else {
            System.out.println("No image in clipboard to paste.");
        }
        logEvent(LocalDateTime.now(), "Selection pasted.");
    }

    public void saveSelectedArea(){
        double selectedWidth = Math.abs(selectEndX - selectBeginX);
        double selectedHeight = Math.abs(selectEndY - selectBeginY);
        if(selectedWidth > 0 && selectedHeight > 0){
            WritableImage selectImage = new WritableImage((int) selectedWidth, (int) selectedHeight);
        }
        logEvent(LocalDateTime.now(), "Selection saved.");
    }

    @FXML
    public void newCanvas() {
        Canvas newCanvas = new Canvas(800, 600); // You can set the default dimensions
        GraphicsContext newGc = newCanvas.getGraphicsContext2D();
        newGc.setFill(Color.WHITE); // Fill with a default background color if necessary
        newGc.fillRect(0, 0, newCanvas.getWidth(), newCanvas.getHeight());

        StackPane canvasContainer = new StackPane(newCanvas);

        Tab newTab = new Tab("Canvas " + (tabPane.getTabs().size() + 1)); // Naming new tab dynamically
        newTab.setContent(canvasContainer);

        tabPane.getTabs().add(newTab);

        tabPane.getSelectionModel().select(newTab);

        initializeCanvas(newCanvas);
        logEvent(LocalDateTime.now(), "Canvas created.");
    }

    public void initializeCanvas(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        this.graphicsContext = gc;
        paintInitialization(canvas);
        mouseFunctions();
        logEvent(LocalDateTime.now(), "Canvas created.");
    }

    public Canvas getActiveCanvas() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            StackPane canvasContainer = (StackPane) selectedTab.getContent();
            return (Canvas) canvasContainer.getChildren().get(0);  // Assuming the canvas is the first child
        }
        return null;
    }

    public GraphicsContext getActiveGraphicsContext() {
        Canvas activeCanvas = getActiveCanvas();
        if (activeCanvas != null) {
            return activeCanvas.getGraphicsContext2D();
        }
        return null;
    }

    private void initializeAutoSave() {
        autoSaveTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            countdown--;
            updateLabel();
        }));
        if(countdown <= 0){
            windowTools.saveClick();
            countdown = 300;
            updateLabel();
        }

        autoSaveTimer.setCycleCount(Timeline.INDEFINITE);
        autoSaveTimer.play();
        updateLabel();

        autoSaveToggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            timerLabel.setVisible(isSelected);
            updateLabel();
        });

        updateLabel();
        timerLabel.setVisible(autoSaveToggle.isSelected());
    }

    private void updateLabel(){
        int minutes = countdown/60;
        int seconds = countdown%60;
        timerLabel.setText(String.format("Autosave in %02d:%02d", minutes, seconds));
    }

    public void resetAutosave(){
        countdown = 300;
        updateLabel();
    }

    public WritableImage getImageFromTab(int tabIndex) {
        if (tabIndex < 0 || tabIndex >= tabPane.getTabs().size()) {
            return null;
        }
        Platform.runLater(() -> {
        });
        Tab tab = tabPane.getTabs().get(tabIndex);
        StackPane canvasContainer = (StackPane) tab.getContent();
        Canvas canvas = (Canvas) canvasContainer.getChildren().get(0);
        WritableImage image = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, image);
        return image;
    }

    public List<String> getSelectedTabIndices() {
        List<String> selectedIndices = new ArrayList<>();
        for (int i = 0; i < tabPane.getTabs().size(); i++) {
            selectedIndices.add(String.valueOf(i));
        }
        return selectedIndices;
    }

    public void logEvent(LocalDateTime now, String event) {
        logicExecutor.submit(() -> {
            try (FileWriter writer = new FileWriter("log.txt", true)) {
                writer.write(now + " " + event + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void flipCanvasHorizontally(GraphicsContext graphicsContext, Canvas canvas) {
        graphicsContext.save();

        graphicsContext.scale(-1, 1);
        graphicsContext.drawImage(canvas.snapshot(null, null), -canvas.getWidth(), 0);

        graphicsContext.restore();
    }

    public void flipCanvasVertically(GraphicsContext graphicsContext, Canvas canvas) {
        graphicsContext.save();

        graphicsContext.scale(1, -1);
        graphicsContext.drawImage(canvas.snapshot(null, null), 0, -canvas.getHeight());

        graphicsContext.restore();
    }

}