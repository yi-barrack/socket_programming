package server.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 콘텐츠 Manifest 파일 항목을 정의하는 데이터 모델입니다.
 */
public final class ManifestItem {
    public final long id;
    public final String title;
    public final String path;
    public final String type;

    public ManifestItem(long id, String title, String path, String type) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.type = type;
    }

    // Map으로 변환하여 JSON 직렬화에 사용
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", this.id);
        map.put("title", this.title);
        map.put("path", this.path);
        map.put("type", this.type);
        return map;
    }
}
