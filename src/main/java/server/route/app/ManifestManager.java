package server.route.app;


import server.model.ManifestItem;
import server.util.JsonParser;
import server.util.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Manifest 파일 I/O와 ManifestItem 리스트 관리를 담당하는 서비스입니다.
 */
public final class ManifestManager {

    private static final String SERVER_ROOT_PATH = "./www";
    private static final Path MANIFEST_PATH = Paths.get(SERVER_ROOT_PATH, "content_manifest.json");
    private static final AtomicLong NEXT_ID = new AtomicLong(0);

    // Manifest 파일을 읽어 ManifestItem 리스트로 변환합니다.
    private static List<ManifestItem> loadManifest() throws IOException {
        // [로딩 및 파싱 로직은 이전과 동일]
        if (!Files.exists(MANIFEST_PATH)) {
            Files.createDirectories(MANIFEST_PATH.getParent());
            Logger.warn("[ManifestManager] Manifest file not found. Initializing empty list.");
            NEXT_ID.set(0);
            return new ArrayList<>();
        }

        String jsonContent = Files.readString(MANIFEST_PATH, StandardCharsets.UTF_8).trim();

        if (jsonContent.isEmpty()) {
            Logger.warn("[ManifestManager] Manifest file is empty. Returning empty list.");
            NEXT_ID.set(0);
            return new ArrayList<>();
        }

        List<ManifestItem> items = new ArrayList<>();
        try {
            // 3. JSON 문자열을 ManifestItem 리스트로 변환
            items = JsonParser.deserializeList(jsonContent);
        } catch (Exception e) {
            // JSON 파싱 오류 발생 시 (파일 내용이 손상되었거나 유효하지 않은 경우)
            Logger.error("[ManifestManager] Failed to deserialize Manifest JSON. File might be corrupted. Starting with empty list.", e);
            NEXT_ID.set(0);
            return new ArrayList<>();
        }

        // 4. NEXT_ID 업데이트 (가장 큰 ID + 1로 설정)
        long maxId = items.stream().mapToLong(item -> item.id).max().orElse(0L);
        NEXT_ID.set(maxId + 1);
        Logger.info("[ManifestManager] Manifest loaded successfully. Total items: " + items.size() + ", Next ID set to: " + NEXT_ID.get());

        return items;
    }

    // ManifestItem 리스트를 JSON 파일로 저장합니다.
    private static void saveManifest(List<ManifestItem> items) throws IOException {
        String jsonString = JsonParser.serializeList(items);
        Files.writeString(MANIFEST_PATH, jsonString, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * 새 항목을 추가하고 Manifest 파일을 업데이트합니다. (기존 코드 유지)
     */
    public static void updateManifest(String title, String path, String type) throws IOException {
        List<ManifestItem> items = loadManifest();

        long newId = NEXT_ID.getAndIncrement();

        ManifestItem newItem = new ManifestItem(newId, title, path, type);
        items.add(newItem);

        saveManifest(items);
        Logger.info("[ManifestManager] Updated content_manifest.json with new item ID: " + newId);
    }

    /**
     * Manifest에서 특정 ID의 항목을 제거하고 Manifest 파일을 업데이트합니다.
     * @param postId 제거할 항목의 ID
     * @return 제거된 ManifestItem 객체
     * @throws IOException 파일 I/O 오류 발생 시
     * @throws NoSuchElementException 해당 ID의 항목이 없을 경우
     */
    public static ManifestItem deleteItem(long postId) throws IOException, NoSuchElementException {
        List<ManifestItem> items = loadManifest();

        // 해당 ID를 가진 항목을 찾습니다.
        ManifestItem itemToDelete = items.stream()
                .filter(item -> item.id == postId)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Item with ID " + postId + " not found in manifest."));

        // 항목을 리스트에서 제거합니다.
        items.removeIf(item -> item.id == postId);

        // 업데이트된 리스트를 파일에 저장합니다.
        saveManifest(items);

        return itemToDelete;
    }
}