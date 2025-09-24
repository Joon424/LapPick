package mini.domain;

import lombok.Data;

@Data
public class PurchaseListDTO {
    String purchaseNum;
    String goodsNum;
    Integer purchaseQty;
    Integer purchasePrice;

    // JOIN해서 가져올 상품 정보
    GoodsDTO goodsDTO;
}