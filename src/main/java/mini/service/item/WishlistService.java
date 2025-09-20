// mini/service/item/WishlistService.java (신규 파일)

package mini.service.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import mini.domain.GoodsDTO;
import mini.mapper.ItemMapper;
import mini.mapper.MemberMapper;

@Service
@Transactional
@RequiredArgsConstructor
public class WishlistService {

    private final ItemMapper itemMapper;
    private final MemberMapper memberMapper;

    /**
     * 위시리스트에 상품을 추가하거나 제거합니다 (토글 방식).
     * (기존 GoodsWishService 역할)
     * @param userId 현재 로그인한 사용자의 아이디
     * @param goodsNum 추가/제거할 상품 번호
     */
    public void toggleWishlistItem(String userId, String goodsNum) {
        String memberNum = memberMapper.memberNumSelect(userId);
        
        Map<String, String> map = new HashMap<>();
        map.put("goodsNum", goodsNum);
        map.put("memberNum", memberNum);
        
        itemMapper.wishItem(map);
    }
    
    /**
     * 사용자의 위시리스트 전체를 조회합니다.
     * (기존 GoodsWishListService 역할)
     * @param userId 현재 로그인한 사용자의 아이디
     * @return 위시리스트에 담긴 GoodsDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<GoodsDTO> getWishlist(String userId) {
        String memberNum = memberMapper.memberNumSelect(userId);
        return itemMapper.wishSelectList(memberNum);
    }
    
    /**
     * 특정 상품이 위시리스트에 있는지 확인합니다.
     * @param userId 현재 로그인한 사용자의 아이디
     * @param goodsNum 확인할 상품 번호
     * @return 위시리스트에 있으면 true, 없으면 false
     */
    @Transactional(readOnly = true)
    public boolean isItemInWishlist(String userId, String goodsNum) {
        String memberNum = memberMapper.memberNumSelect(userId);
        Map<String, String> map = new HashMap<>();
        map.put("goodsNum", goodsNum);
        map.put("memberNum", memberNum);
        Integer count = itemMapper.wishCountSelectOne(map);
        return count != null && count > 0;
    }
}