package net.http;

import java.util.Map;

public class HttpResponse {
    public String statusLine;
    public Map<String,String> headers;
    public byte[] body;

    public HttpResponse(String statusLine, Map<String,String> headers, byte[] body) {
        this.statusLine = statusLine;
        this.headers = headers;
        this.body = body;
    }
}
