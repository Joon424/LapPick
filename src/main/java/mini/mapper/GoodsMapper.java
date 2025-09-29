package mini.mapper;

import mini.command.GoodsFilterCommand;
import mini.domain.GoodsDTO;
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
 // [추가]
    void insertGoodsIpgo(@Param("goodsNum") String goodsNum, @Param("ipgoQty") int ipgoQty);
}