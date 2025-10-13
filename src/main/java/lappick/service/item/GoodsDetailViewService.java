// mini/service/item/GoodsDetailViewService.java

package lappick.service.item;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lappick.domain.GoodsStockDTO;
import lappick.mapper.GoodsMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoodsDetailViewService {

    private final GoodsMapper goodsMapper;

    // [수정] Model을 직접 다루는 대신, 조회한 GoodsStockDTO를 반환하도록 변경
    public GoodsStockDTO execute(String goodsNum, HttpServletRequest request, HttpServletResponse response) {
        // 조회수 증가 로직 (쿠키 기반)
        Cookie goodsCookie = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals("viewGoods")) {
                    goodsCookie = c;
                }
            }
        }

        boolean shouldIncreaseViewCount = false;
        if (goodsCookie != null) {
            if (!goodsCookie.getValue().contains("[" + goodsNum + "]")) {
                goodsCookie.setValue(goodsCookie.getValue() + "_[" + goodsNum + "]");
                shouldIncreaseViewCount = true;
            }
        } else {
            goodsCookie = new Cookie("viewGoods", "[" + goodsNum + "]");
            shouldIncreaseViewCount = true;
        }

        if (shouldIncreaseViewCount) {
            goodsCookie.setPath("/");
            goodsCookie.setMaxAge(60 * 60 * 24 * 30); // 30일
            response.addCookie(goodsCookie);
        }

        // DB에서 모든 상품 정보와 재고를 조회하여 반환
        return goodsMapper.selectOneWithStock(goodsNum); 
    }
}