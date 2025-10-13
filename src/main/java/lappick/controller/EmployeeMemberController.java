package lappick.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lappick.command.MemberCommand;
import lappick.domain.MemberDTO;
import lappick.domain.MemberListPage;
import lappick.service.member.MemberService;

import java.util.List;

@Controller
@RequestMapping("/employee/members") // 관리자용 회원 관리 URL을 명확하게 분리
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_EMP')")
public class EmployeeMemberController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder; // 💥 [추가] PasswordEncoder 주입

    /**
     * 회원 목록 조회
     */
    @GetMapping
    public String listMembers(@RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer size,
                              @RequestParam(required = false) String searchWord,
                              Model model) {
        MemberListPage pageData = memberService.getMemberListPage(page, size, searchWord);
        model.addAttribute("pageData", pageData);
        model.addAttribute("members", pageData.getItems());
        model.addAttribute("searchWord", searchWord);
        model.addAttribute("page", pageData.getPage());
        model.addAttribute("totalPage", pageData.getTotalPages());
        
        // 💥 바로 이 부분입니다! "memberList"를 "memList"로 변경해주세요.
        // 뷰 경로: /templates/thymeleaf/employee/memList.html
        return "thymeleaf/employee/memList"; 
    }

    /**
     * 회원 상세 정보
     */
    @GetMapping("/{memberNum}")
    public String memberDetail(@PathVariable String memberNum, Model model) {
        MemberDTO dto = memberService.getMemberDetail(memberNum);
        
        // 💥 [수정] model에 담는 이름을 "member"에서 "memberCommand"로 변경
        model.addAttribute("memberCommand", dto); 
        
        // 뷰 경로: /templates/thymeleaf/employee/memInfo.html
        return "thymeleaf/employee/memInfo";
    }



    /**
     * 회원 수정 폼
     */
    @GetMapping("/{memberNum}/edit")
    public String editForm(@PathVariable String memberNum, Model model) {
        MemberDTO dto = memberService.getMemberDetail(memberNum);
        model.addAttribute("memberCommand", dto); // 폼을 위한 Command 객체 (DTO 재사용)
        // 뷰 경로: /templates/thymeleaf/employee/memEdit.html
        return "thymeleaf/employee/memEdit";
    }

    /**
     * 회원 수정 처리
     */
    @PostMapping("/update")
    public String updateMember(MemberCommand command, RedirectAttributes ra) {
        memberService.updateMember(command);
        ra.addFlashAttribute("message", "회원 정보가 성공적으로 수정되었습니다.");
        // 수정 후 해당 회원의 상세 페이지로 이동
        return "redirect:/employee/members/" + command.getMemberNum();
    }
    
    /**
     * 회원 등록 폼
     */
    @GetMapping("/add")
    // 💥 [수정] 템플릿에 전달하는 객체를 MemberDTO에서 MemberCommand로 변경
    public String addForm(@ModelAttribute("member") MemberCommand command) {
        // 뷰 경로: /templates/thymeleaf/employee/memWrite.html
        return "thymeleaf/employee/memWrite";
    }

    /**
     * 회원 등록 처리
     */
    /**
     * 회원 등록 처리
     */
    @PostMapping("/add")
    // 💥 [수정] MemberDTO 대신 MemberCommand를 사용하고, BindingResult 위치를 바로 뒤로 이동
    public String addMember(@ModelAttribute("member") @Valid MemberCommand command,
                            BindingResult br, RedirectAttributes ra) {
        
        // 1. 기본 유효성 검사 (비어있는 필드 등)
        if (br.hasErrors()) {
            // 오류가 있으면 다시 등록 폼으로 돌아감
            return "thymeleaf/employee/memWrite";
        }
        
        // 2. 비밀번호와 비밀번호 확인이 일치하는지 검사
        if (!command.getMemberPw().equals(command.getMemberPwCon())) {
            // br 객체에 직접 오류 추가
            br.rejectValue("memberPwCon", "password.mismatch", "비밀번호가 일치하지 않습니다.");
            return "thymeleaf/employee/memWrite";
        }

        try {
            // 3. DTO를 생성하고 Command의 데이터를 DTO로 복사
            MemberDTO dto = new MemberDTO();
            dto.setMemberId(command.getMemberId());
            dto.setMemberName(command.getMemberName());
            dto.setMemberAddr(command.getMemberAddr());
            dto.setMemberAddrDetail(command.getMemberAddrDetail());
            dto.setMemberPost(command.getMemberPost());
            dto.setGender(command.getGender());
            dto.setMemberPhone1(command.getMemberPhone1());
            dto.setMemberPhone2(command.getMemberPhone2());
            dto.setMemberEmail(command.getMemberEmail());
            dto.setMemberBirth(command.getMemberBirth());
            
            // 4. 💥 비밀번호를 암호화하여 DTO에 저장
            String hashedPassword = passwordEncoder.encode(command.getMemberPw());
            dto.setMemberPw(hashedPassword);

            // 5. 암호화된 정보가 담긴 DTO를 서비스로 전달
            memberService.createMember(dto);
            ra.addFlashAttribute("msg", "회원이 등록되었습니다.");
            return "redirect:/employee/members";

        } catch (DuplicateKeyException e) {
            ra.addFlashAttribute("error", "이미 사용 중인 아이디입니다.");
            return "redirect:/employee/members/add";
        }
    }
    

    
    /**
     * 회원 선택 삭제 (벌크 삭제)
     */
    @PostMapping("/delete")
    // 💥 [수정] @RequestParam에 required = false 옵션을 추가하고, 비어있는 경우를 직접 처리
    public String deleteMembers(@RequestParam(value = "memberNums", required = false) List<String> memberNums, 
                                RedirectAttributes ra) {
        if (memberNums == null || memberNums.isEmpty()) {
            ra.addFlashAttribute("error", "삭제할 회원을 선택해주세요.");
            return "redirect:/employee/members";
        }

        int deletedCount = memberService.deleteMembers(memberNums);
        ra.addFlashAttribute("msg", deletedCount + "명의 회원이 삭제되었습니다.");
        return "redirect:/employee/members";
    }
    
    // 💥 [추가] 개별 회원 삭제를 위한 메서드
    @GetMapping("/delete/{memberNum}")
    public String deleteSingleMember(@PathVariable String memberNum, RedirectAttributes ra) {
        // 기존의 여러 명을 삭제하는 서비스를 재활용합니다.
        int deletedCount = memberService.deleteMembers(java.util.Collections.singletonList(memberNum));
        ra.addFlashAttribute("msg", deletedCount + "명의 회원이 삭제되었습니다.");
        return "redirect:/employee/members";
    }
}