package lappick.cart.dto;

import org.apache.ibatis.type.Alias;
import lappick.cart.domain.Cart;
import lappick.goods.dto.GoodsResponse;
import lombok.Data;

@Data
@Alias("CartItemResponse")
public class CartItemResponse {
	GoodsResponse goods;
	Cart cart;
}