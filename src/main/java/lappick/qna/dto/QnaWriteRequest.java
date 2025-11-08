package lappick.qna.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QnaWriteRequest {
    // 상품 상세 페이지에서 직접 문의 시 사용
    private String goodsNum;

    // '나의 문의' 페이지에서 문의 시 사용 (purchaseNum-goodsNum 조합 키)
    private String purchaseItemKey;

    @NotBlank(message = "문의 유형을 선택해주세요.")
    private String qnaType;

    @NotBlank(message = "제목을 입력해주세요.")
    private String qnaTitle;

    @NotBlank(message = "내용을 입력해주세요.")
    private String qnaContent;
}