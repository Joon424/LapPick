package mini.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EmpHomeController {

    @GetMapping("/emp/home")
    public String empHome() {
        // 직원 대시보드(= 예전 empLogin.html)가 templates/thymeleaf/employee/empLogin.html 위치라면:
        return "thymeleaf/employee/empLogin";
    }
}
