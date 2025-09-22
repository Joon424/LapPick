package mini.service.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import mini.domain.GoodsDTO;
import mini.mapper.GoodsMapper; // [수정] ItemMapper -> GoodsMapper
import mini.mapper.MemberMapper;

@Service
@Transactional
@RequiredArgsConstructor
public class WishlistService {

    private final GoodsMapper goodsMapper; // [수정] ItemMapper -> GoodsMapper
    private final MemberMapper memberMapper;

    /**
     * 위시리스트에 상품을 추가하거나 제거합니다 (토글 방식).
     */
    public void toggleWishlistItem(String userId, String goodsNum) {
        String memberNum = memberMapper.memberNumSelect(userId);
        
        Map<String, String> map = new HashMap<>();
        map.put("goodsNum", goodsNum);
        map.put("memberNum", memberNum);
        
        // [수정] 토글 로직을 명확하게 변경
        if (isItemInWishlist(userId, goodsNum)) {
            goodsMapper.wishDelete(map); // 있으면 삭제
        } else {
            goodsMapper.wishInsert(map); // 없으면 추가
        }
    }
    
    /**
     * 사용자의 위시리스트 전체를 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<GoodsDTO> getWishlist(String userId) {
        String memberNum = memberMapper.memberNumSelect(userId);
        return goodsMapper.wishSelectList(memberNum);
    }
    
    /**
     * 특정 상품이 위시리스트에 있는지 확인합니다.
     */
    @Transactional(readOnly = true)
    public boolean isItemInWishlist(String userId, String goodsNum) {
        String memberNum = memberMapper.memberNumSelect(userId);
        if (memberNum == null) return false; // 비회원 등 예외처리
        
        Map<String, String> map = new HashMap<>();
        map.put("goodsNum", goodsNum);
        map.put("memberNum", memberNum);
        
        Integer count = goodsMapper.wishCountSelectOne(map);
        return count != null && count > 0;
    }
}