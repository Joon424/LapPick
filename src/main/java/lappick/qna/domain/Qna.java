package lappick.qna.domain;

import lombok.Data;
import java.util.Date;
import org.apache.ibatis.type.Alias;

@Data
@Alias("Qna")
public class Qna {
    private Integer qnaNum;
    private String memberNum;
    private String goodsNum;
    private String qnaType;
    private String qnaTitle;
    private String qnaContent;
    private Date qnaDate;
    private String answerContent;
    private Date answerDate;
    private String qnaStatus;
}