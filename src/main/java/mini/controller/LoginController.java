package mini.controller;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mini.command.LoginCommand;
import mini.domain.AuthInfoDTO;
import mini.service.IdCheckService;
import mini.service.login.UserLoginService;

@Controller
@RequestMapping("login")
public class LoginController {

    @Autowired
    private IdCheckService idcheckService;
    
    @Autowired
    private UserLoginService userLoginService;

    @PostMapping("userIdCheck")
    public @ResponseBody Integer userIdCheck(String userId) {
        return idcheckService.execute(userId);
    }

    @PostMapping("login")
    public String login(
            @Validated LoginCommand loginCommand,
            BindingResult result,
            HttpSession session,
            Model model,
            @RequestParam(value = "returnUrl", required = false) String returnUrl
    ) {
        userLoginService.execute(loginCommand, session, result);
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "일치하지 않는 사용자 정보입니다.");
            return "thymeleaf/login";
        }

        AuthInfoDTO auth = (AuthInfoDTO) session.getAttribute("auth");
        String grade = (auth != null ? auth.getGrade() : null);

        if (returnUrl != null && !returnUrl.isBlank()) {
            if (!"emp".equals(grade) && isEmployeeOnlyUrl(returnUrl)) {
                return "redirect:/";
            }
            return "redirect:" + returnUrl;
        }

        return "emp".equals(grade) ? "redirect:/myPage/empMyPage" : "redirect:/";
    }

    
 // --- 아래 유틸 메서드를 컨트롤러에 추가 ---
    private boolean isEmployeeOnlyUrl(String url) {
        // 직원 전용으로 묶을 엔드포인트 prefix를 여기에 정의
        // (goods 관리용, 직원 관리용 등)
        String[] empOnlyPrefixes = {
            "/employee/",
            "/goods/goodsForm",
            "/goods/goodsWrite",
            "/goods/goodsModify",
            "/goods/goodsDelete",
            "/goods/productsDelete",
            "/goods/goodsRedirect",
            "/goodsIpgo/"
        };
        for (String p : empOnlyPrefixes) {
            if (url.startsWith(p)) return true;
        }
        return false;
    }


    @GetMapping("logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 세션 무효화 (로그아웃)
        return "redirect:/"; // 메인 페이지로 리다이렉트
    }

    @GetMapping("item.login")
    public String item() {
        return "thymeleaf/login"; // 로그인 페이지로 이동
    }
    
    @GetMapping("/emp.login")  // 직원용 별도 화면을 쓰는 경우
    public String empLogin() {
        return "thymeleaf/employee/empLogin"; // 직원 로그인 템플릿
    }
    
    @PostMapping("item.login")
    public void item(LoginCommand loginCommand, BindingResult result, HttpSession session, HttpServletResponse response) {
        userLoginService.execute(loginCommand, session, result);
        response.setContentType("text/html; charset=utf-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String str = "<script language='javascript'>";
        str += " opener.location.reload();";
        str += " window.self.close();";
        str += " </script>"; 
        out.print(str);
        out.close();
    }

    // 유저 아이콘 클릭 시 세션 정보에 따라 이동 경로를 다르게 설정
    @GetMapping("userIcon")
    public String userIconRedirect(HttpSession session) {
        AuthInfoDTO authInfo = (AuthInfoDTO) session.getAttribute("auth");
        
        if (authInfo == null) {
            System.out.println("세션에 로그인 정보가 없습니다.");
            return "redirect:/login/item.login";  // 로그인 페이지로 리다이렉트
        }
        
        System.out.println("로그인한 사용자 정보: " + authInfo.getUserId());
        
        // 로그인한 사용자가 멤버인지 직원인지 확인하고, 리다이렉트 경로 설정
        if ("mem".equals(authInfo.getGrade())) { // 변경: "mem"과 "emp"로 비교
            return "redirect:/myPage/memMyPage";
        }
        if ("emp".equals(authInfo.getGrade())) { // 변경: "mem"과 "emp"로 비교
            return "redirect:/myPage/empMyPage";
        }

        return "redirect:/"; // 예외 처리 (기본적으로 메인페이지로 돌아가기)
    }

    @PostMapping("user.login")
    public String userLoginAlias(
            @Validated LoginCommand loginCommand,
            BindingResult result,
            HttpSession session,
            Model model,
            @RequestParam(value = "returnUrl", required = false) String returnUrl
    ) {
        return login(loginCommand, result, session, model, returnUrl);
    }


    
}
