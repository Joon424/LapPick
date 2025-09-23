package mini.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import mini.command.CartCommand;
import mini.service.item.CartService;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * [페이지 이동] 장바구니 목록 페이지
     */
    @GetMapping("/cartList")
    public String cartList(
            @RequestParam(value = "searchWord", required = false) String searchWord, // 1. 검색어 파라미터 추가
            Principal principal, Model model) {
        
        if (principal == null) return "redirect:/login/item.login";
        
        // 2. 서비스에 검색어 전달
        Map<String, Object> cartMap = cartService.getCartList(principal.getName(), searchWord);
        
        model.addAttribute("list", cartMap.get("list"));
        model.addAttribute("totPri", cartMap.get("totalPrice"));
        model.addAttribute("totQty", cartMap.get("totalQty"));
        model.addAttribute("searchWord", searchWord); // 3. 뷰에 검색어 다시 전달
        
        return "thymeleaf/item/cartList";
    }

    /**
     * [API] 장바구니 상품 추가 (수량 +1)
     */
    @PostMapping("/cartAdd")
    @ResponseBody
    public String cartAdd(@RequestBody CartCommand cartCommand, Principal principal) {
        if (principal == null) return "000";
        cartService.addItemToCart(principal.getName(), cartCommand.getGoodsNum(), cartCommand.getQty());
        return "200";
    }

    /**
     * [API] 장바구니 수량 감소 (-1)
     * HTML의 $.get 요청에 맞춰 @GetMapping으로 변경
     */
    @GetMapping("/cartQtyDown")
    @ResponseBody
    public void cartQtyDown(@RequestParam("goodsNum") String goodsNum, Principal principal) {
        if (principal != null) {
            cartService.decreaseItemQuantity(principal.getName(), goodsNum);
        }
    }

    /**
     * [API] 선택 상품 삭제 (AJAX 요청 처리)
     */
    @PostMapping("/cartDels")
    @ResponseBody
    public String cartDels(@RequestBody List<String> goodsNums, Principal principal) {
        if (principal == null) return "000";
        // List를 String 배열로 변환하여 서비스에 전달
        cartService.removeItemsFromCart(principal.getName(), goodsNums.toArray(new String[0]));
        return "200";
    }
    
    /**
     * [API] 장바구니 전체 삭제 (신규 추가)
     */
    @PostMapping("/cartDelAll")
    @ResponseBody
    public String cartDelAll(Principal principal) {
        if (principal == null) return "000";
        cartService.removeAllItems(principal.getName());
        return "200";
    }

    /**
     * [페이지 이동] 개별 상품 삭제 (기존 기능)
     */
    @GetMapping("/cartDel")
    public String cartDel(@RequestParam("goodsNums") String goodsNums, Principal principal) {
        if (principal != null) {
            cartService.removeItemsFromCart(principal.getName(), new String[]{goodsNums});
        }
        return "redirect:cartList";
    }
    
    /**
     * [API] 현재 사용자의 장바구니 상품 개수를 반환
     */
    @GetMapping("/count")
    @ResponseBody
    public int getCartItemCount(Principal principal) {
        if (principal == null) {
            return 0; // 로그인하지 않은 사용자는 0개
        }
        return cartService.getCartItemCount(principal.getName());
    }
}