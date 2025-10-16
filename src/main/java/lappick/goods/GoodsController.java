package lappick.goods;

import java.security.Principal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lappick.goods.dto.GoodsStockResponse;
import lappick.goods.dto.StockHistoryPageResponse;
import lappick.goods.dto.GoodsFilterRequest;
import lappick.goods.dto.GoodsPageResponse;
import lappick.goods.dto.GoodsRequest;
import lappick.service.AutoNumService;
import lappick.service.qna.QnaService;
import lombok.RequiredArgsConstructor;

//▼▼▼▼▼ [추가] CornerController에서 사용하던 의존성 주입 ▼▼▼▼▼
import lappick.domain.QnaDTO;
import lappick.domain.ReviewPageDTO;
import lappick.domain.ReviewSummaryDTO;
import lappick.mapper.QnaMapper;
import lappick.member.dto.MemberResponse;
import lappick.member.mapper.MemberMapper;
import lappick.service.review.ReviewService;
//▲▲▲▲▲ [추가] CornerController에서 사용하던 의존성 주입 ▲▲▲▲▲

@Controller
@RequestMapping("/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;
    private final AutoNumService autoNumService;
    private final QnaService qnaService;
    private final MemberMapper memberMapper;
    
 // ▼▼▼▼▼ [추가] CornerController에서 사용하던 의존성 주입 ▼▼▼▼▼
    private final QnaMapper qnaMapper;
    private final ReviewService reviewService;
    // ▲▲▲▲▲ [추가] CornerController에서 사용하던 의존성 주입 ▲▲▲▲▲

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ROLE_EMP')")
    public String goodsListForAdmin(GoodsFilterRequest filter, Model model) {
        GoodsPageResponse pageData = goodsService.getGoodsListPage(filter, 5); 
        
        model.addAttribute("pageData", pageData);
        model.addAttribute("goodsList", pageData.getItems());
        model.addAttribute("filter", filter);
        return "thymeleaf/goods/goodsList";
    }

    @GetMapping("/goodsFullList")
    public String goodsFullList(GoodsFilterRequest filter, Model model) {
        GoodsPageResponse pageData = goodsService.getGoodsListPage(filter, 9); 
        model.addAttribute("pageData", pageData);
        model.addAttribute("goodsList", pageData.getItems());
        model.addAttribute("filter", filter);
        
        int paginationRange = 5;
        int startPage = (int) (Math.floor((pageData.getPage() - 1) / paginationRange) * paginationRange + 1);
        int endPage = Math.min(startPage + paginationRange - 1, pageData.getTotalPages());

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "thymeleaf/goods/goodsFullList";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_EMP')")
    public String addForm(Model model) {
    	String autoNum = autoNumService.execute("goods", "goods_num", "goods_");
        
        GoodsRequest goodsRequest = new GoodsRequest();
        goodsRequest.setGoodsNum(autoNum);
        model.addAttribute("goodsCommand", goodsRequest);
        return "thymeleaf/goods/goodsWrite";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_EMP')")
    public String addGoods(@Validated GoodsRequest command, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("goodsCommand", command);
            return "thymeleaf/goods/goodsWrite";
        }
        goodsService.createGoods(command);
        return "redirect:/goods/list";
    }

    @GetMapping("/{goodsNum}/edit")
    @PreAuthorize("hasAuthority('ROLE_EMP')")
    public String editForm(@PathVariable("goodsNum") String goodsNum, Model model, HttpSession session) {
        session.removeAttribute("fileList");
        GoodsStockResponse dto = goodsService.getGoodsDetailWithStock(goodsNum);
        model.addAttribute("goodsCommand", dto);
        return "thymeleaf/goods/goodsEdit";
    }
    
    @GetMapping("/{goodsNum}")
    @PreAuthorize("hasAuthority('ROLE_EMP')")
    public String goodsDetail(@PathVariable("goodsNum") String goodsNum,
                              @RequestParam(value = "historyPage", defaultValue = "1") int historyPage,
                              // [추가] 재고 이력 창의 표시 여부를 받는 파라미터
                              @RequestParam(value = "showHistory", required = false, defaultValue = "false") boolean showHistory,
                              Model model) {
        GoodsStockResponse dto = goodsService.getGoodsDetailWithStock(goodsNum);
        model.addAttribute("goodsCommand", dto);
        
        StockHistoryPageResponse historyPageData = goodsService.getStockHistoryPage(goodsNum, historyPage, 5);
        model.addAttribute("historyPageData", historyPageData);
        
        // [추가] 표시 여부 상태를 모델에 담아 전달
        model.addAttribute("showHistory", showHistory); 
        
        return "thymeleaf/goods/goodsInfo";
    }
    
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('ROLE_EMP')")
    public String updateGoods(@Validated GoodsRequest command, BindingResult result,
                              @RequestParam(value="imagesToDelete", required = false) List<String> imagesToDelete,
                              @RequestParam(value="detailDescImagesToDelete", required = false) List<String> detailDescImagesToDelete,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("goodsCommand", command);
            return "thymeleaf/goods/goodsEdit";
        }
        goodsService.updateGoods(command, imagesToDelete, detailDescImagesToDelete);
        return "redirect:/goods/" + command.getGoodsNum();
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('ROLE_EMP')")
    public String deleteGoods(@RequestParam("nums") String[] goodsNums) {
        goodsService.deleteGoods(goodsNums);
        return "redirect:/goods/list";
    }
    
    @PostMapping("/stock-change")
    @PreAuthorize("hasAuthority('ROLE_EMP')")
    public String stockChange(@RequestParam("goodsNum") String goodsNum,
                              @RequestParam("quantity") int quantity,
                              // [수정] memo 파라미터를 선택적으로 받도록 required=false 추가
                              @RequestParam(value = "memo", required = false) String memo,
                              RedirectAttributes ra) {
        try {
            goodsService.changeStock(goodsNum, quantity, memo);
            String message = (quantity > 0) ? 
                "'" + goodsNum + "' 상품이 " + quantity + "개 입고 처리되었습니다." :
                "'" + goodsNum + "' 상품이 " + (-quantity) + "개 출고/차감 처리되었습니다.";
            ra.addFlashAttribute("message", message);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/goods/list";
    }
    
    // ▼▼▼▼▼ [추가] CornerController의 상품 상세 보기 메소드를 통합 ▼▼▼▼▼
    @GetMapping("/detail/{goodsNum}") // URL을 /corner/detailView/{num} -> /goods/detail/{num} 으로 변경
    public String showGoodsDetail(
            @PathVariable("goodsNum") String goodsNum,
            @RequestParam(name = "reviewPage", defaultValue = "1") int reviewPage,
            Model model,
            HttpServletRequest request, HttpServletResponse response,
            Principal principal) {

        // GoodsService에 통합된 메소드를 호출
        GoodsStockResponse dto = goodsService.getGoodsDetailForView(goodsNum, request, response);
        List<QnaDTO> qnaList = qnaMapper.selectQnaByGoodsNum(goodsNum);

        String loginMemberNum = null;
        boolean isEmployee = false;

        if (principal != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            MemberResponse member = memberMapper.selectOneById(auth.getName());
            if (member != null) {
                loginMemberNum = member.getMemberNum();
            }
            isEmployee = auth.getAuthorities().stream()
                             .anyMatch(a -> a.getAuthority().equals("ROLE_EMP"));
        }

        ReviewPageDTO reviewPageData = reviewService.findReviewsByGoodsNum(goodsNum, reviewPage, 5);
        ReviewSummaryDTO reviewSummary = reviewService.getReviewSummary(goodsNum);

        model.addAttribute("loginMemberNum", loginMemberNum);
        model.addAttribute("isEmployee", isEmployee);
        model.addAttribute("qnaList", qnaList);
        model.addAttribute("goods", dto); // 뷰에서 사용하는 이름 'goods' 유지
        model.addAttribute("reviewPageData", reviewPageData);
        model.addAttribute("reviewSummary", reviewSummary);

        return "thymeleaf/item/detailView";
    }
    // ▲▲▲▲▲ [추가] CornerController의 상품 상세 보기 메소드를 통합 ▲▲▲▲▲
    
}