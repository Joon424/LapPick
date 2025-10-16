package lappick.domain;

import lombok.Data;
import java.util.Date;

import lappick.goods.dto.GoodsResponse;
import lappick.member.dto.MemberResponse;

@Data
public class QnaDTO {
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

    // JOIN해서 가져올 정보
    private GoodsResponse goodsDTO;
    private MemberResponse memberDTO;
}