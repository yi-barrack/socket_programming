package server.util;

import server.model.ManifestItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 단순한 JSON 파싱/직렬화를 시뮬레이션하는 유틸리티 클래스입니다.
 * 실제 환경에서는 Jackson, Gson 등의 라이브러리를 사용해야 합니다.
 */
public final class JsonParser {

    // POST 요청 바디용 파서 ({"title": "...", "content": "..."} 형태 가정)
    public static Map<String, String> parse(String json) {
        Map<String, String> data = new HashMap<>();
        // 실제로는 Jackson/Gson을 사용하지만, 여기서는 간단한 문자열 파싱으로 대체합니다.
        try {
            String cleaned = json.trim().replaceAll("^\\{|\\}$", ""); // {} 제거
            for (String pair : cleaned.split(",")) {
                if (pair.contains(":")) {
                    String[] kv = pair.split(":", 2);
                    String key = kv[0].trim().replaceAll("^\"|\"$", "");
                    String value = kv[1].trim().replaceAll("^\"|\"$", "");
                    data.put(key, value);
                }
            }
        } catch (Exception e) {
            // 파싱 오류는 RuntimeException으로 던져서 핸들러에서 400 Bad Request로 처리
            throw new RuntimeException("Malformed JSON data received", e);
        }
        return data;
    }

    // ManifestItem 리스트를 JSON 문자열로 직렬화
    public static String serializeList(List<ManifestItem> items) {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < items.size(); i++) {
            ManifestItem item = items.get(i);
            String itemJson = String.format(
                    "  {\n    \"id\": %d,\n    \"title\": \"%s\",\n    \"path\": \"%s\",\n    \"type\": \"%s\"\n  }",
                    item.id,
                    item.title.replace("\"", "\\\""), // 제목 내 따옴표 이스케이프
                    item.path,
                    item.type
            );
            sb.append(itemJson);
            if (i < items.size() - 1) {
                sb.append(",\n");
            }
        }
        sb.append("\n]");
        return sb.toString();
    }

    // JSON 문자열을 ManifestItem 리스트로 역직렬화 (매우 단순한 형태만 처리)
    public static List<ManifestItem> deserializeList(String json) {
        List<ManifestItem> items = new ArrayList<>();

        // JSON 배열의 각 객체 ({...})를 찾는 정규표현식.
        // 이 정규식은 단순한 형태의 ManifestItem 객체에 한정됩니다.
        // 예를 들어 "{"id": 1, "title": "프로젝트 설계 개요 문서", "path": "/posts/design_notes.md", "type": "text"}"
        Pattern pattern = Pattern.compile("\\{\\s*\"id\":\\s*(\\d+),\\s*\"title\":\\s*\"([^\"]*)\",\\s*\"path\":\\s*\"([^\"]*)\",\\s*\"type\":\\s*\"([^\"]*)\"[^\\}]*\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            try {
                long id = Long.parseLong(matcher.group(1));
                String title = matcher.group(2);
                String path = matcher.group(3);
                String type = matcher.group(4);

                // 새로운 ManifestItem 객체를 생성하여 리스트에 추가
                ManifestItem item = new ManifestItem(id, title, path, type);
                items.add(item);
            } catch (Exception e) {
                Logger.error("Error parsing ManifestItem: " + e.getMessage());
                // 파싱 오류 발생 시 해당 항목은 건너뜁니다.
            }
        }

        return items;
    }
}
