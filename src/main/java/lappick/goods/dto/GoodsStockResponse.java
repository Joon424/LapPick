package lappick.goods.dto;

import org.apache.ibatis.type.Alias;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true) // 상속받은 클래스의 필드도 equals/hashCode에 포함
@Alias("goodsStock")
public class GoodsStockResponse extends GoodsResponse { // [수정] GoodsDTO를 상속받습니다.
    Integer stockQty;
}