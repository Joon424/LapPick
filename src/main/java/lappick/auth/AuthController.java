package lappick.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lappick.auth.dto.RegisterRequest;
import lappick.auth.mapper.AuthMapper; // IdCheck용으로 임시 사용
import lappick.auth.service.AuthService;
import lappick.member.service.MemberService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService; // ID/PW 찾기 로직 때문에 임시로 주입
    private final AuthMapper authMapper; // ID 중복 체크 때문에 임시로 주입

    // ====== MemberJoinController 기능 ======
    @ModelAttribute("registerRequest") // command 객체 이름을 registerRequest로 변경
    public RegisterRequest registerRequest() {
        return new RegisterRequest();
    }

    @GetMapping("/register/agree")
    public String agree() {
        return "thymeleaf/memberJoin/agree";
    }

    @GetMapping("/register/write")
    public String registerForm() {
        return "thymeleaf/memberJoin/memForm";
    }

    @PostMapping("/register/write")
    public String register(@Validated @ModelAttribute("registerRequest") RegisterRequest registerRequest, BindingResult result) {
        if (result.hasErrors()) {
            return "thymeleaf/memberJoin/memForm";
        }
        authService.joinMember(registerRequest);
        return "redirect:/register/welcome";
    }

    @GetMapping("/register/welcome")
    public String welcome() {
        return "thymeleaf/memberJoin/welcome";
    }

    // ====== LoginController 기능 ======
    @GetMapping("/login")
    public String loginForm() {
        return "thymeleaf/login";
    }

    @GetMapping("/login/emp")
    public String empLoginForm() {
        return "thymeleaf/employee/empLogin";
    }

    @GetMapping("/userIcon")
    public String userIconRedirect() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MEM"))) {
            return "redirect:/member/my-page";
        }
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMP"))) {
            return "redirect:/employee/hub"; // 직원 허브 페이지로 변경
        }
        return "redirect:/";
    }

    @PostMapping("/userIdCheck")
    @ResponseBody
    public Integer userIdCheck(@RequestParam("userId") String userId) {
        return authMapper.idCheckSelectOne(userId); // IdCheckService 대신 AuthMapper 직접 호출
    }

    // ====== LoginHelpController 기능 ======
    @GetMapping("/find-id")
    public String findIdForm() {
        return "thymeleaf/login/findIdForm";
    }

    @PostMapping("/find-id")
    public String findIdAction(@RequestParam("memberName") String memberName,
                               @RequestParam("memberEmail") String memberEmail, Model model) {
        // ▼▼▼▼▼ [수정] authService를 통해 호출 ▼▼▼▼▼
        String memberId = authService.findIdByNameAndEmail(memberName, memberEmail);
        model.addAttribute("memberId", memberId);
        return "thymeleaf/login/findIdResult";
    }

    @GetMapping("/find-pw")
    public String findPwForm() {
        return "thymeleaf/login/findPwForm";
    }

    @PostMapping("/find-pw")
    public String findPwAction(@RequestParam("memberId") String memberId,
                               @RequestParam("memberEmail") String memberEmail, Model model) {
        // ▼▼▼▼▼ [수정] authService를 통해 호출 ▼▼▼▼▼
        String tempPassword = authService.resetPassword(memberId, memberEmail);
        if (tempPassword != null) {
            model.addAttribute("success", true);
            model.addAttribute("tempPassword", tempPassword);
        } else {
            model.addAttribute("success", false);
        }
        return "thymeleaf/login/findPwResult";
    }
}