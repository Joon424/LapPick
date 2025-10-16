package lappick.controller;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lappick.command.ReviewCommand;
import lappick.domain.ReviewDTO;
import lappick.goods.GoodsMapper;
import lappick.goods.dto.GoodsResponse;
import lappick.service.review.ReviewService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/review")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_MEM')") // 회원만 접근 가능
public class ReviewController {
    
    private final GoodsMapper goodsMapper;
    private final ReviewService reviewService;

    // 리뷰 작성 폼을 보여주는 메소드
    @GetMapping("/write")
    public String reviewForm(@RequestParam("purchaseNum") String purchaseNum,
                             @RequestParam("goodsNum") String goodsNum,
                             Model model) {
        
        // 리뷰할 상품 정보 조회
        GoodsResponse goods = goodsMapper.selectOne(goodsNum);
        
        ReviewCommand reviewCommand = new ReviewCommand();
        reviewCommand.setPurchaseNum(purchaseNum);
        reviewCommand.setGoodsNum(goodsNum);
        
        model.addAttribute("goods", goods);
        model.addAttribute("reviewCommand", reviewCommand);
        
        return "thymeleaf/review/reviewWrite"; // 리뷰 작성 페이지 html
    }
    
    // 작성된 리뷰를 DB에 저장하는 메소드
    @PostMapping("/write")
    public String writeReview(@Validated ReviewCommand command, BindingResult result, Model model) {
        
        if (result.hasErrors()) {
            // 유효성 검사 실패 시, 다시 작성 폼으로 돌아감
            GoodsResponse goods = goodsMapper.selectOne(command.getGoodsNum());
            model.addAttribute("goods", goods);
            model.addAttribute("reviewCommand", command);
            return "thymeleaf/review/reviewWrite";
        }
        
        reviewService.writeReview(command);
        
        // 리뷰 작성 완료 후, 주문 내역 페이지로 리다이렉트
        return "redirect:/purchase/my-orders";
    }
    
    /**
     * [추가] 리뷰 삭제 처리 메소드
     */
    @PostMapping("/delete/{reviewNum}")
    public String deleteReview(@PathVariable("reviewNum") Long reviewNum, RedirectAttributes ra) {
        try {
            reviewService.deleteReview(reviewNum);
            ra.addFlashAttribute("message", "리뷰가 성공적으로 삭제되었습니다.");
        } catch (SecurityException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/member/my-reviews";
    }
    
    /**
     * [추가] 리뷰 수정 폼을 보여주는 메소드
     */
    @GetMapping("/edit/{reviewNum}")
    public String editReviewForm(@PathVariable("reviewNum") Long reviewNum, Model model) {
        try {
            // 1. 수정할 리뷰 데이터를 서비스에서 가져옴 (본인 확인 포함)
            ReviewDTO review = reviewService.getReviewForEdit(reviewNum);
            
            // 2. DTO의 데이터를 form을 채우기 위한 Command 객체로 옮김
            ReviewCommand command = new ReviewCommand();
            command.setPurchaseNum(review.getPurchaseNum());
            command.setGoodsNum(review.getGoodsNum());
            command.setReviewRating(review.getReviewRating());
            command.setReviewContent(review.getReviewContent());
            
            // 3. 리뷰할 상품 정보와 기존 리뷰 내용을 모델에 담아 전달
            GoodsResponse goods = goodsMapper.selectOne(review.getGoodsNum());
            model.addAttribute("goods", goods);
            model.addAttribute("reviewCommand", command);
            model.addAttribute("reviewNum", reviewNum); // 수정할 리뷰의 번호를 전달
            model.addAttribute("existingImages", 
                (review.getReviewImage() != null && !review.getReviewImage().isEmpty()) ?
                review.getReviewImage().split("/") : null);

            // 기존 작성 페이지를 '수정 모드'로 재사용
            return "thymeleaf/review/reviewWrite"; 
            
        } catch (SecurityException e) {
            // 권한이 없을 경우, 내 리뷰 목록으로 돌려보냄
            return "redirect:/member/my-reviews";
        }
    }
    
    /**
     * [추가] 수정된 리뷰를 DB에 저장하는 메소드
     */
    @PostMapping("/update")
    public String updateReview(@Validated ReviewCommand command, BindingResult result,
                               @RequestParam("reviewNum") Long reviewNum,
                               @RequestParam(value="imagesToDelete", required = false) String[] imagesToDelete,
                               Model model, RedirectAttributes ra) {

        if (result.hasErrors()) {
            // 유효성 검사 실패 시, 다시 수정 폼으로 돌아감
            GoodsResponse goods = goodsMapper.selectOne(command.getGoodsNum());
            model.addAttribute("goods", goods);
            model.addAttribute("reviewCommand", command);
            model.addAttribute("reviewNum", reviewNum);
            // 이전에 등록된 이미지 목록도 다시 전달해야 함
            ReviewDTO review = reviewService.getReviewForEdit(reviewNum);
            model.addAttribute("existingImages", 
                (review.getReviewImage() != null && !review.getReviewImage().isEmpty()) ?
                review.getReviewImage().split("/") : null);
            return "thymeleaf/review/reviewWrite";
        }
        
        try {
            reviewService.updateReview(command, reviewNum, imagesToDelete);
            ra.addFlashAttribute("message", "리뷰가 성공적으로 수정되었습니다.");
        } catch (SecurityException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        
        // 리뷰 수정 완료 후, '내가 작성한 리뷰' 목록 페이지로 리다이렉트
        return "redirect:/member/my-reviews";
    }
}