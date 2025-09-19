package mini.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import mini.command.GoodsFilterDTO; // 💥 [추가] GoodsFilterDTO import
import mini.domain.GoodsListPage;
import mini.service.goods.GoodsService;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final GoodsService goodsService; 

    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(value = "searchWord", required = false) String searchWord,
                        @RequestParam(value = "page", defaultValue = "1") int page) {

        // 💥 [수정] GoodsFilterDTO 객체를 생성하고 필요한 값을 설정합니다.
        GoodsFilterDTO filter = new GoodsFilterDTO();
        filter.setSearchWord(searchWord);
        
        // 💥 [수정] 서비스 호출 시 filter 객체를 전달합니다.
        // 메인 페이지에서는 상품을 6개만 보여줍니다.
        GoodsListPage pageData = goodsService.getGoodsListPage(filter, page, 6); 
        
        model.addAttribute("goodsList", pageData.getItems());
        
        return "thymeleaf/index";
    }
}