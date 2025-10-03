package mini.controller;

import java.security.Principal; // [추가]
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mini.domain.GoodsStockDTO;
import mini.domain.MemberDTO; // [추가]
import mini.domain.QnaDTO;
import mini.mapper.MemberMapper; // [추가]
import mini.mapper.QnaMapper;
import mini.service.item.GoodsDetailViewService;
import mini.service.review.ReviewService;

@Controller
@RequestMapping("corner")
@RequiredArgsConstructor
public class CornerController {

    private final GoodsDetailViewService goodsDetailViewService;
    private final QnaMapper qnaMapper;
    private final MemberMapper memberMapper;
    private final ReviewService reviewService;

    @GetMapping("detailView/{goodsNum}")
    public String goodsInfo(
            @PathVariable("goodsNum") String goodsNum, Model model,
            HttpServletRequest request, HttpServletResponse response,
            Principal principal) {
        
        // --- 기존 로직 (동일) ---
        GoodsStockDTO dto = goodsDetailViewService.execute(goodsNum, request, response);
        List<QnaDTO> qnaList = qnaMapper.selectQnaByGoodsNum(goodsNum);
        
        String loginMemberNum = null;
        boolean isEmployee = false;
        
        if (principal != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            MemberDTO member = memberMapper.selectOneById(auth.getName());
            if (member != null) {
                loginMemberNum = member.getMemberNum();
            }
            isEmployee = auth.getAuthorities().stream()
                             .anyMatch(a -> a.getAuthority().equals("ROLE_EMP"));
        }
        
        // --- [추가] 리뷰 정보 조회 로직 ---
        Map<String, Object> reviewData = reviewService.getReviewsForProduct(goodsNum);
        
        // --- 모델에 데이터 추가 ---
        model.addAttribute("loginMemberNum", loginMemberNum);
        model.addAttribute("isEmployee", isEmployee);
        model.addAttribute("qnaList", qnaList);
        model.addAttribute("goods", dto);
        model.addAttribute("reviewList", reviewData.get("list")); // [추가]
        model.addAttribute("reviewSummary", reviewData.get("summary")); // [추가]
        
        return "thymeleaf/item/detailView";
    }
}
