package com.example.paintsprintfinal;

import fi.iki.elonen.NanoHTTPD;
import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;


public class webServer extends NanoHTTPD {
    private final paintFunctions paintFunctions;

    public webServer(int port, paintFunctions paintFunctions) {
        super(port);
        this.paintFunctions = paintFunctions;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        if (uri.equals("/")) {
            return newFixedLengthResponse(Response.Status.OK, "text/html", generateHtmlPage());
        } else if (uri.startsWith("/image")) {
            Map<String, List<String>> params = session.getParameters();
            List<String> tabIndices = params.get("tab");
            if (tabIndices == null || tabIndices.isEmpty()) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "No images selected.");
            }
            String tabIndexStr = tabIndices.get(0);
            try {
                int tabIndex = Integer.parseInt(tabIndexStr);
                WritableImage image = paintFunctions.getImageFromTab(tabIndex);
                if (image == null) {
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Image not found.");
                }
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", outputStream);
                byte[] imageBytes = outputStream.toByteArray();
                return newFixedLengthResponse(Response.Status.OK, "image/png", new ByteArrayInputStream(imageBytes), imageBytes.length);
            } catch (NumberFormatException | IOException e) {
                e.printStackTrace();
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Server error.");
            }
        } else {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found");
        }
    }

    private String generateHtmlPage() {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>PaintApp Images</title></head><body>");
        html.append("<h1>Available Images</h1>");
        List<String> selectedTabs = paintFunctions.getSelectedTabIndices();
        if (selectedTabs.isEmpty()) {
            html.append("<p>No images selected.</p>");
        } else {
            for (String tabIndex : selectedTabs) {
                html.append("<div>");
                html.append("<h3>Image from Tab ").append(tabIndex).append("</h3>");
                html.append("<img src=\"/image?tab=").append(tabIndex).append("\" alt=\"Image ").append(tabIndex).append("\" style=\"max-width:600px;\"/>");
                html.append("</div><br/>");
            }
        }
        html.append("</body></html>");
        return html.toString();
    }
}