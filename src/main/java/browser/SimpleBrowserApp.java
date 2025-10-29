package browser;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.http.HttpResponse;
import net.http.SocketHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;

import java.io.ByteArrayInputStream;
import java.net.URI;


public class SimpleBrowserApp extends Application {

    private VBox contentBox;
    private TextArea rawArea;

    @Override
    public void start(Stage stage) {
        TextField urlField = new TextField("http://example.com/");
        Button go = new Button("Load");
        HBox top = new HBox(8, urlField, go);
        top.setPadding(new Insets(8));

        contentBox = new VBox(10);
        contentBox.setPadding(new Insets(8));

        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);

        rawArea = new TextArea();
        rawArea.setEditable(false);
        rawArea.setPrefRowCount(10);

        SplitPane split = new SplitPane(scroll, rawArea);
        split.setDividerPositions(0.7);

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(split);

        go.setOnAction(ev -> {
            String url = urlField.getText().trim();
            loadUrl(url);
        });

        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.setTitle("SimpleSocketBrowser");
        stage.show();
    }

    private void loadUrl(String urlStr) {
        contentBox.getChildren().clear();
        rawArea.clear();
        new Thread(() -> {
            try {
                URI uri = new URI(urlStr);
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 80 : uri.getPort();
                String path = uri.getRawPath();
                if (path == null || path.isEmpty()) path = "/";

                HttpResponse resp = SocketHttpClient.get(host, port, path, false);
                // show raw
                StringBuilder sb = new StringBuilder();
                sb.append(resp.statusLine).append("\n");
                resp.headers.forEach((k, v) -> sb.append(k).append(": ").append(v).append("\n"));
                sb.append("\n");
                sb.append(new String(resp.body, java.nio.charset.StandardCharsets.UTF_8));
                javafx.application.Platform.runLater(() -> rawArea.setText(sb.toString()));

                // parse and render basic tags
                org.jsoup.nodes.Document doc = Jsoup.parse(new String(resp.body));
                renderDocument(doc, host);
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> rawArea.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    private void renderDocument(org.jsoup.nodes.Document doc, String host) {
        Element body = doc.body();
        if (body == null) return;
        for (Node node : body.childNodes()) {
            javafx.scene.Node view = Renderer.renderNode(node, host);
            if (view != null) {
                javafx.application.Platform.runLater(() -> contentBox.getChildren().add(view));
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
