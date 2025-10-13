package lappick.controller;

import java.util.List; // List import 추가

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lappick.domain.GoodsDTO;
import lappick.service.goods.GoodsService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final GoodsService goodsService; 

    @GetMapping("/")
    public String index(Model model) { // GoodsFilterCommand 파라미터 제거
        // [수정] 베스트 상품 조회 서비스 메서드를 호출합니다.
        List<GoodsDTO> bestGoods = goodsService.getBestGoodsList();
        
        // 모델에 'goodsList'라는 이름으로 베스트 상품 목록을 담아 전달합니다.
        model.addAttribute("goodsList", bestGoods);
        
        return "thymeleaf/index";
    }
}