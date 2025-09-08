package mini.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import mini.command.MemberCommand;
import mini.service.AutoNumService;
import mini.service.member.MemberDeleteService;
import mini.service.member.MemberDetailService;
import mini.service.member.MemberListService;
import mini.service.member.MemberUpdateService;
import mini.service.member.MemberWriteService;
import mini.service.member.MembersDeleteService;

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
    MembersDeleteService membersDeleteService;
    @Autowired
    MemberDetailService memberDetailService;
    @Autowired
    MemberUpdateService memberUpdateService;
    @Autowired
    MemberDeleteService memberDeleteService;

    @GetMapping("memberList")
	public String list(
			 @RequestParam(value="page" , required = false , defaultValue = "1") Integer page
			,@RequestParam(value="searchWord", required = false ) String searchWord
			,Model model) {
		memberListService.execute(searchWord,page, model);
		return "thymeleaf/member/memberList";
		//return "member/memberList";
	}

    @RequestMapping(value = "membersDelete")
    public String membersDelete(@RequestParam("nums") String[] memberNums) {
        membersDeleteService.execute(memberNums);
        return "redirect:memberList"; // 경로 수정
    }

    @GetMapping("memberDetail/{memberNum}")
    public String memberDetail(@PathVariable("memberNum") String memberNum, Model model) {
        memberDetailService.execute(model, memberNum);
        return "thymeleaf/employee/memInfo"; // 경로 수정
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

    @GetMapping("memberDelete/{memberNum}")
    public String memberDelete(@PathVariable("memberNum") String memberNum) {
        memberDeleteService.execute(memberNum);
        return "redirect:/member/memberList";
    }
}
