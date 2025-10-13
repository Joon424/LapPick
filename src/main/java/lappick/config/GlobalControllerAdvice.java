package lappick.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpSession;
import lappick.domain.AuthInfoDTO;

@ControllerAdvice(annotations = Controller.class)
public class GlobalControllerAdvice {

    @ModelAttribute("isLoggedIn")
    public boolean isLoggedIn() {
        // 💥 [수정] HttpSession을 직접 확인하는 대신,
        // 스프링 시큐리티의 현재 인증 정보를 가져옵니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 인증 정보가 존재하고, 인증된 상태이며, 익명 사용자가 아닐 경우에만 true를 반환합니다.
        // 이것이 가장 정확하고 안전한 로그인 확인 방법입니다.
        return authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName());
    }
}
