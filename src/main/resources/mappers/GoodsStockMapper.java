package mini.mapper;

import org.apache.ibatis.annotations.Mapper;

import mini.domain.GoodsStockDTO;

@Mapper
public interface GoodsStockMapper {
	public GoodsStockDTO goodsStockSelectOne(String goodsNum);
	public int goodsVisitCountUpdate(String goodsNum);
}