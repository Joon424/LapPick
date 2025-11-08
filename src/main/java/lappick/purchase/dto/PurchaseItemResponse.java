package lappick.purchase.dto;

import java.util.Date;
import lappick.goods.dto.GoodsResponse;
import lombok.Data;

@Data
public class PurchaseItemResponse {
    private Integer purchaseListNum;
    private String purchaseNum;
    private String goodsNum;
    private Integer purchaseQty;
    private Integer purchasePrice;
    private boolean reviewWritten;
    private GoodsResponse goods;
    private Date purchaseDate;
}