package lappick.domain;

import java.util.Date;
import lombok.Data;

@Data
public class ReviewDTO {
    private Long reviewNum;
    private String goodsNum;
    private String memberNum;
    private String purchaseNum;
    private Integer reviewRating;
    private String reviewContent;
    private String reviewImage;
    private Date reviewDate;

    // JOIN해서 가져올 추가 정보
    private String memberId;
    private String goodsName;
   
    private String reviewStatus; // 이 필드를 추가해주세요.
}