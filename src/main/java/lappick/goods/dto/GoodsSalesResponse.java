package lappick.goods.dto;

import lombok.Data;

@Data
public class GoodsSalesResponse {
    // 상품 기본 정보
    private String goodsNum;
    private String goodsName;

    // 판매 현황 정보
    private int totalSoldQty;    // 총 판매량
    private long totalSalesAmount; // 총 판매액
    private int reviewCount;     // 리뷰 개수
    private double avgRating;        // 평균 평점
}