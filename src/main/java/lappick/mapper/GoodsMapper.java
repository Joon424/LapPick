package lappick.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import lappick.command.GoodsFilterCommand;
import lappick.domain.GoodsDTO;
import lappick.domain.GoodsIpgoDTO;
import lappick.domain.GoodsSalesDTO;
import lappick.domain.GoodsStockDTO;

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
 // [추가] 리뷰 필터링 용도로 모든 상품의 번호와 이름을 조회
    List<GoodsDTO> selectAllForFilter();
 // [추가] 메인 페이지 베스트 상품 6개 조회
    List<GoodsDTO> selectBestGoodsList();
 // [추가] 판매 현황 전체 개수 조회
    int countGoodsSalesStatus(Map<String, Object> params);
    // [추가] 판매 현황 페이징 조회
    List<GoodsSalesDTO> findGoodsSalesStatusPaginated(Map<String, Object> params);
}