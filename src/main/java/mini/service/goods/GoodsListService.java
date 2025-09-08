package mini.service.goods;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import mini.domain.GoodsDTO;
import mini.domain.StartEndPageDTO;
import mini.mapper.GoodsMapper;
import mini.service.StartEndPageService;

@Service
public class GoodsListService {
    @Autowired
    GoodsMapper goodsMapper;

    @Autowired
    StartEndPageService startEndPageService;

    // 상품 목록을 불러오는 서비스
    public void execute(String searchWord, Model model, int page) {
        // 한 페이지에 보일 상품 수 (페이지당 10개씩)
        int limit = 10;  
        // 페이징 정보 생성
        StartEndPageDTO sepDTO = startEndPageService.execute(page, limit, searchWord);

        // 상품 목록 조회
        List<GoodsDTO> list = goodsMapper.allSelect(sepDTO);
        // 전체 상품 수 조회
        int count = goodsMapper.goodsCount(searchWord);

        // 페이징 정보를 모델에 담기
        startEndPageService.execute(page, limit, count, searchWord, list, model);

        // 상품 목록을 모델에 담기
        model.addAttribute("goodsList", list);
    }
}
