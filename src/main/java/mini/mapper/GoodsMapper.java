package mini.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import mini.domain.GoodsDTO;
import mini.domain.StartEndPageDTO;

@Mapper
public interface GoodsMapper {
    int goodsInsert(GoodsDTO dto);
    List<GoodsDTO> allSelect(StartEndPageDTO sepDTO);
    int goodsCount(@Param("searchWord") String searchWord);// @Param 추가
    int productsDelete(@Param("products") String[] products);
    GoodsDTO selectOne(String goodsNum);
    int goodsUpdate(GoodsDTO dto);
    int goodsDelete(String goodsNum);
    List<GoodsDTO> goodsSelectList(@Param("startRow") int startRow, @Param("endRow") int endRow);
}



