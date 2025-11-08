package lappick.cart.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lappick.cart.domain.Cart;
import lappick.cart.dto.CartItemResponse;
import lappick.cart.mapper.CartMapper;
import lappick.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {

    private final CartMapper cartMapper;
    private final MemberMapper memberMapper;

    @Transactional(readOnly = true)
    public Map<String, Object> getCartList(String userId, String searchWord) {
        String memberNum = memberMapper.memberNumSelect(userId);
        
        List<CartItemResponse> list = cartMapper.cartSelectList(memberNum, null, searchWord);
        
        long totalPrice = 0L;
        int totalQty = 0;
        
        for (CartItemResponse dto : list) {
            totalPrice += (long)dto.getGoods().getGoodsPrice() * dto.getCart().getCartQty();
            totalQty += dto.getCart().getCartQty();
        }
        
        Map<String, Object> cartMap = new HashMap<>();
        cartMap.put("list", list);
        cartMap.put("totalPrice", totalPrice);
        cartMap.put("totalQty", totalQty);
        
        return cartMap;
    }

    public boolean addItemToCart(String userId, String goodsNum, Integer qty) {
        String memberNum = memberMapper.memberNumSelect(userId);
        if (memberNum == null) {
            return false;
        }
        
        Cart dto = new Cart();
        dto.setCartQty(qty);
        dto.setGoodsNum(goodsNum);
        dto.setMemberNum(memberNum);
        
        cartMapper.cartMerge(dto);
        return true;
    }

    public void removeItemsFromCart(String userId, String[] goodsNums) {
        String memberNum = memberMapper.memberNumSelect(userId);
        
        Map<String, Object> condition = new HashMap<>();
        condition.put("memberNum", memberNum);
        condition.put("goodsNums", Arrays.asList(goodsNums));
        
        cartMapper.goodsNumsDelete(condition);
    }
    
    public void removeAllItems(String userId) {
        String memberNum = memberMapper.memberNumSelect(userId);
        if (memberNum != null) {
            cartMapper.cartAllDelete(memberNum);
        }
    }

    public void decreaseItemQuantity(String userId, String goodsNum) {
        String memberNum = memberMapper.memberNumSelect(userId);
        cartMapper.cartQtyDown(goodsNum, memberNum);
    }
    
    @Transactional(readOnly = true)
    public int getCartItemCount(String userId) {
        String memberNum = memberMapper.memberNumSelect(userId);
        if (memberNum == null) {
            return 0;
        }
        return cartMapper.countCartItems(memberNum);
    }
}