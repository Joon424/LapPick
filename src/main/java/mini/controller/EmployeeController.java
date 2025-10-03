package mini.controller;

import java.security.Principal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import mini.command.EmployeeCommand;
import mini.domain.EmployeeDTO;
import mini.domain.EmployeeListPage;
import mini.service.AutoNumService;
import mini.service.employee.EmployeeService;

@Controller
@RequestMapping("/employee") // 💥 모든 직원 관련 URL은 /employee로 시작
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_EMP')")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final AutoNumService autoNumService;

    /**
     * [추가] 관리자 허브 페이지를 보여주는 메소드
     */
    @GetMapping("/hub")
    public String adminHub() {
        return "thymeleaf/employee/empLogin"; // empLogin.html을 보여줌
    }
    
    /**
     * 💥 [수정] 직원 목록 페이지 (GET /employee)
     */
    @GetMapping
    public String listEmployees(@RequestParam(value = "page", defaultValue = "1") int page,
                              @RequestParam(value = "searchWord", required = false) String searchWord,
                              Model model) {
        EmployeeListPage pageData = employeeService.getEmployeeListPage(searchWord, page);
        model.addAttribute("pageData", pageData);
        model.addAttribute("employees", pageData.getItems());
        return "thymeleaf/employee/empList"; // empList.html을 보여줌
    }

    /**
     * 직원 상세 정보 (GET /employee/{empNum})
     */
    @GetMapping("/{empNum}")
    public String employeeDetail(@PathVariable("empNum") String empNum, Model model) {
        EmployeeDTO dto = employeeService.getEmployeeDetail(empNum);
        model.addAttribute("employeeCommand", dto);
        
        // 💥 [수정] 우리가 만든 파일 이름인 "empInfo"를 바라보도록 변경
        return "thymeleaf/employee/empInfo"; 
    }
    
    /**
     * 직원 등록 폼 (GET /employee/add)
     */
    @GetMapping("/add")
    public String addForm(Model model) {
    	String autoNum = autoNumService.execute("employees", "emp_num", "emp_");
        EmployeeCommand employeeCommand = new EmployeeCommand();
        employeeCommand.setEmpNum(autoNum);
        model.addAttribute("employeeCommand", employeeCommand);

        // 💥 [수정] 우리가 만든 파일 이름인 "empWrite"를 바라보도록 변경
        return "thymeleaf/employee/empWrite";
    }

    /**
     * 직원 등록 처리 (POST /employee/add)
     */
    @PostMapping("/add")
    public String addEmployee(@Validated EmployeeCommand employeeCommand, BindingResult result, Model model) {
        if (result.hasErrors()) {
            // 💥 [수정] 되돌아갈 템플릿 파일 이름을 empForm -> empWrite 로 변경
            return "thymeleaf/employee/empWrite";
        }
        if (!employeeCommand.isEmpPwEqualsEmpPwCon()) {
            result.rejectValue("empPwCon", "employeeCommand.empPwCon", "비밀번호 확인이 일치하지 않습니다.");
            // 💥 [수정] 여기도 empForm -> empWrite 로 변경
            return "thymeleaf/employee/empWrite";
        }
        
        employeeService.createEmployee(employeeCommand);
        return "redirect:/employee";
    }

    /**
     * 직원 수정 폼 (GET /employee/{empNum}/edit)
     */
    @GetMapping("/{empNum}/edit")
    public String editForm(@PathVariable("empNum") String empNum, Model model) {
        EmployeeDTO dto = employeeService.getEmployeeDetail(empNum);
        model.addAttribute("employeeCommand", dto);
        // 💥 [수정] 우리가 만든 파일 이름인 "empEdit"을 바라보도록 변경
        return "thymeleaf/employee/empEdit";
    }


    /**
     * 직원 수정 처리 (POST /employee/edit)
     */
    @PostMapping("/edit")
    // 💥 [수정] @Validated 어노테이션을 제거하여, 수정 시에는 유효성 검사를 건너뛰도록 변경합니다.
    public String updateEmployee(EmployeeCommand employeeCommand, BindingResult result) {
        
        // 💥 [추가] 만약 이름과 같이 필수적인 필드에 대한 수동 검사가 필요하다면 여기에 추가할 수 있습니다.
        // 예: if (employeeCommand.getEmpName() == null || employeeCommand.getEmpName().isBlank()) { ... }

        employeeService.updateEmployee(employeeCommand);
        return "redirect:/employee/" + employeeCommand.getEmpNum();
    }
    /**
     * 직원 개별 삭제 (GET /employee/delete/{empNum})
     */
    @GetMapping("/delete/{empNum}")
    public String deleteSingleEmployee(@PathVariable("empNum") String empNum) {
        employeeService.deleteEmployees(new String[]{empNum});
        return "redirect:/employee";
    }
    
    /**
     * 직원 선택 삭제 (POST /employee/delete)
     */
    @PostMapping("/delete")
    public String deleteEmployees(@RequestParam("empDels") String[] empNums) {
        employeeService.deleteEmployees(empNums);
        return "redirect:/employee";
    }
    
 // [추가] 1. '내 정보 보기' 페이지를 보여주는 메소드
    @GetMapping("/my-page")
    public String myPage(Model model, Principal principal) {
        String empId = principal.getName();
        EmployeeDTO dto = employeeService.getEmployeeDetailById(empId); // ID로 상세 정보 조회
        model.addAttribute("employeeCommand", dto);
        return "thymeleaf/employee/empMyPage";
    }

    // [추가] 2. 비밀번호 변경 요청을 처리하는 메소드
    @PostMapping("/my-page/change-password")
    public String changePassword(@RequestParam("oldPw") String oldPw,
                                 @RequestParam("newPw") String newPw,
                                 @RequestParam("newPwCon") String newPwCon,
                                 Principal principal,
                                 RedirectAttributes ra,
                                 HttpSession session) {
        if (!newPw.equals(newPwCon)) {
            ra.addFlashAttribute("error", "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            return "redirect:/employee/my-page";
        }

        try {
            employeeService.changePassword(principal.getName(), oldPw, newPw);
            ra.addFlashAttribute("message", "비밀번호가 변경되었습니다. 다시 로그인해주세요.");
            
            // Spring Security 인증 정보 삭제 및 세션 무효화 (로그아웃 처리)
            SecurityContextHolder.clearContext();
            session.invalidate();
            
            return "redirect:/login/item.login";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/employee/my-page";
        }
    }
    
}



