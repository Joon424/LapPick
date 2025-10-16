package lappick.member;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import lappick.domain.ReviewPageDTO;
import lappick.member.dto.MemberResponse;
import lappick.member.dto.MemberUpdateRequest;
import lappick.member.mapper.MemberMapper;
import lappick.member.service.MemberService;
import lappick.service.review.ReviewService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MyPageController { // MemberController -> MyPageController

    private final MemberService memberService; // 새로 통합한 MemberService 주입
    private final ReviewService reviewService;
    private final MemberMapper memberMapper;

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return null;
    }

    @PreAuthorize("hasAuthority('ROLE_MEM')")
    @GetMapping("/my-page")
    public String myPage(Model model) {
        String memberId = getCurrentUserId();
        if (memberId == null) {
            return "redirect:/login";
        }
        MemberResponse dto = memberService.getMemberInfo(memberId);
        model.addAttribute("memberCommand", dto); // 뷰 호환성을 위해 이름 유지
        return "thymeleaf/member/memMyPage"; 
    }

    @PreAuthorize("hasAuthority('ROLE_MEM')")
    @PostMapping("/my-page/update")
    public String updateMyInfo(MemberUpdateRequest command, RedirectAttributes ra) {
        String memberId = getCurrentUserId();
        try {
            memberService.updateMyInfo(command, memberId);
            ra.addFlashAttribute("message", "정보가 성공적으로 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:/member/my-page";
    }

    @PreAuthorize("hasAuthority('ROLE_MEM')")
    @PostMapping("/my-page/change-password")
    public String changePassword(@RequestParam("oldPw") String oldPw,
                                 @RequestParam("newPw") String newPw,
                                 RedirectAttributes ra,
                                 HttpSession session) {
        String memberId = getCurrentUserId();
        if (memberId == null) {
            return "redirect:/login";
        }
        
        try {
            memberService.changePassword(memberId, oldPw, newPw);
            ra.addFlashAttribute("message", "비밀번호가 성공적으로 변경되었습니다. 다시 로그인해주세요.");
            
            SecurityContextHolder.clearContext();
            session.invalidate();
            
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("message", e.getMessage());
            return "redirect:/member/my-page";
        }
    }
    
    @PreAuthorize("hasAuthority('ROLE_MEM')")
    @PostMapping("/withdraw")
    public String withdraw(@RequestParam("memberPw") String rawPassword,
                             HttpSession session,
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
    
    @PreAuthorize("hasAuthority('ROLE_MEM')")
    @GetMapping("/my-reviews")
    public String myReviews(Model model, @RequestParam(value="page", defaultValue="1") int page) {
        String memberId = getCurrentUserId();
        String memberNum = memberMapper.memberNumSelect(memberId);
        
        ReviewPageDTO pageData = reviewService.getMyReviewsPage(memberNum, page, 5);
        
        model.addAttribute("pageData", pageData);
        return "thymeleaf/member/myReviewList";
    }
}