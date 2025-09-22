package mini.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mini.domain.GoodsStockDTO;
import mini.service.item.GoodsDetailViewService;

@Controller
@RequestMapping("corner")
@RequiredArgsConstructor // @Autowired 대신 생성자 주입 방식 사용
public class CornerController {

    private final GoodsDetailViewService goodsDetailViewService;

    @GetMapping("detailView/{goodsNum}")
    public String goodsInfo(
            @PathVariable("goodsNum") String goodsNum, Model model,
            HttpServletRequest request, HttpServletResponse response) {
        
        // 1. 서비스에서 모든 정보가 담긴 DTO를 받음
        GoodsStockDTO dto = goodsDetailViewService.execute(goodsNum, request, response);
        
        // 2. 컨트롤러가 직접 모델에 'goods'라는 이름으로 DTO를 추가
        model.addAttribute("goods", dto);
        
        return "thymeleaf/item/detailView";
    }
    
    // [삭제] goodsInfo 메서드가 모든 정보를 전달하므로, 
    //        상세 설명을 별도로 조회하던 goodsDescript 메서드는 더 이상 필요 없어 삭제합니다.
}
	
