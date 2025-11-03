package server.http;

import server.config.ServerConfig;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

// 소켓에서 들어온 바이트를 읽어서 HttpRequest 객체로 바꿔주는 단순 파서
public final class HttpRequestParser {

    public HttpRequest parse(BufferedInputStream in) throws IOException, HttpParseException {
        // 첫 줄 읽어서 METHOD SP TARGET SP VERSION 순서를 확인한다.
        String requestLine = readLine(in, ServerConfig.MAX_REQUEST_LINE_LENGTH);
        if (requestLine == null) {
            return null;
        }
        String[] parts = requestLine.split(" ", 3);
        if (parts.length != 3) {
            throw new HttpParseException("Invalid request line");
        }
        String method = parts[0];
        String target = parts[1];
        String version = parts[2];

        if (!version.equals("HTTP/1.1") && !version.equals("HTTP/1.0")) {
            throw new HttpParseException("Unsupported HTTP version");
        }

        Map<String, String> headers = readHeaders(in);
        if (version.equals("HTTP/1.1") && !headers.containsKey("host")) {
            throw new HttpParseException("Missing Host header");
        }

        // Content-Length 보고 바디 길이를 정한다.
        int contentLength = parseContentLength(headers.get("content-length"));
        if (contentLength > ServerConfig.MAX_BODY_SIZE) {
            throw new HttpParseException("Request body too large");
        }

        byte[] body = readBody(in, contentLength);
        return new HttpRequest(method, target, version, headers, body);
    }

    private Map<String, String> readHeaders(BufferedInputStream in) throws IOException, HttpParseException {
        Map<String, String> headers = new LinkedHashMap<>();
        int total = 0;
        while (true) {
            // 빈 줄이 나올 때까지 헤더를 계속 읽는다.
            String line = readLine(in, ServerConfig.MAX_HEADER_LINE_LENGTH);
            if (line == null) {
                throw new HttpParseException("Unexpected EOF while reading headers");
            }
            if (line.isEmpty()) {
                return headers;
            }
            total += line.length();
            if (total > ServerConfig.MAX_HEADER_SECTION_SIZE) {
                throw new HttpParseException("Header section too large");
            }
            int colon = line.indexOf(':');
            if (colon <= 0) {
                throw new HttpParseException("Invalid header line");
            }
            String name = line.substring(0, colon).trim().toLowerCase(Locale.ROOT);
            String value = line.substring(colon + 1).trim();
            if (headers.size() >= ServerConfig.MAX_HEADERS && !headers.containsKey(name)) {
                throw new HttpParseException("Too many headers");
            }
            headers.put(name, value);
        }
    }

    private int parseContentLength(String value) throws HttpParseException {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        try {
            int len = Integer.parseInt(value);
            if (len < 0) {
                throw new NumberFormatException("negative");
            }
            return len;
        } catch (NumberFormatException e) {
            throw new HttpParseException("Invalid Content-Length");
        }
    }

    private byte[] readBody(InputStream in, int length) throws IOException {
        // 지금은 Content-Length가 있는 요청만 처리한다.
        if (length == 0) {
            return new byte[0];
        }
        byte[] body = new byte[length];
        int offset = 0;
        while (offset < length) {
            int read = in.read(body, offset, length - offset);
            if (read == -1) {
                throw new IOException("Unexpected EOF while reading body");
            }
            offset += read;
        }
        return body;
    }

    private String readLine(BufferedInputStream in, int maxLength) throws IOException, HttpParseException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        while (true) {
            // CRLF 나올 때까지 계속 읽는다. 너무 길면 바로 예외.
            int b = in.read();
            if (b == -1) {
                if (buffer.size() == 0) {
                    return null;
                }
                throw new HttpParseException("Unexpected EOF in line");
            }
            if (b == '\r') {
                int next = in.read();
                if (next == -1) {
                    throw new HttpParseException("Unexpected EOF after CR");
                }
                if (next == '\n') {
                    break;
                }
                buffer.write(b);
                buffer.write(next);
            } else {
                buffer.write(b);
            }
            if (buffer.size() > maxLength) {
                throw new HttpParseException("Line too long");
            }
        }
        return buffer.toString(StandardCharsets.US_ASCII);
    }
}
