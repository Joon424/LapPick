package lappick.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lappick.auth.dto.RegisterRequest;
import lappick.auth.mapper.AuthMapper;
import lappick.auth.service.AuthService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthMapper authMapper;

    // ====== 로그인/로그아웃 ======
    @GetMapping("/login")
    public String loginForm() {
        return "user/auth/login";
    }
    
    // ====== 회원가입 ======
    @ModelAttribute("registerRequest")
    public RegisterRequest registerRequest() {
        return new RegisterRequest();
    }
    
    @GetMapping("/register/agree")
    public String agree() {
        return "user/auth/register-agree";
    }

    @GetMapping("/register/write")
    public String registerForm() {
        return "user/auth/register-form";
    }

    @PostMapping("/register/write")
    public String register(@Validated @ModelAttribute("registerRequest") RegisterRequest registerRequest, BindingResult result) {
        if (result.hasErrors()) {
            return "user/auth/register-form";
        }
        authService.joinMember(registerRequest);
        return "redirect:/auth/register/welcome";
    }

    @GetMapping("/register/welcome")
    public String welcome() {
        return "user/auth/register-complete";
    }
    
    // ====== 아이디/비밀번호 찾기 ======
    @GetMapping("/find-id")
    public String findIdForm() {
        return "user/auth/find-id-form";
    }

    @PostMapping("/find-id")
    public String findIdAction(@RequestParam("memberName") String memberName,
                               @RequestParam("memberEmail") String memberEmail, Model model) {
        String memberId = authService.findIdByNameAndEmail(memberName, memberEmail);
        model.addAttribute("memberId", memberId);
        return "user/auth/find-id-result";
    }

    @GetMapping("/find-pw")
    public String findPwForm() {
        return "user/auth/find-pw-form";
    }



    @PostMapping("/find-pw")
    public String findPwAction(@RequestParam("memberId") String memberId,
                               @RequestParam("memberEmail") String memberEmail, Model model) {
        String tempPassword = authService.resetPassword(memberId, memberEmail);
        if (tempPassword != null) {
            model.addAttribute("success", true);
            model.addAttribute("tempPassword", tempPassword);
        } else {
            model.addAttribute("success", false);
        }
        return "user/auth/find-pw-result";
    }
    
    // ====== AJAX 중복 체크 ======
    @PostMapping("/userIdCheck")
    @ResponseBody
    public Integer userIdCheck(@RequestParam("userId") String userId) {
        return authMapper.idCheckSelectOne(userId);
    }
}