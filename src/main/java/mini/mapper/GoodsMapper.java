package mini.mapper;

import mini.command.GoodsFilterCommand;
import mini.domain.GoodsDTO;
import mini.domain.GoodsStockDTO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map; // Map import 추가

@Repository
@Mapper
public interface GoodsMapper {
    
    // 상품(Goods) 관련 메서드
    List<GoodsDTO> allSelect(GoodsFilterCommand filter);
    int goodsCount(GoodsFilterCommand filter);
    GoodsDTO selectOne(String goodsNum);
    void goodsInsert(GoodsDTO dto);
    void goodsUpdate(GoodsDTO dto);
    void goodsDelete(String goodsNum);
    GoodsStockDTO selectOneWithStock(String goodsNum);
    void visitCountUpdate(String goodsNum);
    
    // ▼▼▼▼▼ [추가] 위시리스트(Wishlist) 관련 메서드들 ▼▼▼▼▼
    
    /**
     * 특정 상품이 위시리스트에 있는지 확인합니다.
     * @param map (goodsNum, memberNum)
     * @return count (있으면 1, 없으면 0)
     */
    Integer wishCountSelectOne(Map<String, String> map);

    /**
     * 위시리스트에 상품을 추가합니다.
     * @param map (goodsNum, memberNum)
     */
    void wishInsert(Map<String, String> map);

    /**
     * 위시리스트에서 상품을 제거합니다.
     * @param map (goodsNum, memberNum)
     */
    void wishDelete(Map<String, String> map);
    
    /**
     * 특정 회원의 위시리스트 전체 목록을 조회합니다.
     * @param memberNum 회원 번호
     * @return GoodsDTO 리스트
     */
    List<GoodsDTO> wishSelectList(String memberNum);
}