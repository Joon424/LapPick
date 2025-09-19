package mini.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import mini.service.IdCheckService;

@Controller
@RequestMapping("login")
public class LoginController {

    @Autowired
    private IdCheckService idcheckService;

    // 💥 유저 아이콘 클릭 시 권한에 따라 올바른 페이지로 보내주는 역할
    @GetMapping("userIcon")
    public String userIconRedirect() {
        // 현재 로그인된 사용자의 인증 정보를 가져옵니다.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 로그인하지 않은 사용자일 경우, 로그인 페이지로 보냅니다.
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login/item.login";
        }
        
        // 사용자의 권한을 확인하여 분기 처리합니다.
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MEM"))) {
            // 'ROLE_MEM' 권한이 있으면, 회원 마이페이지로 보냅니다.
            return "redirect:/member/my-page";
        }
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMP"))) {
            // 💥 [수정] 'ROLE_EMP' 권한이 있으면, 직원 관리 허브 페이지로 보냅니다.
            return "redirect:/login/emp.login";
        }

        // 그 외의 경우, 메인 페이지로 보냅니다.
        return "redirect:/";
    }
    
    
    // 아이디 중복 체크 기능 (회원가입 시 사용)
    @PostMapping("userIdCheck")
    @ResponseBody
    public Integer userIdCheck(String userId) {
        return idcheckService.execute(userId);
    }
    
    // GET /login/item.login 요청 시 로그인 페이지를 보여줍니다.
    @GetMapping("item.login")
    public String item() {
        return "thymeleaf/login";
    }

    // 직원용 로그인 페이지를 보여줍니다.
    @GetMapping("/emp.login")
    public String empLogin() {
        return "thymeleaf/employee/empLogin";
    }

   
}