package mini.controller;

import lombok.RequiredArgsConstructor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import jakarta.validation.Valid;
import mini.service.member.MemberListService;
import mini.service.member.MemberUpdateService;
import mini.service.member.MemberWriteService;
import mini.command.MemberCommand;
import mini.domain.MemberDTO;
import mini.domain.MemberListPage;
import mini.mapper.MemberMapper;
import mini.service.member.MemberDeleteService;
import mini.service.member.MemberDetailService;

@Controller
@RequestMapping("/employee/member")
@RequiredArgsConstructor
public class EmployeeMemberController {

    private final MemberListService memberListService;
    private final MemberDetailService memberDetailService;
    private final MemberUpdateService memberUpdateService;
    private final MemberWriteService memberWriteService;
    private final MemberDeleteService memberDeleteService; // ← 단일 서비스만 사용
    private final MemberMapper memberMapper; // memberNumSelect 사용

 // 리스트
    @GetMapping("/memberList")
    public String memberList(@RequestParam(defaultValue = "1") Integer page,
                             @RequestParam(defaultValue = "10") Integer size,
                             @RequestParam(required = false) String searchWord,
                             Model model) {
        MemberListPage pd = memberListService.getPage(page, size, searchWord);
        model.addAttribute("pageData", pd);
        model.addAttribute("members", pd.getItems());
        model.addAttribute("searchWord", searchWord);
        return "thymeleaf/employee/memberList";
    }
    
    /** 직원-회원 상세 */
    @GetMapping("/memberDetail/{memberNum}")
    public String memberDetail(@PathVariable String memberNum, Model model) {
        // MemberDetailService 는 model 에 "memberCommand" 로 담습니다.
        memberDetailService.execute(model, memberNum);
        return "thymeleaf/employee/memInfo";
    }
    
    @GetMapping
    public String rootToList() {
        return "redirect:/employee/member/memberList";
    }
    
    /** 회원 수정 폼 (상세와 동일 데이터 바인딩) */
    @GetMapping("/memberEdit/{memberNum}")
    public String memberEdit(@PathVariable String memberNum,
                             @RequestParam(required = false) String searchWord,
                             @RequestParam(required = false) Integer page,
                             Model model) {

        // 상세와 동일하게 DTO/Command 채워서 model에 "memberCommand"로 담는 기존 서비스 재사용
        memberDetailService.execute(model, memberNum);

        model.addAttribute("searchWord", searchWord);
        model.addAttribute("page", page);
        return "thymeleaf/employee/memEdit";
    }
    
    /** 회원 수정 처리 */
    @PostMapping("/memberUpdate")
    public String memberUpdate(@ModelAttribute("memberCommand") mini.command.MemberCommand command,
                               @RequestParam(required = false) String searchWord,
                               @RequestParam(required = false) Integer page,
                               RedirectAttributes ra) {

        memberUpdateService.execute(command);

        ra.addFlashAttribute("message", "성공적으로 수정되었습니다.");
        ra.addAttribute("memberNum", command.getMemberNum());
        if (searchWord != null && !searchWord.isBlank()) ra.addAttribute("searchWord", searchWord);
        if (page != null) ra.addAttribute("page", page);

        return "redirect:/employee/member/memberDetail/{memberNum}";
    }
    

    // 등록 폼
    @GetMapping("/memWrite")
    public String memWriteForm(@ModelAttribute("member") MemberDTO dto) {
        // 실제 파일: resources/templates/thymeleaf/employee/memWrite.html
        return "thymeleaf/employee/memWrite";
    }
    
    // 등록 처리
    @PostMapping("/memberInsert")
    public String memberInsert(@ModelAttribute("member") @Valid MemberDTO dto,
                               BindingResult br,
                               RedirectAttributes ra) {
        if (br.hasErrors()) return "thymeleaf/employee/memWrite";
        try {
            // 서비스에 save/execute/write 모두 준비 → 아무거나 불러도 됨
            memberWriteService.save(dto);
            ra.addFlashAttribute("msg", "회원이 등록되었습니다.");
            return "redirect:/employee/member/memberList";
        } catch (DuplicateKeyException dup) {
            ra.addFlashAttribute("error", dup.getMessage());
            // 폼으로 되돌아갈 때도 뷰 경로를 정확히
            return "redirect:/employee/member/memWrite";
        }
    }
    
    @PostMapping("memberDelete")
    public String deleteOne(@RequestParam("memberNum") String memberNum,
                            RedirectAttributes ra) {
        int n = memberDeleteService.deleteOne(memberNum);
        ra.addFlashAttribute("msg", n > 0 ? "회원이 삭제되었습니다." : "삭제 대상이 없습니다.");
        return "redirect:/employee/member/memberList";
    }

    // 삭제/등록 같은 변경 POST만 redirect로 목록으로 보내세요
    @PostMapping("/deleteSelected")
    public String deleteSelected(
            @RequestParam(value="memberNums", required=false) List<String> memberNums,
            RedirectAttributes ra) {
        if (memberNums == null || memberNums.isEmpty()) {
            ra.addFlashAttribute("msg", "선택된 회원이 없습니다.");
            return "redirect:/employee/member/memberList";
        }
        int n = memberDeleteService.deleteMany(memberNums);
        ra.addFlashAttribute("msg", n + "건이 삭제되었습니다.");
        return "redirect:/employee/member/memberList";
    }
    
}
