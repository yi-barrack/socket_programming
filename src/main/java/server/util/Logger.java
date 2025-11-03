package server.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// 콘솔에 찍는 아주 단순한 로거
public final class Logger {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Logger() {}

    public static void info(String message) {
        System.out.println(timestamp() + " [INFO ] " + message);
    }

    public static void warn(String message) {
        System.out.println(timestamp() + " [WARN ] " + message);
    }

    public static void error(String message, Throwable t) {
        System.err.println(timestamp() + " [ERROR] " + message);
        if (t != null) {
            t.printStackTrace(System.err);
        }
    }

    private static String timestamp() {
        // 로그마다 같은 형식으로 시간 찍어주기
        return LocalDateTime.now().format(FORMATTER);
    }
}
