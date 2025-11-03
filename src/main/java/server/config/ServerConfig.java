package server.config;

import java.nio.file.Path;
import java.nio.file.Paths;

// 서버 곳곳에서 쓰는 설정값 모음. 다 상수라서 그대로 가져다 쓴다.
public final class ServerConfig {

    private ServerConfig() {}

    // 서버가 열어둘 포트 (HTTPS라 443으로 맞춰놨음)
    public static final int PORT = 443;

    // 소켓 accept 대기열 최대치
    public static final int ACCEPT_BACKLOG = 128;

    // 워커 스레드 개수 (코어*2, 최소 4개로 안전빵)
    public static final int WORKER_THREADS = Math.max(4, Runtime.getRuntime().availableProcessors() * 2);

    // 소켓 읽기 타임아웃(ms)
    public static final int SOCKET_TIMEOUT_MILLIS = 15_000;

    // 요청 첫 줄 최대 길이
    public static final int MAX_REQUEST_LINE_LENGTH = 8 * 1024;

    // 헤더 한 줄 최대 길이
    public static final int MAX_HEADER_LINE_LENGTH = 8 * 1024;

    // 허용할 헤더 개수 MAX
    public static final int MAX_HEADERS = 100;

    // 헤더 전부 합친 크기 상한
    public static final int MAX_HEADER_SECTION_SIZE = 32 * 1024;

    // 본문(Content-Length) 제한 (1MB)
    public static final int MAX_BODY_SIZE = 1 * 1024 * 1024;

    // keep-alive 유지 시간(ms)
    public static final int KEEP_ALIVE_TIMEOUT_MILLIS = 15_000;

    // 같은 연결에서 처리 가능한 최대 요청 횟수
    public static final int KEEP_ALIVE_MAX_REQUESTS = 100;

    // 정적 파일 기본 경로
    public static final Path WEB_ROOT = Paths.get("www");

    // 응답 헤더에 찍을 서버 이름
    public static final String SERVER_NAME = "SimpleJavaServer/0.1";

    // HTTPS 켤지 여부
    public static final boolean HTTPS_ENABLED = true;

    // TLS 때문에 필요한 키스토어 정보
    public static final String KEYSTORE_TYPE = "PKCS12";
    public static final Path KEYSTORE_PATH = Paths.get("config", "simple-server.p12");
    public static final String KEYSTORE_PASSWORD = "123123";

    // 사용할 TLS 프로토콜 리스트
    public static final String[] ENABLED_PROTOCOLS = {"TLSv1.3", "TLSv1.2"};
}
