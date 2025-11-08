package lappick.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class GlobalControllerAdvice {

    /**
     * 모든 컨트롤러의 Model에 'isLoggedIn' 속성을 추가합니다.
     * * @return {@link SecurityContextHolder}를 기준으로 한 현재 로그인 상태
     * (인증되었으며, 익명 사용자가 아닐 경우 true)
     */
    @ModelAttribute("isLoggedIn")
    public boolean isLoggedIn() {
        // 스프링 시큐리티의 현재 인증 정보를 가져옵니다. (HttpSession 직접 확인 X)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 인증 정보가 존재하고, 인증된 상태이며, 익명 사용자가 아닐 경우에만 true
        return authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName());
    }
}