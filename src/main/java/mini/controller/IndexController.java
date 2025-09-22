package mini.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.RequiredArgsConstructor;
import mini.command.GoodsFilterCommand;
import mini.domain.GoodsListPage;
import mini.service.goods.GoodsService;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final GoodsService goodsService; 

    @GetMapping("/")
    public String index(GoodsFilterCommand filter, Model model) {
        // 메인 페이지에서는 상품을 6개만 보여줍니다.
        GoodsListPage pageData = goodsService.getGoodsListPage(filter, 6);
        
        model.addAttribute("goodsList", pageData.getItems());
        
        return "thymeleaf/index";
    }
}