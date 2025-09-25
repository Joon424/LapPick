package mini.mapper;

import mini.domain.QnaDTO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface QnaMapper {
    void insertQna(QnaDTO dto);
    List<QnaDTO> selectQnaByGoodsNum(String goodsNum);
    List<QnaDTO> selectQnaByMemberNum(String memberNum);
}