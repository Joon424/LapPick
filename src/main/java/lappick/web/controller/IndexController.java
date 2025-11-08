package lappick.web.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lappick.goods.dto.GoodsResponse;
import lappick.goods.service.GoodsService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final GoodsService goodsService; 

    @GetMapping("/")
    public String index(Model model) {
        List<GoodsResponse> bestGoods = goodsService.getBestGoodsList();
        model.addAttribute("goodsList", bestGoods);
        
        return "index";
    }
}