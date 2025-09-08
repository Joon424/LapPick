package mini.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import mini.command.EmployeeCommand;
import mini.service.AutoNumService;
import mini.service.employee.EmployeeDeleteService;
import mini.service.employee.EmployeeDetailService;
import mini.service.employee.EmployeeInsertService;
import mini.service.employee.EmployeeListService;
import mini.service.employee.EmployeeUpdateService;
import mini.service.employee.EmployeesDeleteService;
import mini.service.member.MemberListService;

@Controller
@RequestMapping("employee")
public class EmployeeController {
	@Autowired
	AutoNumService autoNumService;
	@Autowired
	EmployeeInsertService employeeInsertService;
	@Autowired
	EmployeeListService employeeListService;
	@Autowired
	EmployeesDeleteService employeesDeleteService;
	@Autowired
	EmployeeDetailService employeeDetailService;
	@Autowired
	MemberListService memberListService;
	@RequestMapping(value="employeeList", method=RequestMethod.GET)
	//페이징과 검색을 위한 코드를 추가하겠습니다.
	public String empList(
			@RequestParam(value="page", required = false, defaultValue = "1" ) int page,
			@RequestParam(value="searchWord" , required = false) String searchWord,
			Model model) {
		//직원 목록을 가져오도록 해보자.
		employeeListService.execute(searchWord, page,model);
		return "thymeleaf/employee/employeeList";
	}
	@GetMapping("empRegist")
	public String form(Model model ) {
		String autoNum = autoNumService.execute("emp_", "emp_num", 5, "employees");
		EmployeeCommand  employeeCommand = new EmployeeCommand();
		employeeCommand.setEmpNum(autoNum);
		model.addAttribute("employeeCommand", employeeCommand);
		return "thymeleaf/employee/empForm";
	}
	@RequestMapping(value="empRegist", method=RequestMethod.POST)
	// html에 있는 값을 command로 받아와야 한다.
	// html에서 넘어온 값에 대해 유효성 검사를 합니다.
	public String form(@Validated EmployeeCommand employeeCommand,BindingResult result , Model model ) {
		// 정상적으로 저장되었다면 직원목록페이지로 이동
		if(result.hasErrors()) {
			// 오류가 있다면 employeeForm페이지가 열리게 합니다.
			return "thymeleaf/employee/empForm";
		}
		// 모두 입력을 했다면 비밀번호확인 검사
		if (!employeeCommand.isEmpPwEqualsEmpPwCon()) {
			System.out.println("비밀번호 확인이 다릅니다.");
			//틀렸으면 다시 employeeForm페이지가 열리게 합니다.
			return "thymeleaf/employee/empForm";
		}
		//모든 오류가 없으면 디비에 저장
		employeeInsertService.execute(employeeCommand);
		return "redirect:empList";
	}
	
	@PostMapping("empsDelete")
	public String membersDelete( 
			@RequestParam(value="empDels") String empsDel []) {
		employeesDeleteService.execute(empsDel);
		return "redirect:empList";
	}
	@RequestMapping(value="employeeDetail",method=RequestMethod.GET)
	public String employeeDetail(@RequestParam(value = "empNum") String empNum, Model model) {
		employeeDetailService.execute(empNum, model);
		return "thymeleaf/employee/empDetail";
	}
	@RequestMapping(value = "empModify", method = RequestMethod.GET)
	public String employeeUpdate(@RequestParam(value = "empNum") String empNum, Model model) {
		employeeDetailService.execute(empNum, model);
		return "thymeleaf/employee/empUpdate";
	}

	@Autowired
	EmployeeUpdateService employeeUpdateService;
	@RequestMapping(value = "empModify", method = RequestMethod.POST)
	public String employeeUpdate(@Validated EmployeeCommand employeeCommand, BindingResult result) {

		if (result.hasErrors()) {

			return "thymeleaf/employee/empUpdate";
		}
		employeeUpdateService.execute(employeeCommand);
		//수정하고 직원상세페이지로 
		return "redirect:employeeDetail?empNum=" + employeeCommand.getEmpNum();
	}

	@Autowired
	EmployeeDeleteService employeeDeleteService;
	@GetMapping("empDelete")
	public String employeeDelete(@RequestParam(value = "empNum") String empNum) {

		employeeDeleteService.execute(empNum);

		return "redirect:empList";

	}

}














