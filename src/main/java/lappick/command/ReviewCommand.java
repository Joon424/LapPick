package lappick.command;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewCommand {
    private String purchaseNum;
    private String goodsNum;
    
    @NotNull(message = "별점을 선택해주세요.")
    @Min(value = 1, message = "별점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "별점은 5점 이하이어야 합니다.")
    private Integer reviewRating;
    
    @NotEmpty(message = "리뷰 내용을 입력해주세요.")
    private String reviewContent;
    
    // [수정] 단일 파일(MultipartFile)에서 여러 파일(배열)을 받을 수 있도록 변경
    private MultipartFile[] reviewImages;
}