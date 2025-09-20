// mini/service/item/CartService.java (신규 파일)

package mini.service.item;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import mini.domain.CartDTO;
import mini.domain.GoodsCartDTO;
import mini.mapper.CartMapper;
import mini.mapper.MemberMapper;

@Service
@Transactional // 서비스 내의 모든 DB 작업은 하나의 트랜잭션으로 묶입니다.
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 주입
public class CartService {

    private final CartMapper cartMapper;
    private final MemberMapper memberMapper;

    /**
     * 사용자의 장바구니 목록과 합계 정보를 조회합니다.
     * (기존 CartListService 역할)
     * @param userId 현재 로그인한 사용자의 아이디
     * @return 장바구니 아이템 리스트와 합계 정보가 담긴 Map
     */
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public Map<String, Object> getCartList(String userId) {
        String memberNum = memberMapper.memberNumSelect(userId);
        
        List<GoodsCartDTO> list = cartMapper.cartSelectList(memberNum, null);
        
        long totalPrice = 0L; // long 타입으로 변경하여 큰 금액도 처리 가능
        int totalQty = 0;
        
        for (GoodsCartDTO dto : list) {
            totalPrice += (long)dto.getGoodsDTO().getGoodsPrice() * dto.getCartDTO().getCartQty();
            totalQty += dto.getCartDTO().getCartQty();
        }
        
        Map<String, Object> cartMap = new HashMap<>();
        cartMap.put("list", list);
        cartMap.put("totalPrice", totalPrice);
        cartMap.put("totalQty", totalQty);
        
        return cartMap;
    }

    /**
     * 장바구니에 상품을 추가(또는 수량 업데이트)합니다.
     * (기존 CartInsertService 역할)
     * @param userId 현재 로그인한 사용자의 아이디
     * @param goodsNum 추가할 상품 번호
     * @param qty 추가할 수량
     * @return 성공 여부 (true/false)
     */
    public boolean addItemToCart(String userId, String goodsNum, Integer qty) {
        String memberNum = memberMapper.memberNumSelect(userId);
        if (memberNum == null) {
            return false; // 회원 정보 없음
        }
        
        CartDTO dto = new CartDTO();
        dto.setCartQty(qty);
        dto.setGoodsNum(goodsNum);
        dto.setMemberNum(memberNum);
        
        cartMapper.cartMerge(dto);
        return true;
    }

    /**
     * 장바구니에서 여러 상품을 삭제합니다.
     * (기존 GoodsCartDelsService 역할)
     * @param userId 현재 로그인한 사용자의 아이디
     * @param goodsNums 삭제할 상품 번호 배열
     */
    public void removeItemsFromCart(String userId, String[] goodsNums) {
        String memberNum = memberMapper.memberNumSelect(userId);
        
        Map<String, Object> condition = new HashMap<>();
        condition.put("memberNum", memberNum);
        condition.put("goodsNums", Arrays.asList(goodsNums)); // 배열을 리스트로 변환
        
        cartMapper.goodsNumsDelete(condition);
    }
    
    /**
     * 장바구니 상품의 수량을 1 감소시킵니다.
     * (기존 CartQtyDownService 역할)
     * @param userId 현재 로그인한 사용자의 아이디
     * @param goodsNum 수량을 감소시킬 상품 번호
     */
    public void decreaseItemQuantity(String userId, String goodsNum) {
        String memberNum = memberMapper.memberNumSelect(userId);
        cartMapper.cartQtyDown(goodsNum, memberNum);
    }
}