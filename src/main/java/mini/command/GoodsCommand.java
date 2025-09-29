package mini.command;

import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

//command의 멤버 필드는 html의 input의 이름과 같게 만들어줍니다.
// 유효성 검사를 하기 위해 validated을 부여합니다. 
@Data
public class GoodsCommand {
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
	MultipartFile[] goodsDetailImage; // 배열 타입 유지
	MultipartFile[] goodsDetail; // 상세 설명용 이미지
	
    @NotEmpty(message = "브랜드를 선택해주세요.")
    String goodsBrand; // 변경 없음

    @NotEmpty(message = "용도를 선택해주세요.")
    String goodsPurpose; // 변경 없음

    @NotNull(message = "화면 크기를 입력해주세요.")
    Double goodsScreenSize; // 'screenSize' -> 'goodsScreenSize'

    @NotNull(message = "무게를 입력해주세요.")
    Double goodsWeight; // 'weight' -> 'goodsWeight'
    
    String goodsKeyword1;
	String goodsKeyword2;
	String goodsKeyword3;
	String goodsShippingInfo;
	String goodsSellerInfo;
	
	// [추가]
	private Integer initialStock;
}










