package mx.oaxinvest.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.Map;

public final class PageUtil {
    private PageUtil() {
    }

    public static final int MAX_SIZE = 50;

    public static Pageable of(int page, int size, String sort, Map<String, String> allowed, String defaultProperty) {
        int p = Math.max(page, 0);
        int s = Math.min(Math.max(size, 1), MAX_SIZE);
        Sort order = Sort.by(Sort.Direction.DESC, defaultProperty);
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String property = allowed.get(parts[0].trim());
            if (property != null) {
                Sort.Direction dir = parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC : Sort.Direction.DESC;
                order = Sort.by(dir, property);
            }
        }
        return PageRequest.of(p, s, order);
    }

    public static String like(String q) {
        return "%" + (q == null ? "" : q.trim().toLowerCase().replace("%", "\\%").replace("_", "\\_")) + "%";
    }
}
