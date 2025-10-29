package net.http;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SocketHttpClient {

    public static HttpResponse get(String host, int port, String path, boolean keepAlive) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 5000);
            socket.setSoTimeout(5000);
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            // Build request manually
            StringBuilder req = new StringBuilder();
            req.append("GET ").append(path).append(" HTTP/1.1\r\n");
            req.append("Host: ").append(host).append("\r\n");
            req.append("User-Agent: SimpleSocketBrowser/0.1\r\n");
            req.append("Accept: */*\r\n");
            req.append("Connection: ").append(keepAlive ? "keep-alive" : "close").append("\r\n");
            req.append("\r\n");

            byte[] reqBytes = req.toString().getBytes(StandardCharsets.US_ASCII);
            System.out.println("=== REQUEST ===");
            System.out.println(new String(reqBytes, StandardCharsets.US_ASCII));
            out.write(reqBytes);
            out.flush();

            BufferedInputStream bin = new BufferedInputStream(in);
            // Read status line and headers
            String statusLine = readLine(bin);
            Map<String,String> headers = new LinkedHashMap<>();
            String line;
            while (!(line = readLine(bin)).isEmpty()) {
                int idx = line.indexOf(':');
                if (idx > 0) {
                    String name = line.substring(0, idx).trim().toLowerCase(Locale.ROOT);
                    String val = line.substring(idx+1).trim();
                    headers.put(name, val);
                }
            }
            // Body handling
            byte[] body;
            String te = headers.getOrDefault("transfer-encoding", "");
            String cl = headers.get("content-length");
            if ("chunked".equalsIgnoreCase(te)) {
                body = readChunked(bin);
            } else if (cl != null) {
                int length = Integer.parseInt(cl);
                body = readFixed(bin, length);
            } else {
                // read until socket close
                body = readUntilClose(bin);
            }
            return new HttpResponse(statusLine, headers, body);
        }
    }

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int prev = -1;
        int cur;
        while ((cur = in.read()) != -1) {
            if (prev == '\r' && cur == '\n') {
                break;
            }
            if (prev != -1) baos.write(prev);
            prev = cur;
        }
        if (prev != -1 && prev != '\r') baos.write(prev);
        return baos.toString(StandardCharsets.ISO_8859_1);
    }

    private static byte[] readFixed(InputStream in, int length) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int remaining = length;
        while (remaining > 0) {
            int r = in.read(buf, 0, Math.min(buf.length, remaining));
            if (r == -1) throw new EOFException("Unexpected EOF in fixed body");
            out.write(buf, 0, r);
            remaining -= r;
        }
        return out.toByteArray();
    }

    private static byte[] readUntilClose(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int r;
        while ((r = in.read(buf)) != -1) {
            out.write(buf, 0, r);
        }
        return out.toByteArray();
    }

    private static byte[] readChunked(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while (true) {
            String line = readLine(in);
            int semi = line.indexOf(';');
            String hex = (semi > 0 ? line.substring(0, semi) : line).trim();
            int size = Integer.parseInt(hex, 16);
            if (size == 0) {
                // consume trailing headers
                while (!readLine(in).isEmpty()) {}
                break;
            }
            byte[] chunk = readFixed(in, size);
            out.write(chunk);
            // consume CRLF after chunk (readLine will have consumed prior CRLFs correctly, but ensure)
            readLine(in); // consumes the trailing CRLF
        }
        return out.toByteArray();
    }
}
