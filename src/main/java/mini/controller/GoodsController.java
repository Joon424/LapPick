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
import mini.command.GoodsFilterCommand;
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
     * [수정] 상품 목록 (직원용) - 디버깅 로그 추가
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ROLE_EMP')")
    public String goodsListForAdmin(GoodsFilterCommand filter, Model model) {
        // [수정] 페이지당 표시 개수를 10개에서 5개로 변경합니다.
        GoodsListPage pageData = goodsService.getGoodsListPage(filter, 5); 
        
        model.addAttribute("pageData", pageData);
        model.addAttribute("goodsList", pageData.getItems());
        model.addAttribute("filter", filter);
        return "thymeleaf/goods/goodsList"; // 직원용 상품 목록 페이지
    }

    /**
     * 상품 전체 목록 (사용자용)
     */
    @GetMapping("/goodsFullList")
    public String goodsFullList(GoodsFilterCommand filter, Model model) {
        // 4. 페이지 당 상품 개수 9개로 고정
        GoodsListPage pageData = goodsService.getGoodsListPage(filter, 9); 
        model.addAttribute("pageData", pageData);
        model.addAttribute("goodsList", pageData.getItems());
        model.addAttribute("filter", filter);
        
        // 페이지네이션을 위한 시작/끝 페이지 계산
        int paginationRange = 5; // 한 번에 보여줄 페이지 번호 개수
        int startPage = (int) (Math.floor((pageData.getPage() - 1) / paginationRange) * paginationRange + 1);
        int endPage = Math.min(startPage + paginationRange - 1, pageData.getTotalPages());

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

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
        // [수정] 반환하는 html 파일 이름을 'goodsInfo'로 변경합니다.
        return "thymeleaf/goods/goodsInfo";
    }

    /**
     * 상품 등록 폼 (직원용)
     */
    @GetMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_EMP')")
    public String addForm(Model model) {
        // [수정] AutoNumService를 호출할 때 접두사를 'goods_'로 명확히 지정합니다.
    	String autoNum = autoNumService.execute("goods", "goods_num", "goods_");
        
        GoodsCommand goodsCommand = new GoodsCommand();
        goodsCommand.setGoodsNum(autoNum);
        model.addAttribute("goodsCommand", goodsCommand);
        return "thymeleaf/goods/goodsWrite";
    }


    /**
     * 상품 등록 처리 (직원용)
     */
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_EMP')")
    public String addGoods(@Validated GoodsCommand command, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("goodsCommand", command);
            // [수정] 등록 실패 시 돌아갈 html 파일 이름도 'goodsWrite'로 변경합니다.
            return "thymeleaf/goods/goodsWrite";
        }
        goodsService.createGoods(command);
        return "redirect:/goods/list";
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
        // [수정] 반환하는 html 파일 이름을 'goodsEdit'로 변경합니다.
        return "thymeleaf/goods/goodsEdit";
    }

    /**
     * 상품 수정 처리 (직원용)
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('ROLE_EMP')")
    public String updateGoods(@Validated GoodsCommand command, BindingResult result,
                              @RequestParam(value="imagesToDelete", required = false) List<String> imagesToDelete,
                              // ▼▼▼▼▼ [추가] 상세 설명 이미지 중 삭제할 목록을 받는 파라미터 ▼▼▼▼▼
                              @RequestParam(value="detailDescImagesToDelete", required = false) List<String> detailDescImagesToDelete,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("goodsCommand", command);
            return "thymeleaf/goods/goodsEdit";
        }
        // [수정] 서비스 호출 시 새로운 파라미터를 함께 전달합니다.
        goodsService.updateGoods(command, imagesToDelete, detailDescImagesToDelete);
        return "redirect:/goods/" + command.getGoodsNum();
    }

    /**
     * 상품 삭제 (개별/다중) (직원용)
     */
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('ROLE_EMP')")
    public String deleteGoods(@RequestParam("nums") String[] goodsNums) {
        goodsService.deleteGoods(goodsNums);
        return "redirect:/goods/list"; // [수정] 직원용 목록 페이지로 리다이렉트
    }
}








