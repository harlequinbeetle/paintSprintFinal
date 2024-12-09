module com.example.paintsprintfinal {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires nanohttpd;
    requires org.junit.jupiter.api;
    requires org.testng;


    opens com.example.paintsprintfinal to javafx.fxml;
    exports com.example.paintsprintfinal;
}