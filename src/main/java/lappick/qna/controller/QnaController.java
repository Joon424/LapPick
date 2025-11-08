package lappick.qna.controller;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lappick.common.dto.PageData;
import lappick.purchase.dto.PurchaseItemResponse;
import lappick.qna.dto.QnaResponse;
import lappick.qna.dto.QnaWriteRequest;
import lappick.qna.service.QnaService;

import java.util.List;

@Controller
@RequestMapping("/qna")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_MEMBER')")
public class QnaController {

    private static final Logger log = LoggerFactory.getLogger(QnaController.class);

    private final QnaService qnaService;

    /**
     * 나의 Q&A 목록 페이지
     */
    @GetMapping("/my-qna")
    public String myQnaList(Authentication auth, Model model,
                        @RequestParam(value = "searchWord", required = false) String searchWord,
                        @RequestParam(value = "status", required = false) String status,
                        @RequestParam(value = "page", defaultValue = "1") int page) {
        try {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String memberId = userDetails.getUsername();
            int size = 5; 

            PageData<QnaResponse> pageData = qnaService.getMyQnaListPage(memberId, searchWord, status, page, size);
            // 구매 상품 목록 조회 (모달 폼 채우기용)
            List<PurchaseItemResponse> purchaseList = qnaService.getPurchasedItemsForQna(memberId);

            model.addAttribute("pageData", pageData);
            model.addAttribute("myQnaList", pageData.getItems());
            model.addAttribute("purchaseList", purchaseList);
            model.addAttribute("status", status);

            model.addAttribute("qnaWriteRequest", new QnaWriteRequest());

            return "user/member/my-qna";

        } catch (Exception e) {
            log.error("나의 QnA 목록 조회 중 오류 발생", e);
            model.addAttribute("error", "문의 목록을 불러오는 중 오류가 발생했습니다.");
            // 오류 발생 시 보여줄 페이지
            return "redirect:/";
        }
    }

    /**
     * '나의 문의' 페이지에서 문의 등록 처리
     */
    @PostMapping("/write")
    public String writeQnaFromMyPage(@Validated @ModelAttribute("qnaWriteRequest") QnaWriteRequest request, BindingResult bindingResult,
                                     Authentication auth, RedirectAttributes ra) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String memberId = userDetails.getUsername();

        if (bindingResult.hasErrors()) {
            log.warn("나의 QnA 작성 유효성 검사 실패: {}", bindingResult.getAllErrors());
            ra.addFlashAttribute("qnaWriteRequest", request);
            ra.addFlashAttribute("validationError", true);
            
            if (bindingResult.hasErrors()) {
                ra.addFlashAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
            }
            
            return "redirect:/qna/my-qna";
        }

        try {
            qnaService.writeQnaFromMyPage(request, memberId);
            ra.addFlashAttribute("message", "문의가 성공적으로 등록되었습니다.");
            return "redirect:/qna/my-qna";
        } catch (IllegalArgumentException | SecurityException e) {
            log.warn("나의 QnA 작성 실패: {}", e.getMessage());
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/qna/my-qna";
        } catch (Exception e) {
            log.error("나의 QnA 작성 중 예상치 못한 오류 발생", e);
            ra.addFlashAttribute("error", "문의 등록 중 오류가 발생했습니다.");
            return "redirect:/qna/my-qna";
        }
    }


    /**
     * 상품 상세 페이지에서 문의 등록 처리
     */
    @PostMapping("/write/product")
    public String writeQnaFromProduct(@Validated @ModelAttribute QnaWriteRequest request, BindingResult bindingResult,
                                      Authentication auth, RedirectAttributes ra, Model model) {
        String goodsNum = request.getGoodsNum();

        if (bindingResult.hasErrors()) {
            log.warn("상품 상세 QnA 작성 유효성 검사 실패: {}", bindingResult.getAllErrors());
            // 상세 페이지를 다시 로드하려면 goods 정보 등이 필요하므로, 에러 메시지만 전달하고 리다이렉트
            ra.addFlashAttribute("qnaValidationError", bindingResult.getFieldError().getDefaultMessage());
            ra.addFlashAttribute("qnaWriteRequest", request);
            return "redirect:/goods/detail/" + goodsNum + "?openQnaModal=true"; // 모달 바로 열리도록 파라미터 전달
        }

        try {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            qnaService.writeQnaFromProductPage(request, userDetails.getUsername());
            ra.addFlashAttribute("message", "문의가 성공적으로 등록되었습니다.");
            // 문의 작성 후, 원래 있던 상품 상세 페이지로 돌아갑니다.
            return "redirect:/goods/detail/" + goodsNum;
        } catch (SecurityException e) {
             log.warn("상품 상세 QnA 작성 권한 오류: {}", e.getMessage());
             ra.addFlashAttribute("error", e.getMessage());
             return "redirect:/auth/login";
        } catch (Exception e) {
            log.error("상품 상세 QnA 작성 중 오류 발생: goodsNum={}", goodsNum, e);
            ra.addFlashAttribute("error", "문의 등록 중 오류가 발생했습니다.");
            return "redirect:/goods/detail/" + goodsNum;
        }
    }
}