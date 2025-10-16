package lappick.controller;

import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lappick.command.PageData;
import lappick.domain.ReviewDTO;
import lappick.goods.dto.GoodsResponse;
import lappick.service.review.ReviewService;

@Controller
@RequestMapping("/employee/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_EMP')")
public class EmployeeReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public String adminReviewList(@RequestParam(value = "searchWord", required = false) String searchWord,
                                  @RequestParam(value = "rating", required = false) Integer rating,
                                  @RequestParam(value = "goodsNum", required = false) String goodsNum,
                                  @RequestParam(value = "page", defaultValue = "1") int page,
                                  Model model) {
        int size = 10;
        PageData<ReviewDTO> pageData = reviewService.getReviewPageForAdmin(searchWord, rating, goodsNum, page, size);
        List<GoodsResponse> goodsFilterList = reviewService.getAllGoods(); // 상품 필터 목록

        model.addAttribute("pageData", pageData);
        model.addAttribute("reviewList", pageData.getItems());
        model.addAttribute("goodsFilterList", goodsFilterList); // view 로 전달

        // 검색 조건 유지를 위해 모든 파라미터를 다시 model에 추가
        model.addAttribute("searchWord", searchWord);
        model.addAttribute("rating", rating);
        model.addAttribute("goodsNum", goodsNum);

        return "thymeleaf/employee/empReviewList";
    }

    @PostMapping("/bulk-action")
    public String bulkAction(@RequestParam("action") String action,
                             @RequestParam("reviewNums") List<Long> reviewNums,
                             RedirectAttributes ra) {
        if ("hide".equals(action)) {
            reviewService.updateReviewStatus(reviewNums, "HIDDEN");
            ra.addFlashAttribute("message", reviewNums.size() + "개의 리뷰가 숨김 처리되었습니다.");
        } else if ("publish".equals(action)) {
            reviewService.updateReviewStatus(reviewNums, "PUBLISHED");
            ra.addFlashAttribute("message", reviewNums.size() + "개의 리뷰가 게시 처리되었습니다.");
        } else if ("delete".equals(action)) {
            reviewService.deleteReviewsByAdmin(reviewNums);
            ra.addFlashAttribute("message", reviewNums.size() + "개의 리뷰가 삭제되었습니다.");
        }
        return "redirect:/employee/reviews";
    }
    
    // 단일 상태 변경 및 삭제를 일괄 처리 API를 재활용하도록 수정
    @GetMapping("/toggle-status/{reviewNum}")
    public String toggleStatus(@PathVariable("reviewNum") Long reviewNum, @RequestParam("currentStatus") String currentStatus, RedirectAttributes ra) {
        String newStatus = "PUBLISHED".equals(currentStatus) ? "HIDDEN" : "PUBLISHED";
        reviewService.updateReviewStatus(Collections.singletonList(reviewNum), newStatus);
        ra.addFlashAttribute("message", reviewNum + "번 리뷰 상태가 변경되었습니다.");
        return "redirect:/employee/reviews";
    }

    @GetMapping("/delete/{reviewNum}")
    public String deleteReview(@PathVariable("reviewNum") Long reviewNum, RedirectAttributes ra) {
        reviewService.deleteReviewsByAdmin(Collections.singletonList(reviewNum));
        ra.addFlashAttribute("message", reviewNum + "번 리뷰가 삭제되었습니다.");
        return "redirect:/employee/reviews";
    }
}