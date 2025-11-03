package server.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import server.http.HttpRequest;
import server.http.HttpResponse;

// 쿠키 읽고 쓰는 데 필요한 잡다한 유틸
public final class CookieUtil {

    private CookieUtil() {}

    // 요청 헤더에서 쿠키 문자열을 파싱해서 Map으로 돌려준다.
    public static Map<String, String> parseCookies(HttpRequest request) {
        Map<String, String> cookies = new HashMap<>();
        String cookieHeader = request.header("cookie");
        
        if (cookieHeader == null || cookieHeader.trim().isEmpty()) {
            return cookies;
        }

        String[] pairs = cookieHeader.split(";");
        for (String pair : pairs) {
            String[] keyValue = pair.trim().split("=", 2);
            if (keyValue.length == 2) {
                try {
                    String name = URLDecoder.decode(keyValue[0].trim(), StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(keyValue[1].trim(), StandardCharsets.UTF_8);
                    cookies.put(name, value);
                } catch (Exception e) {
                    // 디코딩 실패 시 무시
                }
            }
        }
        
        return cookies;
    }

    // 이름으로 쿠키 하나만 바로 꺼내고 싶을 때 사용한다.
    public static String getCookie(HttpRequest request, String name) {
        Map<String, String> cookies = parseCookies(request);
        return cookies.get(name);
    }

    // 기본 옵션으로 쿠키를 심는다. max-age 같은 건 따로 안 건드린다.
    public static void setCookie(HttpResponse.Builder responseBuilder, String name, String value) {
        setCookie(responseBuilder, name, value, -1, "/", false, true);
    }

    // 옵션을 직접 넣어서 쿠키를 심고 싶을 때 쓰는 버전
    public static void setCookie(HttpResponse.Builder responseBuilder, String name, String value, 
                                int maxAgeSeconds, String path, boolean secure, boolean httpOnly) {
        try {
            StringBuilder cookie = new StringBuilder();
            cookie.append(URLEncoder.encode(name, StandardCharsets.UTF_8));
            cookie.append("=");
            cookie.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
            
            if (maxAgeSeconds >= 0) {
                cookie.append("; Max-Age=").append(maxAgeSeconds);
            }
            
            if (path != null && !path.isEmpty()) {
                cookie.append("; Path=").append(path);
            }
            
            if (secure) {
                cookie.append("; Secure");
            }
            
            if (httpOnly) {
                cookie.append("; HttpOnly");
            }
            
            responseBuilder.header("Set-Cookie", cookie.toString());
        } catch (Exception e) {
            Logger.error("Failed to set cookie: " + name, e);
        }
    }

    // 쿠키를 삭제하고 싶을 때 Max-Age를 0으로 만들어서 내려보낸다.
    public static void deleteCookie(HttpResponse.Builder responseBuilder, String name, String path) {
        setCookie(responseBuilder, name, "", 0, path, false, true);
    }

    // 우리 서버에서 쓰는 세션 쿠키(JSESSIONID) 심기
    public static void setSessionCookie(HttpResponse.Builder responseBuilder, String sessionId) {
        setCookie(responseBuilder, "JSESSIONID", sessionId, -1, "/", false, true);
    }

    // 세션 쿠키 날리기
    public static void deleteSessionCookie(HttpResponse.Builder responseBuilder) {
        deleteCookie(responseBuilder, "JSESSIONID", "/");
    }

    // 요청 헤더에서 JSESSIONID만 바로 꺼내기
    public static String getSessionId(HttpRequest request) {
        return getCookie(request, "JSESSIONID");
    }
}
