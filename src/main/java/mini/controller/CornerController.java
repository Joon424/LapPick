package mini.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mini.domain.GoodsStockDTO;
import mini.domain.QnaDTO;
import mini.mapper.QnaMapper;
import mini.service.AutoNumService;
import mini.service.item.GoodsDetailViewService;

@Controller
@RequestMapping("corner")
@RequiredArgsConstructor // @Autowired 대신 생성자 주입 방식 사용
public class CornerController {

    private final GoodsDetailViewService goodsDetailViewService;
    private final QnaMapper qnaMapper;

    @GetMapping("detailView/{goodsNum}")
    public String goodsInfo(
            @PathVariable("goodsNum") String goodsNum, Model model,
            HttpServletRequest request, HttpServletResponse response) {
        
        // 1. 서비스에서 모든 정보가 담긴 DTO를 받음
        GoodsStockDTO dto = goodsDetailViewService.execute(goodsNum, request, response);
        List<QnaDTO> qnaList = qnaMapper.selectQnaByGoodsNum(goodsNum);
        model.addAttribute("qnaList", qnaList);
        // 2. 컨트롤러가 직접 모델에 'goods'라는 이름으로 DTO를 추가
        model.addAttribute("goods", dto);
        
        
        
        return "thymeleaf/item/detailView";
    }
}
	
