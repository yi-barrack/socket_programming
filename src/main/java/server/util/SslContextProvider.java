package server.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import server.config.ServerConfig;

// HTTPS 모드에서 쓸 SSLServerSocketFactory를 만들어 주는 헬퍼
public final class SslContextProvider {

    private SslContextProvider() {}

    public static SSLServerSocketFactory serverSocketFactory() {
        try {
            KeyStore keyStore = KeyStore.getInstance(ServerConfig.KEYSTORE_TYPE);
            try (InputStream in = Files.newInputStream(ServerConfig.KEYSTORE_PATH)) {
                // 키스토어 파일을 읽어서 비밀키와 인증서를 로드한다.
                keyStore.load(in, ServerConfig.KEYSTORE_PASSWORD.toCharArray());
            }

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, ServerConfig.KEYSTORE_PASSWORD.toCharArray());

            SSLContext context = SSLContext.getInstance("TLS");
            // 서버 쪽 키 매니저만 있으면 되어서 나머지 파라미터는 null 로 둔다.
            context.init(kmf.getKeyManagers(), null, null);
            return context.getServerSocketFactory();
        } catch (IOException e) {
            throw new IllegalStateException("키스토어를 읽을 수 없습니다: " + ServerConfig.KEYSTORE_PATH, e);
        } catch (Exception e) {
            throw new IllegalStateException("SSL 컨텍스트 초기화에 실패했습니다.", e);
        }
    }
}
