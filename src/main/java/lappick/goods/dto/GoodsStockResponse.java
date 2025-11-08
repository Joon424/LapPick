package lappick.goods.dto;

import org.apache.ibatis.type.Alias;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Alias("goodsStock")
public class GoodsStockResponse extends GoodsResponse {
    Integer stockQty;
}