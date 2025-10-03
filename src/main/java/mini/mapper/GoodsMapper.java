package mini.mapper;

import mini.command.GoodsFilterCommand;
import mini.domain.GoodsDTO;
import mini.domain.GoodsIpgoDTO;
import mini.domain.GoodsStockDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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
    public void goodsDelete(List<String> nums);
    GoodsStockDTO selectOneWithStock(String goodsNum);
    public List<GoodsDTO> selectGoodsByNumList(List<String> nums);
    // [수정] 재고 변경 사유(memo)를 함께 저장하도록 파라미터 추가
    void insertGoodsIpgo(@Param("goodsNum") String goodsNum, @Param("ipgoQty") int ipgoQty, @Param("memo") String memo);
 // [추가] 재고 변경 이력 개수 카운트
    int countIpgoHistory(String goodsNum);
    // [추가] 재고 변경 이력을 페이징하여 조회
    List<GoodsIpgoDTO> selectIpgoHistoryPaged(java.util.Map<String, Object> params);
}