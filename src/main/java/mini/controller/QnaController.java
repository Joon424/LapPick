package mini.controller;

import lombok.RequiredArgsConstructor;
import mini.domain.MemberDTO;
import mini.domain.PurchaseListDTO;
import mini.domain.QnaDTO;
import mini.mapper.MemberMapper;
import mini.mapper.QnaMapper;
import mini.service.purchase.PurchaseService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/qna")
@RequiredArgsConstructor
public class QnaController {

    private final QnaMapper qnaMapper;
    private final MemberMapper memberMapper;
    private final PurchaseService purchaseService;

    // 나의 상품 문의 목록 페이지
    @GetMapping("/my-qna")
    public String myQnaList(Authentication auth, Model model) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        MemberDTO member = memberMapper.selectOneById(userDetails.getUsername());

        // 1. 나의 문의 내역 조회
        List<QnaDTO> myQnaList = qnaMapper.selectQnaByMemberNum(member.getMemberNum());
        
        // 2. 문의할 상품 목록 조회를 위해, 내가 구매했던 상품 목록 조회
        List<PurchaseListDTO> purchaseList = purchaseService.getPurchasedItems(member.getMemberNum());

        model.addAttribute("myQnaList", myQnaList);
        model.addAttribute("purchaseList", purchaseList);

        return "thymeleaf/qna/myQnaList";
    }
    
    // 문의 작성 페이지로 이동 (상품 상세페이지에서 넘어올 경우)
    @GetMapping("/write")
    public String writeQnaForm(@RequestParam(value="goodsNum", required = false) String goodsNum, Model model) {
        model.addAttribute("selectedGoodsNum", goodsNum);
        // 이 페이지는 my-qna 페이지와 동일한 뷰를 사용하므로, my-qna로 리다이렉트 하거나 동일 데이터를 로드해야 합니다.
        // 여기서는 my-qna로 포워딩하는 대신, my-qna 뷰에서 처리하도록 유도하겠습니다.
        return "redirect:/qna/my-qna" + (goodsNum != null ? "?goodsNum=" + goodsNum : "");
    }
    
    // 문의 등록 처리
    @PostMapping("/write")
    public String writeQna(QnaDTO dto, Authentication auth) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        MemberDTO member = memberMapper.selectOneById(userDetails.getUsername());
        
        dto.setMemberNum(member.getMemberNum());
        qnaMapper.insertQna(dto);
        
        return "redirect:/qna/my-qna";
    }
}