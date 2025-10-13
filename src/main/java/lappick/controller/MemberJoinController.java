package lappick.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lappick.command.UserCommand;
import lappick.service.memberjoin.MemberJoinService;

@Controller
@RequestMapping("register")
public class MemberJoinController {
	@Autowired
	MemberJoinService memberJoinService;
	
	@ModelAttribute
	public UserCommand userCommand() {
		return new UserCommand();
	}
	@RequestMapping("userAgree")
	public String agree() {
		return "thymeleaf/memberJoin/agree";
	}
	 @GetMapping("userAgree")
	    public String showAgreePage() {
	        // "agree"는 Thymeleaf 템플릿의 이름입니다
	        return "thymeleaf/memberJoin/agree";  // /src/main/resources/templates/thymeleaf/memberJoin/agree.html
	    }
	
	@GetMapping("userWrite")
	public String userWrite(/*Model model*/) {
		//model.addAttribute("userCommand", new UserCommand());
		return "thymeleaf/memberJoin/memForm";
	}
	@PostMapping("userWrite")
	// @Validated:유효성검사
	public String userWrite1(@Validated UserCommand userCommand
			,BindingResult result) {
		if(result.hasErrors()) {
			return "thymeleaf/memberJoin/memForm";
		}
		memberJoinService.execute(userCommand);
		return "redirect:/register/welcome";
	}
	
	  // 회원가입 완료 페이지
    @RequestMapping("welcome")
    public String welcome() {
        return "thymeleaf/memberJoin/welcome"; // 성공적으로 등록된 후 보여줄 페이지
    }
}










