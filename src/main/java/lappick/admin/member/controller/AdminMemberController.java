package lappick.admin.member.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lappick.admin.member.dto.AdminMemberPageResponse;
import lappick.admin.member.service.AdminMemberService;
import lappick.member.dto.MemberResponse;
import lappick.member.dto.MemberUpdateRequest;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/members")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    @GetMapping
    public String listMembers(@RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "5") Integer size,
                              @RequestParam(required = false) String searchWord,
                              Model model) {
        AdminMemberPageResponse pageData = adminMemberService.getMemberListPage(page, size, searchWord);
        model.addAttribute("pageData", pageData);
        model.addAttribute("members", pageData.getItems());
        model.addAttribute("searchWord", searchWord);
        model.addAttribute("page", pageData.getPage());
        model.addAttribute("totalPage", pageData.getTotalPages());
        
        return "admin/member/member-list"; 
    }

    @GetMapping("/{memberNum}")
    public String memberDetail(@PathVariable String memberNum, Model model) {
        MemberResponse dto = adminMemberService.getMemberDetail(memberNum);
        model.addAttribute("memberCommand", dto); 
        return "admin/member/member-detail";
    }

    @GetMapping("/{memberNum}/edit")
    public String editForm(@PathVariable String memberNum, Model model) {
        // DB에서 조회 (타입: MemberResponse)
        MemberResponse responseDto = adminMemberService.getMemberDetail(memberNum);
        
        // 폼 전용 DTO 생성 (타입: MemberUpdateRequest)
        MemberUpdateRequest requestDto = new MemberUpdateRequest();

        // 조회한 데이터를 폼 DTO로 복사
        BeanUtils.copyProperties(responseDto, requestDto);
        
        model.addAttribute("memberCommand", requestDto);
        return "admin/member/member-edit";
    }

    @PostMapping("/update")
    public String updateMember(MemberUpdateRequest command, RedirectAttributes ra) {
        adminMemberService.updateMember(command);
        ra.addFlashAttribute("message", "회원 정보가 성공적으로 수정되었습니다.");
        return "redirect:/admin/members/" + command.getMemberNum();
    }
    
    @GetMapping("/add")
    public String addForm(@ModelAttribute("member") MemberUpdateRequest command) {
        // 생년월일 기본값 '1990-01-01' 설정
        command.setMemberBirth(LocalDate.of(1990, 1, 1));
        return "admin/member/member-form";
    }

    @PostMapping("/add")
    public String addMember(@ModelAttribute("member") @Valid MemberUpdateRequest command,
                            BindingResult br, RedirectAttributes ra) {
        
        if (br.hasErrors()) {
            return "admin/member/member-form";
        }
        
        if (!command.getMemberPw().equals(command.getMemberPwCon())) {
            br.rejectValue("memberPwCon", "password.mismatch", "비밀번호가 일치하지 않습니다.");
            return "admin/member/member-form";
        }

        try {
            adminMemberService.createMember(command);
            ra.addFlashAttribute("msg", "회원이 등록되었습니다.");
            return "redirect:/admin/members";

        } catch (DuplicateKeyException e) {
            ra.addFlashAttribute("error", "이미 사용 중인 아이디입니다.");
            return "redirect:/admin/members/add";
        }
    }
    
    @PostMapping("/delete")
    public String deleteMembers(@RequestParam(value = "memberNums", required = false) List<String> memberNums, 
                                RedirectAttributes ra) {
        if (memberNums == null || memberNums.isEmpty()) {
            ra.addFlashAttribute("error", "삭제할 회원을 선택해주세요.");
            return "redirect:/admin/members";
        }

        int deletedCount = adminMemberService.deleteMembers(memberNums);
        ra.addFlashAttribute("msg", deletedCount + "명의 회원이 삭제되었습니다.");
        return "redirect:/admin/members";
    }
    
    @GetMapping("/delete/{memberNum}")
    public String deleteSingleMember(@PathVariable String memberNum, RedirectAttributes ra) {
        int deletedCount = adminMemberService.deleteMembers(java.util.Collections.singletonList(memberNum));
        ra.addFlashAttribute("msg", deletedCount + "명의 회원이 삭제되었습니다.");
        return "redirect:/admin/members";
    }
}