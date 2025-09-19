package mini.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import mini.command.GoodsFilterDTO;
import mini.domain.GoodsDTO;
import mini.domain.StartEndPageDTO;

@Mapper
public interface GoodsMapper {
    int goodsInsert(GoodsDTO dto);
 // 💥 [수정] StartEndPageDTO 대신 GoodsFilterDTO를 사용
    List<GoodsDTO> allSelect(GoodsFilterDTO filter);
    // 💥 [수정] String 대신 GoodsFilterDTO를 사용
    int goodsCount(GoodsFilterDTO filter);
    int productsDelete(@Param("products") String[] products);
    GoodsDTO selectOne(String goodsNum);
    int goodsUpdate(GoodsDTO dto);
    int goodsDelete(String goodsNum);
    List<GoodsDTO> goodsSelectList(@Param("startRow") int startRow, @Param("endRow") int endRow);
}



