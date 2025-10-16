package lappick.admin.employee;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lappick.admin.employee.dto.AdminEmployeePageResponse;
import lappick.admin.employee.dto.EmployeeResponse;
import lappick.admin.employee.dto.EmployeeUpdateRequest;
import lappick.admin.employee.service.AdminEmployeeService;
import lappick.service.AutoNumService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/employees") // [수정] URL을 관리자 전용으로 명확하게 변경
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_EMP')")
public class AdminEmployeeController {

    private final AdminEmployeeService adminEmployeeService;
    private final AutoNumService autoNumService;

    @GetMapping
    public String listEmployees(@RequestParam(value = "page", defaultValue = "1") int page,
                              @RequestParam(value = "searchWord", required = false) String searchWord,
                              Model model) {
        AdminEmployeePageResponse pageData = adminEmployeeService.getEmployeeListPage(searchWord, page);
        model.addAttribute("pageData", pageData);
        model.addAttribute("employees", pageData.getItems());
        return "thymeleaf/employee/empList";
    }
    
    @GetMapping("/hub")
    public String adminHub() {
        return "thymeleaf/employee/empLogin"; 
    }

    @GetMapping("/{empNum}")
    public String employeeDetail(@PathVariable("empNum") String empNum, Model model) {
        EmployeeResponse dto = adminEmployeeService.getEmployeeDetail(empNum);
        model.addAttribute("employeeCommand", dto);
        return "thymeleaf/employee/empInfo"; 
    }
    
    @GetMapping("/add")
    public String addForm(Model model) {
        String autoNum = autoNumService.execute("employees", "emp_num", "emp_");
        EmployeeUpdateRequest employeeCommand = new EmployeeUpdateRequest();
        employeeCommand.setEmpNum(autoNum);
        model.addAttribute("employeeCommand", employeeCommand);
        return "thymeleaf/employee/empWrite";
    }

    @PostMapping("/add")
    public String addEmployee(@Validated EmployeeUpdateRequest employeeCommand, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "thymeleaf/employee/empWrite";
        }
        if (!employeeCommand.isEmpPwEqualsEmpPwCon()) {
            result.rejectValue("empPwCon", "employeeCommand.empPwCon", "비밀번호 확인이 일치하지 않습니다.");
            return "thymeleaf/employee/empWrite";
        }
        
        adminEmployeeService.createEmployee(employeeCommand);
        return "redirect:/admin/employees"; // [수정] 리다이렉트 URL 변경
    }

    @GetMapping("/{empNum}/edit")
    public String editForm(@PathVariable("empNum") String empNum, Model model) {
        EmployeeResponse dto = adminEmployeeService.getEmployeeDetail(empNum);
        model.addAttribute("employeeCommand", dto);
        return "thymeleaf/employee/empEdit";
    }

    @PostMapping("/edit")
    public String updateEmployee(EmployeeUpdateRequest employeeCommand, BindingResult result) {
        adminEmployeeService.updateEmployee(employeeCommand);
        return "redirect:/admin/employees/" + employeeCommand.getEmpNum(); // [수정] 리다이렉트 URL 변경
    }

    @GetMapping("/delete/{empNum}")
    public String deleteSingleEmployee(@PathVariable("empNum") String empNum) {
        adminEmployeeService.deleteEmployees(new String[]{empNum});
        return "redirect:/admin/employees"; // [수정] 리다이렉트 URL 변경
    }
    
    @PostMapping("/delete")
    public String deleteEmployees(@RequestParam("empDels") String[] empNums) {
        adminEmployeeService.deleteEmployees(empNums);
        return "redirect:/admin/employees"; // [수정] 리다이렉트 URL 변경
    }
}