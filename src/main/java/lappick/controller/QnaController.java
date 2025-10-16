package lappick.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lappick.command.PageData;

import lappick.domain.PurchaseListDTO;
import lappick.domain.QnaDTO;
import lappick.member.mapper.MemberMapper;
import lappick.service.purchase.PurchaseService;
import lappick.service.qna.QnaService;
import lappick.member.dto.MemberResponse;


import java.util.List;

@Controller
@RequestMapping("/qna")
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;
    private final PurchaseService purchaseService;
    private final MemberMapper memberMapper;

 // [수정] status 파라미터를 받아 서비스로 전달하도록 변경
    @GetMapping("/my-qna")
    public String myQnaList(Authentication auth, Model model,
                        @RequestParam(value = "searchWord", required = false) String searchWord,
                        @RequestParam(value = "status", required = false) String status, // [추가]
                        @RequestParam(value = "page", defaultValue = "1") int page) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String memberId = userDetails.getUsername();
        int size = 5;

        // [수정] 서비스 호출 시 status 전달
        PageData<QnaDTO> pageData = qnaService.getMyQnaList(memberId, searchWord, status, page, size);

        MemberResponse member = memberMapper.selectOneById(memberId);
        String memberNum = member.getMemberNum();
        List<PurchaseListDTO> purchaseList = purchaseService.getPurchasedItems(memberNum);

        model.addAttribute("pageData", pageData);
        model.addAttribute("myQnaList", pageData.getItems());
        model.addAttribute("purchaseList", purchaseList);
        model.addAttribute("status", status); // [추가] 뷰에서 현재 필터 상태를 알 수 있도록 전달

        return "thymeleaf/qna/myQnaList";
    }
    
    // 문의 등록 처리
    @PostMapping("/write")
    public String writeQna(QnaDTO dto, @RequestParam("purchaseItemKey") String purchaseItemKey, Authentication auth) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        // [수정] 마지막 하이픈(-)을 기준으로 purchaseNum과 goodsNum을 안전하게 분리
        int lastHyphenIndex = purchaseItemKey.lastIndexOf('-');
        if (lastHyphenIndex == -1) {
            // 비정상적인 키 값이 들어왔을 경우의 예외 처리
            // 여기서는 간단하게 리다이렉트 처리합니다.
            return "redirect:/qna/my-qna?error=invalidKey";
        }
        
        String purchaseNum = purchaseItemKey.substring(0, lastHyphenIndex);
        String goodsNum = purchaseItemKey.substring(lastHyphenIndex + 1);

        qnaService.writeQna(dto, purchaseNum, goodsNum, userDetails.getUsername());
        
        return "redirect:/qna/my-qna";
    }
    
    
    /**
     * [추가] 상품 상세 페이지의 문의하기 팝업에서 오는 요청을 처리합니다.
     */
    @PostMapping("/write/product")
    public String writeQnaFromProduct(QnaDTO dto, @RequestParam("goodsNum") String goodsNum, Authentication auth) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        qnaService.writeQnaFromProductPage(dto, goodsNum, userDetails.getUsername());
        
        // 문의 작성 후, 원래 있던 상품 상세 페이지로 돌아갑니다.
        return "redirect:/corner/detailView/" + goodsNum;
    }
    
}