package server.route;

import server.http.HttpRequest;
import server.http.HttpResponse;
import server.route.app.ContentService;
import server.util.Logger;
import server.util.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 새 글(Post) 추가 요청을 처리하는 핸들러입니다.
 * 1. POST 요청의 JSON 바디를 파싱하여 글 제목과 내용을 추출합니다. (JsonParser 사용)
 * 2. 추출한 내용을 새 텍스트 파일로 저장하고 Manifest를 업데이트합니다. (ContentService로 위임)
 */
public final class PostCreationHandler implements Handler {



    // ContentService를 인스턴스화하여 파일 I/O 로직을 위임합니다.
    private final ContentService contentService = new ContentService();
    @Override
    public HttpResponse handle(HttpRequest request) {

        try {
            // 1. 요청 바디(JSON) 파싱
            String jsonBody = new String(request.body(), StandardCharsets.UTF_8);
            Map<String, String> data = JsonParser.parse(jsonBody);

            String title = data.get("title");
            String content = data.get("content");

            // Null 체크: 필수 데이터 검증
            if (title == null || content == null || title.trim().isEmpty() || content.trim().isEmpty()) {
                Logger.warn("Invalid POST request for new post: Missing title or content.");
                return HttpResponse.builder(400, "Bad Request")
                        .body("Title and content are required (Expected JSON: {\"title\":\"...\", \"content\":\"...\"})".getBytes(StandardCharsets.UTF_8))
                        .build();
            }

            // 2. 글 저장 및 Manifest 업데이트 (ContentService에 위임)
            String newFilePath = contentService.createNewPost(title, content);

            Logger.info("Successfully created new post: " + title + " at " + newFilePath);

            // 3. 성공 응답 (201 Created) 반환
            String responseText = String.format("Post successfully created. File: %s", newFilePath);

            return HttpResponse.builder(201, "Created")
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .body(responseText.getBytes(StandardCharsets.UTF_8))
                    .build();

        } catch (RuntimeException e) {
            // JSON 파싱 오류 등 처리 (JsonParser에서 발생한 예외)
            Logger.error("Client side JSON/Data error: " + e.getMessage());
            return HttpResponse.builder(400, "Bad Request")
                    .body(("Data processing failed: " + e.getMessage()).getBytes(StandardCharsets.UTF_8))
                    .build();
        } catch (IOException e) {
            // 파일 I/O 오류 처리 (ContentService/ManifestManager에서 발생한 예외)
            Logger.error("Server side I/O error during post creation: " + e.getMessage());
            e.printStackTrace();

            return HttpResponse.builder(500, "Internal Server Error")
                    .body("Failed to create post due to server storage error.".getBytes(StandardCharsets.UTF_8))
                    .build();
        }
    }
}
