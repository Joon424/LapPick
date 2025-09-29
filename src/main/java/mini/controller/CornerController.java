package mini.controller;

import java.security.Principal; // [추가]
import java.util.List;

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

@Controller
@RequestMapping("corner")
@RequiredArgsConstructor
public class CornerController {

    private final GoodsDetailViewService goodsDetailViewService;
    private final QnaMapper qnaMapper;
    private final MemberMapper memberMapper; // [추가] MemberMapper 의존성 주입

 // [교체] goodsInfo 메소드
    @GetMapping("detailView/{goodsNum}")
    public String goodsInfo(
            @PathVariable("goodsNum") String goodsNum, Model model,
            HttpServletRequest request, HttpServletResponse response,
            Principal principal) {
        
        GoodsStockDTO dto = goodsDetailViewService.execute(goodsNum, request, response);
        List<QnaDTO> qnaList = qnaMapper.selectQnaByGoodsNum(goodsNum);
        
        String loginMemberNum = null;
        boolean isEmployee = false; // 직원 여부 플래그
        
        if (principal != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            MemberDTO member = memberMapper.selectOneById(auth.getName());
            if (member != null) {
                loginMemberNum = member.getMemberNum();
            }
            // [추가] 현재 사용자가 'ROLE_EMP' 권한을 가지고 있는지 확인
            isEmployee = auth.getAuthorities().stream()
                             .anyMatch(a -> a.getAuthority().equals("ROLE_EMP"));
        }
        
        model.addAttribute("loginMemberNum", loginMemberNum);
        model.addAttribute("isEmployee", isEmployee); // [추가]
        model.addAttribute("qnaList", qnaList);
        model.addAttribute("goods", dto);
        
        return "thymeleaf/item/detailView";
    }
}
