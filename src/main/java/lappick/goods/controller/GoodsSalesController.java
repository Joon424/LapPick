package lappick.goods.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lappick.common.dto.PageData;
import lappick.goods.dto.GoodsSalesResponse;
import lappick.goods.service.GoodsService;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
public class GoodsSalesController {

    private final GoodsService goodsService;

    @GetMapping("/goods-sales")
    public String salesStatusPage(@RequestParam(value = "sortBy", defaultValue = "sales") String sortBy,
                                  @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
                                  @RequestParam(value = "page", defaultValue = "1") int page,
                                  @RequestParam(value = "searchWord", required = false) String searchWord,
                                  Model model) {
        int size = 10;
        PageData<GoodsSalesResponse> pageData = goodsService.getGoodsSalesStatusPage(sortBy, sortDir, searchWord, page, size);
        
        model.addAttribute("pageData", pageData);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("searchWord", searchWord);
        
        return "admin/goods/goods-sales";
    }
}