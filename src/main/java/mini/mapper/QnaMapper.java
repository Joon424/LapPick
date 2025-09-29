package mini.mapper;

import mini.domain.QnaDTO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface QnaMapper {
    void insertQna(QnaDTO dto);
    List<QnaDTO> selectQnaByGoodsNum(String goodsNum);
    List<QnaDTO> selectQnaByMemberNum(Map<String, Object> params); // 파라미터 타입을 Map으로 변경
    int countMyQna(Map<String, Object> params); // [추가]
    List<QnaDTO> selectAllQna(Map<String, Object> params);
    int countAllQna(Map<String, Object> params);
    void updateAnswer(QnaDTO dto);
}