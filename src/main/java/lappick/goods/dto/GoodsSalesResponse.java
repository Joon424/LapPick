package lappick.goods.dto;

import lombok.Data;

@Data
public class GoodsSalesResponse {
    // 상품 기본 정보
    private String goodsNum;
    private String goodsName;

    // 판매 현황 정보
    private int totalSoldQty;
    private long totalSalesAmount;
    private int reviewCount;
    private double avgRating;
}