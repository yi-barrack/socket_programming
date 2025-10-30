package server.route;


import server.http.HttpRequest;
import server.http.HttpResponse;
import server.util.Logger;
import server.route.app.ContentService;
import server.model.ManifestItem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

/**
 * 글 삭제 요청 (DELETE /api/posts/{id})을 처리하는 핸들러입니다.
 */
public final class PostDeleteHandler implements Handler {

    private final ContentService contentService = new ContentService();

    @Override
    public HttpResponse handle(HttpRequest request) {
        String uri = request.target();
        long postId = 0;

        try {
            // 1. URI에서 ID 추출 (예: /api/posts/123 -> 123)
            String[] parts = uri.split("/");
            if (parts.length < 4) {
                throw new IllegalArgumentException("Invalid post ID in URI.");
            }
            // 마지막 부분(ID)을 파싱
            postId = Long.parseLong(parts[parts.length - 1]);
            System.out.println("asdasd");
            // 2. 서비스 계층을 호출하여 Manifest 및 파일 시스템에서 삭제
            ManifestItem deletedItem = contentService.deletePost(postId);
            Logger.info("Successfully deleted post ID: " + postId + ", Title: " + deletedItem.title);

            // 3. 성공 응답 (204 No Content) 반환
            return HttpResponse.builder(204, "No Content")
                    .build();

        } catch (NumberFormatException e) {
            Logger.warn("Invalid Post ID format received: " + uri);
            return HttpResponse.builder(400, "Bad Request")
                    .body("Invalid Post ID format.".getBytes(StandardCharsets.UTF_8))
                    .build();
        } catch (NoSuchElementException e) {
            Logger.warn("Attempted to delete non-existent post: ID " + postId);
            return HttpResponse.builder(404, "Not Found")
                    .body("Post with ID not found.".getBytes(StandardCharsets.UTF_8))
                    .build();
        } catch (IOException e) {
            Logger.error("Server side I/O error during post deletion: " + e.getMessage(), e);
            return HttpResponse.builder(500, "Internal Server Error")
                    .body("Failed to delete post due to server error.".getBytes(StandardCharsets.UTF_8))
                    .build();
        } catch (IllegalArgumentException e) {
            Logger.warn(e.getMessage());
            return HttpResponse.builder(400, "Bad Request")
                    .body(e.getMessage().getBytes(StandardCharsets.UTF_8))
                    .build();
        }
    }
}