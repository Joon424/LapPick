package mini.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import mini.domain.CartDTO;
import mini.domain.GoodsCartDTO;
import mini.domain.GoodsDTO;

@Mapper
public interface CartMapper {
	public void cartMerge(CartDTO dto);
	
	public GoodsDTO goodsSelect(String goodsNum);
	public CartDTO cartSelect(Integer cartNum);
	 // [수정] searchWord 파라미터 추가
    public List<GoodsCartDTO> cartSelectList(
            @Param("memberNum") String memberNum, 
            @Param("nums") String [] nums,
            @Param("searchWord") String searchWord);
    // [신규] 전체삭제 메소드 추가
    public int cartAllDelete(String memberNum);
	public int cartQtyDown(@Param("goodsNum") String goodsNum
            ,@Param("memberNum") String memberNum);
	public int goodsNumsDelete(Map<String, Object> condition);
	public int countCartItems(String memberNum);
}











