package server.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 간단한 콘솔 로깅 유틸리티.
 * 시간 포맷을 통일하고 로그 레벨별 출력 채널을 나눈다.
 */
public final class Logger {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Logger() {}

    public static void info(String message) {
        System.out.println(timestamp() + " [INFO ] " + message);
    }

    public static void warn(String message) {
        System.out.println(timestamp() + " [WARN ] " + message);
    }

    // 메시지만을 받는 오버로딩된 error 메서드를 추가하여
    // Logger.error("...") 형태로 호출할 수 있도록 합니다.
    public static void error(String message) {
        System.err.println(timestamp() + " [ERROR] " + message);
    }

    public static void error(String message, Throwable t) {
        System.err.println(timestamp() + " [ERROR] " + message);
        if (t != null) {
            t.printStackTrace(System.err);
        }
    }

    private static String timestamp() {
        return LocalDateTime.now().format(FORMATTER);
    }
}
