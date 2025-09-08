package mini.command;

import lombok.Data;

@Data
public class CartCommand {
	String goodsNum;
	Integer qty;
}