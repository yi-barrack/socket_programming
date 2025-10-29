package browser;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import org.jsoup.nodes.*;
import net.http.HttpResponse;
import net.http.SocketHttpClient;

import java.io.ByteArrayInputStream;
import java.net.URI;

public class Renderer {

    public static Node renderNode(org.jsoup.nodes.Node node, String baseHost) {
        if (node instanceof TextNode) {
            String text = ((TextNode) node).text().trim();
            if (text.isEmpty()) return null;
            Label lbl = new Label(text);
            lbl.setWrapText(true);
            lbl.setFont(Font.font(14));
            lbl.setPadding(new Insets(2,0,2,0));
            return lbl;
        } else if (node instanceof Element) {
            Element e = (Element) node;
            String tag = e.tagName().toLowerCase();
            switch (tag) {
                case "p":
                case "div":
                    Label p = new Label(e.text());
                    p.setWrapText(true);
                    p.setFont(Font.font(14));
                    p.setPadding(new Insets(6,0,6,0));
                    return p;
                case "h1":
                    Label h1 = new Label(e.text());
                    h1.setFont(Font.font(24));
                    h1.setPadding(new Insets(6,0,6,0));
                    return h1;
                case "h2":
                    Label h2 = new Label(e.text());
                    h2.setFont(Font.font(20));
                    h2.setPadding(new Insets(6,0,6,0));
                    return h2;
                case "img":
                    String src = e.attr("src");
                    try {
                        // build absolute url if needed
                        URI uri = new URI(src);
                        String host = uri.getHost();
                        String path = uri.getRawPath();
                        int port = uri.getPort() == -1 ? 80 : uri.getPort();
                        if (host == null) { // relative
                            host = baseHost;
                            if (path == null) path = src;
                        }
                        HttpResponse imgResp = SocketHttpClient.get(host, port, path, false);
                        ByteArrayInputStream bais = new ByteArrayInputStream(imgResp.body);
                        Image img = new Image(bais);
                        ImageView iv = new ImageView(img);
                        iv.setPreserveRatio(true);
                        iv.setFitWidth(600);
                        iv.setSmooth(true);
                        return iv;
                    } catch (Exception ex) {
                        Label err = new Label("[image load error: " + src + "]");
                        return err;
                    }
                default:
                    // fallback: render children
                    javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(2);
                    for (org.jsoup.nodes.Node c : e.childNodes()) {
                        Node n = renderNode(c, baseHost);
                        if (n != null) box.getChildren().add(n);
                    }
                    return box.getChildren().isEmpty() ? null : box;
            }
        }
        return null;
    }
}
