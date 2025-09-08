package mini.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import mini.domain.AuthInfoDTO;
import mini.service.goods.GoodsListService;

@Controller
public class IndexController {

    @Autowired
    GoodsListService goodsListService;

    @GetMapping("/")
    public String index(HttpSession session, Model model, 
                        @RequestParam(value = "searchWord", required = false) String searchWord,
                        @RequestParam(value = "page", required = false, defaultValue = "1") int page) {

        // 로그인 상태 확인
        AuthInfoDTO authInfo = (AuthInfoDTO) session.getAttribute("auth");
        if (authInfo != null) {
            model.addAttribute("isLoggedIn", true);  // 로그인 상태
        } else {
            model.addAttribute("isLoggedIn", false); // 비로그인 상태
        }

        // 상품 목록을 메인 화면에 표시
        goodsListService.execute(searchWord, model, page);
        
        return "thymeleaf/index";  // 메인 페이지로 반환
    }
}
