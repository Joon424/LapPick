package mini.controller;

import jakarta.servlet.http.HttpSession;
import mini.command.MemberCommand;
import mini.domain.AuthInfoDTO;
import mini.domain.MemberDTO;
import mini.mapper.MemberInfoMapper;
import mini.service.myPage.MemberDropService;
import mini.service.myPage.MemberMyInfoService;
import mini.service.myPage.MemberMyUpdateService;
import mini.service.myPage.MemberPwUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MyPageController {

    // ===== 서비스/매퍼 주입 =====
    @Autowired MemberMyInfoService memberMyInfoService;
    @Autowired MemberMyUpdateService memberMyUpdateService;
    @Autowired MemberPwUpdateService memberPwUpdateService;
    @Autowired MemberDropService memberDropService;
    @Autowired MemberInfoMapper memberInfoMapper;

    // ===== 회원 마이페이지 =====
    // ※ 기존에 동일 경로가 또 있으면 제거하세요(이번 오류 원인)
    @GetMapping("/myPage/memMyPage")
    public String memMyPage2(HttpSession session, Model model) {
        // 프로젝트 서비스 시그니처: (session, model) → void
        memberMyInfoService.execute(session, model);
        model.addAttribute("isLoggedIn", session.getAttribute("auth") != null);
        return "thymeleaf/member/memMyPage";
    }

    // ===== 장바구니 별칭 =====
    // 템플릿에서 javascript:navigateTo('/cart') 또는 단순 링크 '/cart' 모두 지원
    @GetMapping("/cart")
    public String cartAlias() {
        return "redirect:/item/cartList";
    }

    // ===== 내 정보 수정 =====
    // memMyPage 폼 action="/memberUpdate"
    @PostMapping("/memberUpdate")
    public String memberUpdate(MemberCommand memberCommand,
                               HttpSession session,
                               RedirectAttributes ra) {
        memberMyUpdateService.execute(memberCommand, session);
        ra.addFlashAttribute("flashMsg", "수정되었습니다.");
        return "redirect:/myPage/memMyPage";
    }

    // ===== 비밀번호 변경 =====
    // memMyPage 폼 action="/memberPwModify"
    @PostMapping("/memberPwModify")
    public String memberPwModify(@RequestParam String oldPw,
                                 @RequestParam String newPw,
                                 HttpSession session,
                                 RedirectAttributes ra) {

        AuthInfoDTO before = (AuthInfoDTO) session.getAttribute("auth");
        String beforeHash = (before != null ? before.getUserPw() : null);

        memberPwUpdateService.execute(oldPw, newPw, session);

        AuthInfoDTO after = (AuthInfoDTO) session.getAttribute("auth");
        boolean changed = (after != null && beforeHash != null && !beforeHash.equals(after.getUserPw()));

        if (changed) {
            ra.addFlashAttribute("flashMsg", "성공적으로 비밀번호가 변경됐습니다");
            session.invalidate();
            return "redirect:/login/item.login";
        } else {
            ra.addFlashAttribute("flashMsg", "현재 비밀번호가 잘못됐습니다.");
            return "redirect:/myPage/memMyPage";
        }
    }

    // ===== 회원 탈퇴 =====
    // 버튼에서 location.href="/memberDropOk?memberPw=..." 형태로 호출(비번 확인 필요)
    @GetMapping("/memberDropOk")
    public String memberDropOk(@RequestParam(name = "memberPw", required = false) String memberPw,
                               HttpSession session,
                               RedirectAttributes ra) {
        AuthInfoDTO auth = (AuthInfoDTO) session.getAttribute("auth");
        if (auth == null) return "redirect:/login/item.login";

        if (memberPw == null || memberPw.isBlank()) {
            ra.addFlashAttribute("flashMsg", "회원 탈퇴는 비밀번호 확인이 필요합니다.");
            return "redirect:/myPage/memMyPage";
        }

        String userId = auth.getUserId();
        memberDropService.execute(memberPw, session); // (memberPw, session)

        MemberDTO stillThere = memberInfoMapper.memberSelectOne(userId);
        if (stillThere == null) {   // 삭제 성공
            session.invalidate();
            return "redirect:/";
        } else {                    // 비번 불일치 등으로 미삭제
            ra.addFlashAttribute("flashMsg", "비밀번호가 일치하지 않습니다.");
            return "redirect:/myPage/memMyPage";
        }
    }

    // ===== (주의) 이 클래스에 동일 경로(@GetMapping("/myPage/memMyPage"))가
    // 또 하나라도 존재하면 매핑 충돌이 납니다. 반드시 하나만 유지하세요. =====
}










