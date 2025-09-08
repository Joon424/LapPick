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
    public String login(@Validated LoginCommand loginCommand, BindingResult result, HttpSession session, Model model) {
        userLoginService.execute(loginCommand, session, result);
        if(result.hasErrors()) {
            // 로그인 실패 시 "일치하지 않는 사용자 정보" 메시지를 로그인 페이지로 전달
            model.addAttribute("errorMessage", "일치하지 않는 사용자 정보입니다.");
            return "thymeleaf/login"; // 로그인 페이지로 포워딩
        }
        return "redirect:/"; // 로그인 성공 시 메인 페이지로 리다이렉트
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

}
