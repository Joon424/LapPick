package lappick.goods.dto;

import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GoodsRequest {
	String goodsNum;
	@NotEmpty(message = "이름을 입력해주세요")
	String goodsName;
	@NotNull(message = "가격을 입력해주세요.")
	Integer goodsPrice;
	@NotEmpty(message = "설명을 입력해주세요")
	String goodsContents;
	String empNum;
	Date goodsRegist;
	String updateEmpNum;
	Date goodsUpdateDate;
	MultipartFile goodsMainImage;
	MultipartFile[] goodsDetailImage;
	MultipartFile[] goodsDetail;
	
    @NotEmpty(message = "브랜드를 선택해주세요.")
    String goodsBrand;

    @NotEmpty(message = "용도를 선택해주세요.")
    String goodsPurpose;

    @NotNull(message = "화면 크기를 입력해주세요.")
    Integer goodsScreenSize;

    @NotNull(message = "무게를 입력해주세요.")
    Integer goodsWeight;
    
    String goodsKeyword1;
	String goodsKeyword2;
	String goodsKeyword3;
	String goodsShippingInfo;
	@NotEmpty(message = "판매자 정보를 입력해주세요.")
	String goodsSellerInfo;
	
	private Integer initialStock;
}