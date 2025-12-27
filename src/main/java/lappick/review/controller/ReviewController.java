package lappick.review.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lappick.goods.dto.GoodsResponse;
import lappick.goods.mapper.GoodsMapper;
import lappick.review.domain.Review;
import lappick.review.dto.ReviewWriteRequest;
import lappick.review.service.ReviewService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/review")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_MEMBER')")
public class ReviewController {

    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);

    // 리뷰 작성/수정 폼에서 상품 정보를 표시하기 위해 GoodsMapper를 주입받습니다.
    private final GoodsMapper goodsMapper;
    private final ReviewService reviewService;

    /**
     * 리뷰 작성 폼 GET
     */
    @GetMapping("/write")
    public String reviewForm(@RequestParam("purchaseNum") String purchaseNum,
                             @RequestParam("goodsNum") String goodsNum,
                             Model model) {
        try {
            GoodsResponse goods = goodsMapper.selectOne(goodsNum);
            if (goods == null) {
                log.warn("리뷰 작성 폼 요청: 상품 정보 없음 (goodsNum={})", goodsNum);
                // 리뷰 대상 상품 정보가 없으므로, 로그를 남기고 홈으로 리다이렉트합니다.
                return "redirect:/";
            }
            ReviewWriteRequest reviewRequest = new ReviewWriteRequest();
            reviewRequest.setPurchaseNum(purchaseNum);
            reviewRequest.setGoodsNum(goodsNum);

            model.addAttribute("goods", goods);
            model.addAttribute("reviewCommand", reviewRequest);

            // 작성 폼(review-form) 재사용을 위해 'isEditMode' 플래그를 false로 설정합니다.
            model.addAttribute("isEditMode", false);

            return "user/review/review-form";

        } catch (Exception e) {
             log.error("리뷰 작성 폼 로딩 중 오류 발생", e);
             // 폼 로딩 중 예외 발생 시, 에러 로깅 후 홈으로 리다이렉트합니다.
             model.addAttribute("error", "리뷰 작성 페이지를 로드하는 중 오류가 발생했습니다.");
             return "redirect:/";
        }
    }
    
    /**
     * 리뷰 작성 처리 POST
     */
    @PostMapping("/write")
    public String writeReview(@Validated @ModelAttribute("reviewCommand") ReviewWriteRequest command, BindingResult result,
                              Model model, RedirectAttributes ra, Authentication authentication) {

        // 유효성 검사 실패 시 폼으로 복귀
        if (result.hasErrors()) {
            log.warn("리뷰 작성 유효성 검사 실패: {}", result.getAllErrors());
            try {
                GoodsResponse goods = goodsMapper.selectOne(command.getGoodsNum());
                model.addAttribute("goods", goods);
                model.addAttribute("isEditMode", false);
                return "user/review/review-form";
            } catch (Exception e) {
                log.error("리뷰 작성 유효성 검사 실패 후 상품 정보 로딩 중 오류", e);
                ra.addFlashAttribute("error", "상품 정보를 불러오는 데 실패했습니다.");
                return "redirect:/";
            }
        }

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("리뷰 작성 시도: 인증되지 않은 사용자");
                throw new SecurityException("로그인이 필요합니다.");
            }

            String userId = authentication.getName();
            reviewService.writeReview(command, userId);

            ra.addFlashAttribute("message", "리뷰가 성공적으로 등록되었습니다.");
            return "redirect:/member/my-reviews";

        } catch (IllegalArgumentException | IllegalStateException e) {
            // 배송완료/구매자/중복 등 “리뷰 작성 제약” 위반 케이스
            log.warn("리뷰 작성 제약 위반: purchaseNum={}, goodsNum={}, message={}",
                    command.getPurchaseNum(), command.getGoodsNum(), e.getMessage());
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/purchases/my-orders";

        } catch (SecurityException e) {
            log.error("리뷰 작성 권한 오류", e);
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/login";

        } catch (Exception e) {
            log.error("리뷰 등록 중 오류 발생", e);
            // 서비스 로직 수행 중 예외 발생 시, 에러 메시지와 함께 폼을 다시 보여줍니다.
            try {
                GoodsResponse goods = goodsMapper.selectOne(command.getGoodsNum());
                model.addAttribute("goods", goods);
                model.addAttribute("isEditMode", false);
                model.addAttribute("errorMessage", "리뷰 등록 중 오류가 발생했습니다: " + e.getMessage());
                return "user/review/review-form";
            } catch (Exception goodsEx) {
                log.error("리뷰 등록 오류 후 상품 정보 로딩 중 추가 오류", goodsEx);
                ra.addFlashAttribute("error", "리뷰 등록 및 상품 정보 로딩 중 오류가 발생했습니다.");
                return "redirect:/";
            }
        }
    }


    /**
     * 리뷰 삭제 처리 POST
     */
    @PostMapping("/delete/{reviewNum}")
    public String deleteReview(@PathVariable("reviewNum") Long reviewNum, RedirectAttributes ra) {
        try {
            reviewService.deleteReview(reviewNum);
            // 삭제 성공 시, 'deleteMessage' 플래그를 전달하여 뷰에서 강조 표시합니다.
            ra.addFlashAttribute("deleteMessage", "리뷰가 성공적으로 삭제되었습니다.");
        } catch (SecurityException e) {
             log.warn("리뷰 삭제 권한 오류: reviewNum={}", reviewNum, e);
             ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("리뷰 삭제 중 오류 발생: reviewNum={}", reviewNum, e);
            ra.addFlashAttribute("error", "리뷰 삭제 중 오류가 발생했습니다.");
        }
        // 작업 완료 후, 내 리뷰 목록 페이지로 리다이렉트합니다.
        return "redirect:/member/my-reviews";
    }

    /**
     * 리뷰 수정 폼 GET
     */
    @GetMapping("/edit/{reviewNum}")
    public String editReviewForm(@PathVariable("reviewNum") Long reviewNum, Model model, RedirectAttributes ra) {
        try {
            // 서비스 레이어에서 권한 검사 후 리뷰 정보 반환
            Review review = reviewService.getReviewForEdit(reviewNum); 
            
            // Domain -> DTO 매핑
            ReviewWriteRequest command = new ReviewWriteRequest();
            command.setPurchaseNum(review.getPurchaseNum());
            command.setGoodsNum(review.getGoodsNum());
            command.setReviewRating(review.getReviewRating());
            command.setReviewContent(review.getReviewContent());

            GoodsResponse goods = goodsMapper.selectOne(review.getGoodsNum());
             if (goods == null) {
                 // 이 경우는 데이터 정합성이 깨진 상황일 수 있으나, 방어 코드를 추가합니다.
                 log.warn("리뷰 수정 폼: 상품 정보 없음 (goodsNum={})", review.getGoodsNum());
                 throw new IllegalArgumentException("리뷰 대상 상품 정보를 찾을 수 없습니다.");
             }
             
            model.addAttribute("goods", goods);
            model.addAttribute("reviewCommand", command);
            model.addAttribute("reviewNum", reviewNum);
            model.addAttribute("existingImages",
                (review.getReviewImage() != null && !review.getReviewImage().isEmpty()) ?
                review.getReviewImage().split("/") : null);
            
            // 폼 재사용을 위해 'isEditMode' 플래그를 true로 설정합니다.
            model.addAttribute("isEditMode", true);

            return "user/review/review-form";

        } catch (SecurityException e) {
            log.warn("리뷰 수정 폼 접근 권한 오류: reviewNum={}", reviewNum, e);
            ra.addFlashAttribute("error", "리뷰를 수정할 권한이 없습니다.");
            return "redirect:/member/my-reviews";
        } catch (Exception e) {
            log.error("리뷰 수정 폼 로딩 중 오류 발생: reviewNum={}", reviewNum, e);
            ra.addFlashAttribute("error", "리뷰 수정 페이지를 로드하는 중 오류가 발생했습니다.");
            return "redirect:/member/my-reviews";
        }
    }

    /**
     * 리뷰 수정 처리 POST
     */
    @PostMapping("/update")
    public String updateReview(@Validated @ModelAttribute("reviewCommand") ReviewWriteRequest command, BindingResult result,
                               @RequestParam("reviewNum") Long reviewNum,
                               @RequestParam(value="imagesToDelete", required = false) String[] imagesToDelete,
                               Model model, RedirectAttributes ra) {

        // 유효성 검사 실패 시 폼으로 복귀
        if (result.hasErrors()) {
            log.warn("리뷰 수정 유효성 검사 실패: {}", result.getAllErrors());
            try {
                GoodsResponse goods = goodsMapper.selectOne(command.getGoodsNum());
                model.addAttribute("goods", goods);
                model.addAttribute("reviewNum", reviewNum);
                 
                // 유효성 검사 실패 시, 폼을 다시 로드하기 위해 기존 이미지 목록을 다시 조회하여 전달합니다.
                Review review = reviewService.getReviewForEdit(reviewNum); 
                model.addAttribute("existingImages",
                    (review.getReviewImage() != null && !review.getReviewImage().isEmpty()) ?
                    review.getReviewImage().split("/") : null);
                model.addAttribute("isEditMode", true);
                return "user/review/review-form";
                
            } catch(SecurityException se) {
                 // 폼 재로드를 위한 데이터 조회(getReviewForEdit) 시에도 권한 검사가 실패할 수 있습니다.
                 log.warn("리뷰 수정 유효성 검사 실패 후 폼 재로드 중 권한 오류", se);
                 ra.addFlashAttribute("error", "리뷰 수정 권한이 없습니다.");
                 return "redirect:/member/my-reviews";
            
            } catch (Exception e) {
                log.error("리뷰 수정 유효성 검사 실패 후 추가 정보 로딩 중 오류", e);
                ra.addFlashAttribute("error", "수정 폼을 다시 로드하는 중 오류가 발생했습니다.");
                return "redirect:/member/my-reviews";
            }
        }

        try {
            reviewService.updateReview(command, reviewNum, imagesToDelete);
            ra.addFlashAttribute("message", "리뷰가 성공적으로 수정되었습니다.");
        } catch (SecurityException e) {
             log.warn("리뷰 수정 권한 오류: reviewNum={}", reviewNum, e);
             ra.addFlashAttribute("error", e.getMessage());
        
        } catch (Exception e) {
             log.error("리뷰 수정 중 오류 발생: reviewNum={}", reviewNum, e);
             ra.addFlashAttribute("error", "리뷰 수정 중 오류가 발생했습니다.");
        }

        return "redirect:/member/my-reviews";
    }
}