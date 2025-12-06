package Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Simple API key gate for sensitive proxy endpoints.
 * - Protects /api/deepseek/* and /api/serp/* if app.api.key (or APP_API_KEY env) is set.
 * - Uses header X-API-KEY for authentication.
 * - If no key configured, filter is permissive (does not block), so local/demo remains usable.
 */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {
    private final String configuredKey;
    private static final Set<String> PROTECTED_PREFIXES = Set.of(
            "/api/deepseek/",
            "/api/serp/",
            "/api/ml/",
            "/api/azure/",
            "/api/chat/"
    );

    public ApiKeyFilter(Environment env) {
        String k = System.getenv("APP_API_KEY");
        if (k == null || k.isBlank()) {
            k = env != null ? env.getProperty("app.api.key", "") : "";
        }
        this.configuredKey = k;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (needsProtection(path) && isProtectionEnabled()) {
            String key = request.getHeader("X-API-KEY");
            if (key == null || !key.equals(configuredKey)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("{\"error\":\"Unauthorized – missing or invalid X-API-KEY\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean needsProtection(String path) {
        if (path == null) return false;
        for (String p : PROTECTED_PREFIXES) {
            if (path.startsWith(p)) return true;
        }
        return false;
    }

    private boolean isProtectionEnabled() {
        return configuredKey != null && !configuredKey.isBlank();
    }
}
