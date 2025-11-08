package lappick.qna.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QnaAnswerRequest {
    @NotNull(message = "답변할 문의 번호가 필요합니다.")
    private Integer qnaNum;

    @NotBlank(message = "답변 내용을 입력해주세요.")
    private String answerContent;

    private String returnUrl;
}