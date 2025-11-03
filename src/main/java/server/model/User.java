package server.model;

import java.time.LocalDateTime;
import java.util.Objects;

// 사용자 한 명의 기본 정보를 담는 모델
public final class User {
    private final String username;
    private final String passwordHash;
    private final LocalDateTime createdAt;
    private final LocalDateTime lastLoginAt;

    public User(String username, String passwordHash) {
        this(username, passwordHash, LocalDateTime.now(), null);
    }

    public User(String username, String passwordHash, LocalDateTime createdAt, LocalDateTime lastLoginAt) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public User withLastLogin(LocalDateTime lastLoginAt) {
        // 마지막 로그인 시간만 바꾼 새 객체를 만들어 돌려준다.
        return new User(username, passwordHash, createdAt, lastLoginAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", createdAt=" + createdAt +
                ", lastLoginAt=" + lastLoginAt +
                '}';
    }
}
