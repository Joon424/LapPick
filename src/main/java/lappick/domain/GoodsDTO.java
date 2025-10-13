package lappick.domain;

import java.util.Date;
import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("goods")
public class GoodsDTO {
    String goodsNum;
    String goodsName;
    Integer goodsPrice;
    String goodsContents;
    String empNum;
    Date goodsDate;
    
    String goodsMainImage;
    String goodsMainStoreImage;
    String goodsDetailImage;
    String goodsDetailStoreImage;
    
    String goodsBrand;
    String goodsPurpose;
    Double goodsScreenSize;
    
    // ▼▼▼▼▼ [수정] String -> Double로 타입 변경 ▼▼▼▼▼
    Double goodsWeight;
    
    String goodsDetail;
    String goodsDetailStore;
    String goodsKeyword1;
    String goodsKeyword2;
    String goodsKeyword3;
    String goodsShippingInfo;
    String goodsSellerInfo;
    String updateEmpNum;
    Date updateDate;
    
    // [추가] 현재 재고 수량을 담을 필드
    private Integer stockQty;
    
 // [추가] 리뷰 요약 정보를 담을 필드
    private Integer reviewCount;
    private Double avgRating;
}