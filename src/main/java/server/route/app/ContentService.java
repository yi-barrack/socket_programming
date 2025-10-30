package server.route.app;


import server.model.ManifestItem;
import server.util.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

/**
 * 파일 I/O 및 Manifest 관리를 담당하는 서비스 클래스입니다.
 * 실제 파일 시스템을 사용하도록 구현했습니다.
 */
public final class ContentService {

    // 이 경로는 ManifestManager와 동일하게 설정되어야 합니다.
    private static final String SERVER_ROOT_PATH = "./www";
    private static final Path POSTS_DIR = Paths.get(SERVER_ROOT_PATH, "posts");

    /**
     * 새 글을 저장하고 Manifest를 업데이트합니다.
     * (기존 코드 유지)
     */
    public String createNewPost(String title, String content) throws IOException {
        String safeTitle = title.replaceAll("[^a-zA-Z0-9가-힣\\s]", "").trim().replaceAll("\\s+", "_").toLowerCase();
        String fileName = safeTitle + "_" + System.currentTimeMillis() + ".txt";

        Path fullPostPath = POSTS_DIR.resolve(fileName);
        String publicFilePath = "/posts/" + fileName;

        Files.createDirectories(POSTS_DIR);
        Files.write(fullPostPath, content.getBytes(StandardCharsets.UTF_8));
        Logger.info("[ContentService] Post content saved to: " + fullPostPath);

        ManifestManager.updateManifest(title, publicFilePath, "text");

        return publicFilePath;
    }

    /**
     * Manifest 및 실제 파일 시스템에서 글을 삭제합니다.
     * @param postId 삭제할 글의 고유 ID
     * @return 삭제된 ManifestItem 객체
     * @throws IOException 파일 I/O 오류 발생 시
     * @throws NoSuchElementException 해당 ID의 항목이 Manifest에 없을 경우
     */
    public ManifestItem deletePost(long postId) throws IOException, NoSuchElementException {

        // 1. Manifest에서 항목을 삭제하고, 삭제된 항목 정보를 얻습니다.
        ManifestItem deletedItem = ManifestManager.deleteItem(postId);
        System.out.println("1");
        if (deletedItem == null) {
            throw new NoSuchElementException("Manifest item not found for ID: " + postId);
        }

        // 2. 실제 파일 시스템에서 파일 삭제
        // 글 항목만 삭제할 것이므로, 타입이 "text"인지 확인은 건너뜁니다.
        // 클라이언트에서 보낸 path를 사용하여 서버 내부의 전체 경로를 만듭니다.
        Path fileToDeletePath = Paths.get(SERVER_ROOT_PATH, deletedItem.path);
        System.out.println("3");
        try {
            boolean deleted = Files.deleteIfExists(fileToDeletePath);
            if (deleted) {
                Logger.info("[ContentService] Post file deleted: " + fileToDeletePath);
            } else {
                Logger.warn("[ContentService] Post file not found on disk, but deleted from Manifest: " + fileToDeletePath);
            }
        } catch (SecurityException e) {
            // 파일 삭제 권한이 없는 경우
            Logger.error("[ContentService] Permission denied when deleting file: " + fileToDeletePath, e);
            // Manifest는 이미 업데이트되었으므로, 파일 삭제 실패를 알리고 500 에러를 던질 수 있습니다.
            throw new IOException("Security error during file deletion.", e);
        }

        return deletedItem;
    }
}