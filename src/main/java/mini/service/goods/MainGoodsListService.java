package mini.service.goods;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import mini.domain.GoodsDTO;
import mini.mapper.GoodsMapper;

@Service
public class MainGoodsListService {
    @Autowired
    GoodsMapper goodsMapper;

    public void execute(Integer page, Model model) {
        int limit = 6; // 1 ~ 6
        int startRow = ((page - 1) * limit) + 1; // 1
        int endRow = startRow + limit - 1; // 6

        // 상품 목록 조회
        List<GoodsDTO> list = goodsMapper.goodsSelectList(startRow, endRow);
        
        // 상품 개수 조회 (searchWord에 빈 문자열 전달)
        int count = goodsMapper.goodsCount(""); // 빈 문자열 전달

        // 최대 페이지 수 계산
        int maxPage = (int)((double)count / limit + 0.95);
        
        // 모델에 데이터 추가
        model.addAttribute("maxPage", maxPage);
        model.addAttribute("list", list);
    }
}
