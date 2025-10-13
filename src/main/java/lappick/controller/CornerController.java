package lappick.controller;

import java.security.Principal;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // ⭐️ 이 import 문이 필요합니다.

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lappick.domain.GoodsStockDTO;
import lappick.domain.MemberDTO;
import lappick.domain.QnaDTO;
import lappick.domain.ReviewPageDTO;
import lappick.domain.ReviewSummaryDTO;
import lappick.mapper.MemberMapper;
import lappick.mapper.QnaMapper;
import lappick.service.item.GoodsDetailViewService;
import lappick.service.review.ReviewService;
import lombok.RequiredArgsConstructor;

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
            @PathVariable("goodsNum") String goodsNum,
            @RequestParam(name = "reviewPage", defaultValue = "1") int reviewPage,
            Model model,
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

        // --- [수정] 리뷰 정보 조회 로직 ---
        // 한 페이지에 5개의 리뷰를 표시
        ReviewPageDTO reviewPageData = reviewService.findReviewsByGoodsNum(goodsNum, reviewPage, 5);
        ReviewSummaryDTO reviewSummary = reviewService.getReviewSummary(goodsNum);

        // --- 모델에 데이터 추가 ---
        model.addAttribute("loginMemberNum", loginMemberNum);
        model.addAttribute("isEmployee", isEmployee);
        model.addAttribute("qnaList", qnaList);
        model.addAttribute("goods", dto);
        model.addAttribute("reviewPageData", reviewPageData);
        model.addAttribute("reviewSummary", reviewSummary);

        return "thymeleaf/item/detailView";
    }
}