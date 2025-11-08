package lappick.review.domain;

import java.util.Date;
import org.apache.ibatis.type.Alias;
import lombok.Data;

@Data
@Alias("Review")
public class Review {
    // reviewNum은 DB 시퀀스(BIGINT)와 매핑되므로 Long 타입을 사용합니다.
    private Long reviewNum;
    private String goodsNum;
    private String memberNum;
    private String purchaseNum;
    private Integer reviewRating;
    private String reviewContent;
    // 다중 이미지 파일명을 '/' 구분자로 결합하여 저장합니다.
    private String reviewImage;
    private Date reviewDate;
    private Date reviewModDate;

    // JOIN 쿼리 결과 매핑용 필드
    private String memberId;
    private String goodsName;
    private String reviewStatus;
}