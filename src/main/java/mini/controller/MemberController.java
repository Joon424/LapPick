package mini.controller;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import mini.command.MemberCommand;
import mini.domain.MemberDTO;
import mini.domain.MemberListPage;
import mini.mapper.MemberMapper;
import mini.service.AutoNumService;
import mini.service.member.MemberDeleteService;
import mini.service.member.MemberDetailService;
import mini.service.member.MemberListService;
import mini.service.member.MemberUpdateService;
import mini.service.member.MemberWriteService;

@Controller
@RequestMapping("member") // 공통주소 처리
public class MemberController {
    @Autowired
    MemberWriteService memberWriteService;
    @Autowired
    AutoNumService autoNumService;
    @Autowired
    MemberListService memberListService;
    @Autowired
    MemberDeleteService memberDeleteService;
    @Autowired
    MemberDetailService memberDetailService;
    @Autowired
    MemberUpdateService memberUpdateService;
    
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    public MemberController(MemberMapper memberMapper, PasswordEncoder passwordEncoder) {
        this.memberMapper = memberMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/employee/memberList")
    public String list(@RequestParam(value = "searchWord", required = false) String searchWord,
                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                       Model model) {
        MemberListPage pd = memberListService.getPage(page, 10, searchWord);
        model.addAttribute("pageData", pd);
        model.addAttribute("members", pd.getItems());
        model.addAttribute("searchWord", searchWord);
        return "thymeleaf/employee/memberList";
    }
    
    
    @GetMapping("/memberDetail/{memberNum}")
    public String memberDetail(@PathVariable("memberNum") String memberNum, Model model) {
        model.addAttribute("memberNum", memberNum);
        memberDetailService.execute(model, memberNum);
        return "thymeleaf/employee/memInfo";
    }

    @GetMapping("memberUpdate")
    public String memberUpdate(@RequestParam("memberNum") String memberNum, Model model) {
        memberDetailService.execute(model, memberNum);
        return "thymeleaf/employee/memModify"; // 경로 수정
    }

    @PostMapping("memberUpdate")
    public String memberUpdate(@Validated MemberCommand memberCommand, BindingResult result) {
        if (result.hasErrors()) {
            return "thymeleaf/employee/memModify"; // 경로 수정
        }
        memberUpdateService.execute(memberCommand);
        return "redirect:memberDetail/" + memberCommand.getMemberNum();
    }

    /** 단건 삭제 (리스트에서 휴지통/링크로 호출) 
     *  URL: /member/memberDelete/{memberNum}
     */
    @GetMapping("/memberDelete/{memberNum}")
    public String deleteOne(@PathVariable("memberNum") String memberNum,
                            @RequestParam(value = "searchWord", required = false) String searchWord,
                            @RequestParam(value = "page", defaultValue = "1") Integer page,
                            RedirectAttributes ra) {
        memberDeleteService.execute(memberNum); // 너 서비스에 있는 그대로 'execute(String)'
        ra.addFlashAttribute("msg", "삭제되었습니다.");
        // 검색/페이지 유지
        String redirect = "redirect:/member/employee/memberList?page=" + page;
        if (searchWord != null && !searchWord.isBlank()) {
            redirect += "&searchWord=" + java.net.URLEncoder.encode(searchWord, java.nio.charset.StandardCharsets.UTF_8);
        }
        return redirect;
    }
    
    @PostMapping("/memberDropOk")
    public String memberDropOk(@RequestParam("memberPw") String rawPw,
                               HttpSession session,
                               RedirectAttributes ra) {

        // 1) 로그인 아이디 가져오기 (Security 우선, 없으면 세션 키 fallback)
        String memberId = resolveLoginId(session);
        if (memberId == null || memberId.isBlank()) {
            ra.addFlashAttribute("message", "로그인이 필요합니다.");
            return "redirect:/login/item.login";
        }

        // 2) 비밀번호 검증
        String hash = memberMapper.selectPwById(memberId);
        if (hash == null || !passwordEncoder.matches(rawPw, hash)) {
            ra.addFlashAttribute("message", "비밀번호가 일치하지 않습니다.");
            return "redirect:/member/memMyPage";
        }

        // 3) 회원번호 조회 → 삭제
        String memberNum = memberMapper.memberNumSelect(memberId);
        if (memberNum == null) {
            ra.addFlashAttribute("message", "회원 정보를 찾을 수 없습니다.");
            return "redirect:/member/memMyPage";
        }

        try {
            memberMapper.memberDelete(Collections.singletonList(memberNum));
        } catch (DataIntegrityViolationException ex) {
            ra.addFlashAttribute("message", "연관 데이터(주문 등) 때문에 탈퇴할 수 없습니다.");
            return "redirect:/member/memMyPage";
        }

        // 4) 세션/인증 정리
        try { SecurityContextHolder.clearContext(); } catch (Exception ignore) {}
        try { session.invalidate(); } catch (Exception ignore) {}

        ra.addFlashAttribute("message", "탈퇴가 완료되었습니다.");
        return "redirect:/";
    }

    private String resolveLoginId(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName(); // 통상 username
        }
        if (session != null) {
            Object sid = session.getAttribute("userId");
            if (sid == null) sid = session.getAttribute("memberId");
            if (sid == null) sid = session.getAttribute("loginId");
            if (sid instanceof String s) return s;
        }
        return null;
        }
    
    @GetMapping("/memWrite")
    public String memWriteForm(Model model) {
        model.addAttribute("member", new MemberDTO());
        return "employee/member/memWrite"; // /templates/employee/member/memWrite.html
    }

    @PostMapping("/memWrite")
    public String memWriteSubmit(@ModelAttribute("member") @Valid MemberDTO dto,
                                 BindingResult br,
                                 RedirectAttributes ra) {
        if (br.hasErrors()) return "employee/member/memWrite";
        memberWriteService.execute(dto);
        ra.addFlashAttribute("msg", "등록되었습니다.");
        return "redirect:/employee/member/memberList";
    }
}
