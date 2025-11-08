package lappick.qna.dto;

import java.time.LocalDateTime;
import org.apache.ibatis.type.Alias;
import lombok.Data;

@Data
@Alias("QnaResponse")
public class QnaResponse {
    private Integer qnaNum;
    private String memberNum;
    private String goodsNum;
    private String qnaType;
    private String qnaTitle;
    private String qnaContent;
    private LocalDateTime qnaDate;
    private String answerContent;
    private LocalDateTime answerDate;
    private String qnaStatus;

    // JOIN된 정보
    private String goodsName;
    private String memberId;
}