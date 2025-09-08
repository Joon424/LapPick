package mini.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpSession;
import mini.command.EmployeeCommand;
import mini.command.MemberCommand;
import mini.service.myPage.EmployeeInfoService;
import mini.service.myPage.EmployeeModifyService;
import mini.service.myPage.EmployeePwUpdateService;
import mini.service.myPage.MemberDropService;
import mini.service.myPage.MemberMyInfoService;
import mini.service.myPage.MemberMyUpdateService;
import mini.service.myPage.MemberPwUpdateService;

@Controller
@RequestMapping("myPage")
public class MyPageController {
	@Autowired
	MemberMyInfoService memberMyInfoService;
	@Autowired
	MemberMyUpdateService memberMyUpdateService;
	@Autowired
	MemberPwUpdateService memberPwUpdateService ;
	@Autowired
	MemberDropService memberDropService ;
	@Autowired
	EmployeeInfoService employeeInfoService;
	@Autowired
	EmployeeModifyService employeeModifyService;
	@GetMapping("memberMyPage")
	public String memMyPage(HttpSession session,Model model) {
		memberMyInfoService.execute(session, model);
		return "thymeleaf/myPage/memberMyPage";
	}
	@GetMapping("memberUpdate")
	public String memberUpdate(HttpSession session,Model model) {
		memberMyInfoService.execute(session, model);
		return "thymeleaf/myPage/myModify";
	}
	@PostMapping("memberModify")
	public String memberModify(MemberCommand memberCommand
			, HttpSession session) {
		memberMyUpdateService.execute(memberCommand, session);
		return "redirect:memberMyPage";
	}
	@RequestMapping(value="memberPwModify" ,method = RequestMethod.GET)
	public String memberPwModify() {
		return  "thymeleaf/myPage/myNewPw";
	}
	@RequestMapping(value="memberPwPro" ,method = RequestMethod.POST)
	public String newPw(
			String oldPw, String newPw,HttpSession session
			) {
		memberPwUpdateService.execute(oldPw, newPw, session);
		return "redirect:memberMyPage";
	}
	@GetMapping("memberDrop")
	public String memberDrop() {
		return "thymeleaf/myPage/memberDrop";
	}
	@PostMapping("memberDropOk")
	public String memberDropOk(String memberPw, HttpSession session) {
		memberDropService.execute(memberPw, session);
		return "redirect:/login/logout";
	}
	@GetMapping("empModify")
	public @ResponseBody Map<String, Object> empPage(HttpSession session, Model model) {
		Map<String, Object> map = employeeInfoService.execute(session, model );
		return map;
	}
	@PostMapping("empModify")
	public String empModify(EmployeeCommand employeeCommand, HttpSession session) {
		employeeModifyService.execute(employeeCommand, session);
		return "redirect:employeeMyPage";
	}
	
	@GetMapping("employeeMyPage")
	public String empMyPage() {
		return "thymeleaf/myPage/employeeInfo";
	}
	@PostMapping("empMyPage")
	public ModelAndView empMyPage(HttpSession session,Model model) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("jsonView");
		employeeInfoService.execute(session, model);
		return mav;
	}
	@Autowired
	EmployeePwUpdateService employeePwUpdateService;
	@PostMapping("empPwPro")
	public String empPwPro(@RequestParam("oldPw") String oldPw
						,String newPw, HttpSession session) {
		employeePwUpdateService.execute(oldPw, newPw, session);
		return "redirect:employeeMyPage";
	}
	 @GetMapping("/empMyPage")
	    public String empMyPage2() {
	        // 여기서 직원을 위한 MyPage를 반환합니다.
	        return "thymeleaf/employee/empLogin"; // empMyPage 페이지를 반환 (템플릿 경로에 맞게 수정)
	    }
	 @GetMapping("/memMyPage")
	    public String memMyPage2(HttpSession session, Model model) {
	        // 여기서 멤버를 위한 MyPage를 반환합니다.
		 memberMyInfoService.execute(session, model); 
	        return "thymeleaf/member/memMyPage"; // memMyPage 페이지를 반환 (템플릿 경로에 맞게 수정)
	    }
}





















