package mini.controller;

import jakarta.servlet.http.HttpSession;
import mini.domain.AuthInfoDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EmpMyPageController {

    /** 직원 마이페이지(대시보드) */
    @GetMapping("/myPage/empMyPage")
    public String empMyPage(HttpSession session, Model model) {
        AuthInfoDTO auth = (AuthInfoDTO) session.getAttribute("auth");
        if (auth == null) {
            // 세션 없으면 로그인 페이지로
            return "redirect:/login/item.login";
        }
        if (!"emp".equals(auth.getGrade())) {
            // 직원이 아니면 메인으로
            return "redirect:/";
        }
        // 템플릿: templates/thymeleaf/employee/empLogin.html
        // (empLogin.html이 직원 대시보드 파일임)
        return "thymeleaf/employee/empLogin";
    }
}
