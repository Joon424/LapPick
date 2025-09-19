package mini.controller;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import mini.command.GoodsCommand;
import mini.command.GoodsFilterDTO;
import mini.domain.GoodsDTO;
import mini.domain.GoodsListPage;
import mini.service.AutoNumService;
import mini.service.goods.GoodsService;

@Controller
@RequestMapping("/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;
    private final AutoNumService autoNumService;

    /**
     * 상품 목록 (직원용)
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_EMP')")
    public String goodsListForAdmin(@RequestParam(value = "page", defaultValue = "1") int page,
                                    @RequestParam(value = "searchWord", required = false) String searchWord,
                                    Model model) {
        // 💥 [수정] GoodsFilterDTO 객체를 생성하고 searchWord를 설정합니다.
        GoodsFilterDTO filter = new GoodsFilterDTO();
        filter.setSearchWord(searchWord);

        // 💥 [수정] 서비스 호출 시 filter 객체를 전달합니다.
        GoodsListPage pageData = goodsService.getGoodsListPage(filter, page, 10);
        
        model.addAttribute("pageData", pageData);
        model.addAttribute("goodsList", pageData.getItems());
        model.addAttribute("filter", filter); // 검색어를 유지하기 위해 필터 객체 전달
        return "thymeleaf/goods/goodsList";
    }

    /**
     * 상품 전체 목록 (사용자용)
     */
    // 💥 [수정] URL 경로를 헤더의 링크와 동일한 "goodsFullList"로 변경합니다.
    @GetMapping("/goodsFullList") 
    public String goodsFullList(GoodsFilterDTO filter,
                                @RequestParam(value = "page", defaultValue = "1") int page,
                                Model model) {
        GoodsListPage pageData = goodsService.getGoodsListPage(filter, page, 10);
        model.addAttribute("pageData", pageData);
        model.addAttribute("goodsList", pageData.getItems());
        model.addAttribute("filter", filter);
        return "thymeleaf/goods/goodsFullList";
    }
    /**
     * 상품 상세 정보 (사용자용)
     */
    @GetMapping("/{goodsNum}")
    public String goodsDetail(@PathVariable("goodsNum") String goodsNum, Model model) {
        GoodsDTO dto = goodsService.getGoodsDetail(goodsNum);
        model.addAttribute("goodsCommand", dto);
        model.addAttribute("newLine", "\n");
        return "thymeleaf/item/detailView";
    }

    /**
     * 상품 등록 폼 (직원용)
     */
    @GetMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_EMP')")
    public String addForm(Model model) {
        String autoNum = autoNumService.execute("goods_", "goods_num", 7, "goods");
        GoodsCommand goodsCommand = new GoodsCommand();
        goodsCommand.setGoodsNum(autoNum);
        model.addAttribute("goodsCommand", goodsCommand);
        return "thymeleaf/goods/goodsForm";
    }

    /**
     * 상품 등록 처리 (직원용)
     */
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_EMP')")
    public String addGoods(@Validated GoodsCommand command, BindingResult result) {
        if (result.hasErrors()) {
            return "thymeleaf/goods/goodsForm";
        }
        goodsService.createGoods(command);
        return "redirect:/goods";
    }

    /**
     * 상품 수정 폼 (직원용)
     */
    @GetMapping("/{goodsNum}/edit")
    @PreAuthorize("hasAuthority('ROLE_EMP')")
    public String editForm(@PathVariable("goodsNum") String goodsNum, Model model, HttpSession session) {
        session.removeAttribute("fileList");
        GoodsDTO dto = goodsService.getGoodsDetail(goodsNum);
        model.addAttribute("goodsCommand", dto);
        return "thymeleaf/goods/goodsModify";
    }

    /**
     * 상품 수정 처리 (직원용)
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('ROLE_EMP')")
    public String updateGoods(@Validated GoodsCommand command, BindingResult result,
                              @RequestParam(value="imagesToDelete", required = false) List<String> imagesToDelete) {
        if (result.hasErrors()) {
            return "thymeleaf/goods/goodsModify";
        }
        goodsService.updateGoods(command, imagesToDelete);
        return "redirect:/goods/" + command.getGoodsNum();
    }

    /**
     * 상품 삭제 (개별/다중) (직원용)
     */
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('ROLE_EMP')")
    public String deleteGoods(@RequestParam("nums") String[] goodsNums) {
        goodsService.deleteGoods(goodsNums);
        return "redirect:/goods";
    }
}




















































