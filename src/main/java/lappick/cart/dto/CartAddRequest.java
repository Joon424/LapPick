package lappick.cart.dto;

import lombok.Data;

@Data
public class CartAddRequest {
	String goodsNum;
	Integer qty;
}