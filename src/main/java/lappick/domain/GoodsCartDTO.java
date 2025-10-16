package lappick.domain;

import org.apache.ibatis.type.Alias;

import lappick.goods.dto.GoodsResponse;
import lombok.Data;

@Data
@Alias("cartGoods")
public class GoodsCartDTO {
	GoodsResponse goodsDTO;     // 1
	CartDTO cartDTO;       // 1
}