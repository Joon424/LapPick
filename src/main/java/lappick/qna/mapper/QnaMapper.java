package lappick.qna.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import lappick.qna.domain.Qna;
import lappick.qna.dto.QnaResponse;

import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface QnaMapper {
    
    void insertQna(Qna dto);

    List<QnaResponse> selectQnaByGoodsNum(Map<String, Object> params);

    int countQnaByGoodsNum(String goodsNum);
    
    List<QnaResponse> selectQnaByMemberNum(Map<String, Object> params);

    int countMyQna(Map<String, Object> params);

    List<QnaResponse> selectAllQna(Map<String, Object> params);

    int countAllQna(Map<String, Object> params);

    void updateAnswer(Qna dto);

    Qna selectQnaByNum(Integer qnaNum);

    int deleteQnaByMemberNums(List<String> memberNums);
    
    int deleteQnaByQnaNums(List<Integer> qnaNums);
}