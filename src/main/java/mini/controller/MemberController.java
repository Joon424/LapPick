package mini.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import mini.command.MemberCommand;
import mini.domain.MemberDTO;
import mini.domain.ReviewPageDTO;
import mini.mapper.MemberMapper;
import mini.service.member.MemberService;
import mini.service.review.ReviewService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/member") // 💥 모든 회원 관련 URL은 /member로 시작하도록 통일
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final ReviewService reviewService; // [추가]
    private final MemberMapper memberMapper; // [추가]

    // 현재 로그인된 사용자 ID를 가져오는 헬퍼 메서드
    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return null;
    }

    /**
     * 마이페이지 폼
     */
    @PreAuthorize("hasAuthority('ROLE_MEM')")
    @GetMapping("/my-page")
    public String myPage(Model model) {
        String memberId = getCurrentUserId();
        if (memberId == null) {
            // SecurityConfig가 알아서 로그인 페이지로 보내주므로 이 코드는 사실상 실행되지 않음
            return "redirect:/login/item.login";
        }
        MemberDTO dto = memberService.getMemberInfo(memberId);
        model.addAttribute("memberCommand", dto);
        return "thymeleaf/member/memMyPage"; 
    }

    /**
     * 내 정보 수정 처리
     */
    @PreAuthorize("hasAuthority('ROLE_MEM')")
    @PostMapping("/my-page/update")
    public String updateMyInfo(MemberCommand command, RedirectAttributes ra) {
        String memberId = getCurrentUserId();
        command.setMemberId(memberId); // 현재 로그인된 사용자로 ID 고정 (보안)
        
        try {
            memberService.updateMyInfo(command);
            ra.addFlashAttribute("message", "정보가 성공적으로 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:/member/my-page";
    }

    /**
     * 비밀번호 변경 처리
     */
    @PreAuthorize("hasAuthority('ROLE_MEM')")
    @PostMapping("/my-page/change-password")
    public String changePassword(@RequestParam("oldPw") String oldPw,
                                 @RequestParam("newPw") String newPw,
                                 RedirectAttributes ra,
                                 HttpSession session) { // 💥 [추가] HttpSession 주입
        String memberId = getCurrentUserId();
        if (memberId == null) {
            return "redirect:/login/item.login";
        }
        
        try {
            memberService.changePassword(memberId, oldPw, newPw);
            ra.addFlashAttribute("message", "비밀번호가 성공적으로 변경되었습니다. 다시 로그인해주세요.");
            
            // 💥 [추가] 여기서 직접 로그아웃을 처리합니다.
            SecurityContextHolder.clearContext(); // 스프링 시큐리티 인증 정보 삭제
            session.invalidate(); // 세션 무효화
            
            // 💥 [수정] 로그아웃 후 로그인 페이지로 직접 리다이렉트합니다.
            return "redirect:/login/item.login";

        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("message", e.getMessage());
            return "redirect:/member/my-page";
        }
    }
    
    /**
     * 회원 탈퇴 처리
     */
    @PreAuthorize("hasAuthority('ROLE_MEM')")
    @PostMapping("/withdraw")
    public String withdraw(@RequestParam("memberPw") String rawPassword,
                             HttpSession session, // 세션 무효화를 위해 필요
                             RedirectAttributes ra) {
        String memberId = getCurrentUserId();
        try {
            memberService.withdrawMember(memberId, rawPassword);
            SecurityContextHolder.clearContext();
            session.invalidate();
            ra.addFlashAttribute("message", "회원 탈퇴가 완료되었습니다.");
            return "redirect:/";
            
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("message", e.getMessage());
            return "redirect:/member/my-page";
        }
    }
    
    /**
     * [추가] 내가 작성한 리뷰 목록 페이지
     */
    @PreAuthorize("hasAuthority('ROLE_MEM')")
    @GetMapping("/my-reviews")
    public String myReviews(Model model, @RequestParam(value="page", defaultValue="1") int page) {
        String memberId = getCurrentUserId();
        String memberNum = memberMapper.memberNumSelect(memberId);
        
        // 한 페이지에 5개씩 표시
        ReviewPageDTO pageData = reviewService.getMyReviewsPage(memberNum, page, 5);
        
        model.addAttribute("pageData", pageData);
        return "thymeleaf/member/myReviewList"; // 새로 만들 HTML 파일
    }
}
