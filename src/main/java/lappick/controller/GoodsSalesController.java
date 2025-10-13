package lappick.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lappick.command.PageData;
import lappick.domain.GoodsSalesDTO;
import lappick.service.goods.GoodsService;

import java.util.List;

@Controller
@RequestMapping("/goods")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_EMP')") // 직원만 접근 가능
public class GoodsSalesController {

    private final GoodsService goodsService;

    @GetMapping("/admin/sales-status")
    public String salesStatusPage(@RequestParam(value = "sortBy", defaultValue = "sales") String sortBy,
                                  @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
                                  @RequestParam(value = "page", defaultValue = "1") int page,
                                  @RequestParam(value = "searchWord", required = false) String searchWord,
                                  Model model) {
        int size = 10; // 한 페이지에 15개씩 표시
        PageData<GoodsSalesDTO> pageData = goodsService.getGoodsSalesStatusPage(sortBy, sortDir, searchWord, page, size);
        
        model.addAttribute("pageData", pageData);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("searchWord", searchWord);
        
        return "thymeleaf/employee/empGoodsSales";
    }
}