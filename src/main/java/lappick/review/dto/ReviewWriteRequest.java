package lappick.review.dto;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewWriteRequest {
    private String purchaseNum;
    private String goodsNum;

    @NotNull(message = "별점을 선택해주세요.")
    @Min(value = 1, message = "별점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "별점은 5점 이하이어야 합니다.")
    private Integer reviewRating;

    @NotBlank(message = "리뷰 내용을 입력해주세요.")
    private String reviewContent;

    private MultipartFile[] reviewImages;
}