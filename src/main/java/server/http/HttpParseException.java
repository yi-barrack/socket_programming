package server.http;

// HTTP 요청을 읽다가 문법이 이상할 때 던지는 예외
public class HttpParseException extends Exception {
    public HttpParseException(String message) {
        super(message);
    }
}
