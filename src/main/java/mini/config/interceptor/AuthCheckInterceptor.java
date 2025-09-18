package mini.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mini.domain.AuthInfoDTO;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class AuthCheckInterceptor implements HandlerInterceptor {
    private final Set<String> allowedGrades; // 예: ["mem"], ["emp"], ["mem","emp"]

    public AuthCheckInterceptor(Set<String> allowedGrades) {
        this.allowedGrades = allowedGrades;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        HttpSession session = req.getSession(false);
        AuthInfoDTO auth = (session == null) ? null : (AuthInfoDTO) session.getAttribute("auth");

        if (auth == null) {
            String q = req.getQueryString();
            String requested = req.getRequestURI() + (q != null ? "?" + q : "");
            String returnUrl = URLEncoder.encode(requested, StandardCharsets.UTF_8);
            res.sendRedirect(req.getContextPath() + "/login/item.login?returnUrl=" + returnUrl);
            return false;
        }
        if (allowedGrades != null && !allowedGrades.isEmpty() && !allowedGrades.contains(auth.getGrade())) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }
}
