package lappick.goods.dto;

import java.util.Date;
import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("GoodsResponse")
public class GoodsResponse {
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
    
    private Integer stockQty;
    
    private Integer reviewCount;
    private Double avgRating;
}