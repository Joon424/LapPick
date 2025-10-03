package mini.domain;

import java.util.Date;

import lombok.Data;

@Data
public class PurchaseListDTO {
    // ⚠️ 중요: PURCHASE_LIST 테이블의 PK. 없다면 추가를 강력히 권장합니다.
    private Integer purchaseListNum;
    
    private String purchaseNum;
    private String goodsNum;
    private Integer purchaseQty;
    private Integer purchasePrice;

    // JOIN해서 가져올 정보
    private GoodsDTO goodsDTO;
    private Date purchaseDate; // [추가] 구매일 필드
    
 // [추가] 리뷰 작성 여부를 확인하기 위한 필드
    private boolean reviewWritten; 
}