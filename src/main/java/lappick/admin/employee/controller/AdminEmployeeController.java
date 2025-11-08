package lappick.admin.employee.controller;


import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lappick.admin.employee.dto.AdminEmployeePageResponse;
import lappick.admin.employee.dto.EmployeeResponse;
import lappick.admin.employee.dto.EmployeeUpdateRequest;
import lappick.admin.employee.service.AdminEmployeeService;
import lappick.common.service.AutoNumService;
import lappick.config.ValidationGroups;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/employees")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
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
        return "admin/employee/employee-list";
    }
    
    @GetMapping("/hub")
    public String adminHub() {
        return "admin/employee/dashboard"; 
    }

    @GetMapping("/{empNum}")
    public String employeeDetail(@PathVariable("empNum") String empNum, Model model) {
        EmployeeResponse dto = adminEmployeeService.getEmployeeDetail(empNum);
        model.addAttribute("employeeCommand", dto);
        return "admin/employee/employee-detail"; 
    }
    
    @GetMapping("/add")
    public String addForm(Model model) {
        String autoNum = autoNumService.execute("employees", "emp_num", "emp_");
        EmployeeUpdateRequest employeeCommand = new EmployeeUpdateRequest();
        employeeCommand.setEmpNum(autoNum);
        model.addAttribute("employeeCommand", employeeCommand);
        return "admin/employee/employee-form";
    }

    @PostMapping("/add")
    public String addEmployee(@Validated(ValidationGroups.Create.class) EmployeeUpdateRequest employeeCommand, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("employeeCommand", employeeCommand);
            model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "employeeCommand", result); 
            return "admin/employee/employee-form";
        }

        if (!employeeCommand.isEmpPwEqualsEmpPwCon()) {
             result.rejectValue("empPwCon", "employeeCommand.empPwCon", "비밀번호 확인이 일치하지 않습니다.");
            model.addAttribute("employeeCommand", employeeCommand);
             model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "employeeCommand", result);
            return "admin/employee/employee-form";
        }
        
        adminEmployeeService.createEmployee(employeeCommand);
        ra.addFlashAttribute("message", "직원이 성공적으로 등록되었습니다.");
        return "redirect:/admin/employees";
    }

    @GetMapping("/{empNum}/edit")
    public String editForm(@PathVariable("empNum") String empNum, Model model) {
        EmployeeResponse responseDto = adminEmployeeService.getEmployeeDetail(empNum);
        EmployeeUpdateRequest requestDto = new EmployeeUpdateRequest();
        BeanUtils.copyProperties(responseDto, requestDto);
        
        // 뷰에서 '관리자' 컨텍스트임을 전달 (예: 직원 마이페이지와 폼 공유 시)
        model.addAttribute("isAdminContext", true); 
        
        model.addAttribute("employeeCommand", requestDto);
        return "admin/employee/employee-edit";
    }

    @PostMapping("/edit")
    public String updateEmployee(@Validated(ValidationGroups.Update.class) EmployeeUpdateRequest employeeCommand, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("isAdminContext", true);
            model.addAttribute("employeeCommand", employeeCommand); 
            return "admin/employee/employee-edit";
        }
        adminEmployeeService.updateEmployee(employeeCommand);
        ra.addFlashAttribute("message", "직원 정보가 성공적으로 수정되었습니다.");
        return "redirect:/admin/employees/" + employeeCommand.getEmpNum();
    }

    @GetMapping("/delete/{empNum}")
    public String deleteSingleEmployee(@PathVariable("empNum") String empNum, RedirectAttributes ra) {
        adminEmployeeService.deleteEmployees(new String[]{empNum});
        ra.addFlashAttribute("message", "직원이 삭제되었습니다.");
        return "redirect:/admin/employees";
    }
    
    @PostMapping("/delete")
    public String deleteEmployees(@RequestParam("empDels") String[] empNums, RedirectAttributes ra) {
        adminEmployeeService.deleteEmployees(empNums);
        ra.addFlashAttribute("message", "선택한 " + empNums.length + "명의 직원이 삭제되었습니다.");
        return "redirect:/admin/employees";
    }
}