package lappick.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lappick.service.member.MemberService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginHelpController {

    private final MemberService memberService;

    // 아이디 찾기 폼 페이지
    @GetMapping("/findId")
    public String findIdForm() {
        return "thymeleaf/login/findIdForm";
    }

    // 아이디 찾기 처리
    @PostMapping("/findId")
    public String findId(@RequestParam("memberName") String memberName,
                         @RequestParam("memberEmail") String memberEmail,
                         Model model) {
        String memberId = memberService.findIdByNameAndEmail(memberName, memberEmail);
        model.addAttribute("memberId", memberId);
        return "thymeleaf/login/findIdResult";
    }

    // 비밀번호 찾기(재설정) 폼 페이지
    @GetMapping("/findPw")
    public String findPwForm() {
        return "thymeleaf/login/findPwForm";
    }

 // ▼▼▼ [수정] 비밀번호 찾기(재설정) 처리 ▼▼▼
    @PostMapping("/findPw")
    public String findPw(@RequestParam("memberId") String memberId,
                         @RequestParam("memberEmail") String memberEmail,
                         Model model) {
        // [수정] String으로 임시 비밀번호를 직접 받습니다.
        String tempPassword = memberService.resetPassword(memberId, memberEmail);

        if (tempPassword != null) {
            // 성공 시, 결과와 임시 비밀번호를 모델에 추가
            model.addAttribute("success", true);
            model.addAttribute("tempPassword", tempPassword);
        } else {
            // 실패 시
            model.addAttribute("success", false);
        }

        return "thymeleaf/login/findPwResult";
    }
}