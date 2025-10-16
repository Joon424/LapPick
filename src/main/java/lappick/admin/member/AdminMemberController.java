package lappick.admin.member;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lappick.admin.member.dto.AdminMemberPageResponse; // [수정] DTO 경로 변경
import lappick.member.dto.MemberResponse;
import lappick.member.dto.MemberUpdateRequest;

import java.util.List;

@Controller
@RequestMapping("/admin/members") // [수정] URL을 /employee/members -> /admin/members 로 변경
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_EMP')")
public class AdminMemberController { // EmployeeMemberController -> AdminMemberController

    private final AdminMemberService adminMemberService;

    @GetMapping
    public String listMembers(@RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer size,
                              @RequestParam(required = false) String searchWord,
                              Model model) {
        AdminMemberPageResponse pageData = adminMemberService.getMemberListPage(page, size, searchWord); // [수정] DTO 타입 변경
        model.addAttribute("pageData", pageData);
        model.addAttribute("members", pageData.getItems());
        model.addAttribute("searchWord", searchWord);
        model.addAttribute("page", pageData.getPage());
        model.addAttribute("totalPage", pageData.getTotalPages());
        
        return "thymeleaf/employee/memList"; 
    }

    @GetMapping("/{memberNum}")
    public String memberDetail(@PathVariable String memberNum, Model model) {
        MemberResponse dto = adminMemberService.getMemberDetail(memberNum);
        model.addAttribute("memberCommand", dto); 
        return "thymeleaf/employee/memInfo";
    }

    @GetMapping("/{memberNum}/edit")
    public String editForm(@PathVariable String memberNum, Model model) {
        MemberResponse dto = adminMemberService.getMemberDetail(memberNum);
        model.addAttribute("memberCommand", dto);
        return "thymeleaf/employee/memEdit";
    }

    @PostMapping("/update")
    public String updateMember(MemberUpdateRequest command, RedirectAttributes ra) {
        adminMemberService.updateMember(command);
        ra.addFlashAttribute("message", "회원 정보가 성공적으로 수정되었습니다.");
        return "redirect:/admin/members/" + command.getMemberNum(); // [수정] URL 변경
    }
    
    @GetMapping("/add")
    public String addForm(@ModelAttribute("member") MemberUpdateRequest command) {
        return "thymeleaf/employee/memWrite";
    }

    @PostMapping("/add")
    public String addMember(@ModelAttribute("member") @Valid MemberUpdateRequest command,
                            BindingResult br, RedirectAttributes ra) {
        
        if (br.hasErrors()) {
            return "thymeleaf/employee/memWrite";
        }
        
        if (!command.getMemberPw().equals(command.getMemberPwCon())) {
            br.rejectValue("memberPwCon", "password.mismatch", "비밀번호가 일치하지 않습니다.");
            return "thymeleaf/employee/memWrite";
        }

        try {
            adminMemberService.createMember(command);
            ra.addFlashAttribute("msg", "회원이 등록되었습니다.");
            return "redirect:/admin/members"; // [수정] URL 변경

        } catch (DuplicateKeyException e) {
            ra.addFlashAttribute("error", "이미 사용 중인 아이디입니다.");
            return "redirect:/admin/members/add"; // [수정] URL 변경
        }
    }
    
    @PostMapping("/delete")
    public String deleteMembers(@RequestParam(value = "memberNums", required = false) List<String> memberNums, 
                                RedirectAttributes ra) {
        if (memberNums == null || memberNums.isEmpty()) {
            ra.addFlashAttribute("error", "삭제할 회원을 선택해주세요.");
            return "redirect:/admin/members"; // [수정] URL 변경
        }

        int deletedCount = adminMemberService.deleteMembers(memberNums);
        ra.addFlashAttribute("msg", deletedCount + "명의 회원이 삭제되었습니다.");
        return "redirect:/admin/members"; // [수정] URL 변경
    }
    
    @GetMapping("/delete/{memberNum}")
    public String deleteSingleMember(@PathVariable String memberNum, RedirectAttributes ra) {
        int deletedCount = adminMemberService.deleteMembers(java.util.Collections.singletonList(memberNum));
        ra.addFlashAttribute("msg", deletedCount + "명의 회원이 삭제되었습니다.");
        return "redirect:/admin/members"; // [수정] URL 변경
    }
}