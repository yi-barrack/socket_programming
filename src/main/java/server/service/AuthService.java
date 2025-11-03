package server.service;

import java.util.Optional;

import server.model.Session;
import server.model.User;

// 회원가입, 로그인, 세션 검증을 한 곳에서 처리하는 서비스
public final class AuthService {
    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    public AuthService() {
        this.userRepository = new UserRepository();
        this.sessionManager = new SessionManager();
    }

    // 아이디/비밀번호를 받아 회원가입을 시도한다.
    public RegisterResult register(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return new RegisterResult(false, "아이디를 입력해주세요.");
        }

        if (password == null || password.length() < 6) {
            return new RegisterResult(false, "비밀번호는 최소 6자 이상이어야 합니다.");
        }

        if (userRepository.userExists(username)) {
            return new RegisterResult(false, "이미 존재하는 아이디입니다.");
        }

        boolean success = userRepository.registerUser(username, password);
        if (success) {
            return new RegisterResult(true, "회원가입이 완료되었습니다.");
        } else {
            return new RegisterResult(false, "회원가입 중 오류가 발생했습니다.");
        }
    }

    // 로그인 요청을 처리하고 성공하면 세션을 만들어 준다.
    public LoginResult login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return new LoginResult(false, null, "아이디와 비밀번호를 입력해주세요.");
        }

        boolean authenticated = userRepository.authenticateUser(username, password);
        if (authenticated) {
            Session session = sessionManager.createSession(username);
            return new LoginResult(true, session, "로그인 성공");
        } else {
            return new LoginResult(false, null, "아이디 또는 비밀번호가 잘못되었습니다.");
        }
    }

    // 세션 ID가 유효하면 사용자명을 돌려준다.
    public Optional<String> validateSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return Optional.empty();
        }

        Optional<Session> sessionOpt = sessionManager.getValidSession(sessionId);
        return sessionOpt.map(Session::getUsername);
    }

    // 세션 파일을 지워서 로그아웃 처리한다.
    public void logout(String sessionId) {
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            sessionManager.deleteSession(sessionId);
        }
    }

    // 저장소에서 사용자 정보를 그대로 읽어온다.
    public Optional<User> getUser(String username) {
        return userRepository.loadUser(username);
    }

    // 쌓여 있는 만료 세션들도 주기적으로 날려준다.
    public void cleanupExpiredSessions() {
        sessionManager.cleanupExpiredSessions();
    }

    // 회원가입 결과를 담아두는 단순 DTO
    public static class RegisterResult {
        private final boolean success;
        private final String message;

        public RegisterResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    // 로그인 처리 결과를 담는 DTO
    public static class LoginResult {
        private final boolean success;
        private final Session session;
        private final String message;

        public LoginResult(boolean success, Session session, String message) {
            this.success = success;
            this.session = session;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public Session getSession() {
            return session;
        }

        public String getMessage() {
            return message;
        }
    }
}
