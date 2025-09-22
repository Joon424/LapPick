// mini/controller/ItemController.java (최종 확인용)

package mini.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mini.command.CartCommand;
import mini.service.item.CartService;
import mini.service.item.WishlistService;

@Controller
@RequestMapping("item")
@RequiredArgsConstructor
public class ItemController {

    private final CartService cartService;
    private final WishlistService wishlistService;

    // [API 역할] 상세페이지의 '장바구니' 버튼 클릭 시 AJAX 요청 처리
    @PostMapping("cartAdd")
    @ResponseBody 
    public String cartAdd(@RequestBody CartCommand cartCommand, Principal principal) {
        if (principal == null) return "000"; // 로그아웃 상태
        cartService.addItemToCart(principal.getName(), cartCommand.getGoodsNum(), cartCommand.getQty());
        return "200";
    }

    // [API 역할] 상세페이지의 '위시리스트' 아이콘 클릭 시 AJAX 요청 처리
    @PostMapping("wishItem")
    @ResponseBody
    public String wishItem(@RequestBody CartCommand cartCommand, Principal principal) {
        if (principal == null) return "login";
        wishlistService.toggleWishlistItem(principal.getName(), cartCommand.getGoodsNum());
        return "success";
    }
    
    // [API 역할] 장바구니 수량 감소 (goodsList.html에서 사용될 수 있음)
    @PostMapping("cartQtyDown")
    @ResponseBody
    public void cartQtyDown(@RequestParam("goodsNum") String goodsNum, Principal principal) {
        if (principal != null) {
            cartService.decreaseItemQuantity(principal.getName(), goodsNum);
        }
    }

    // [페이지 이동] '바로 구매' 기능
    @GetMapping("buyItem")
    public String buyItem(CartCommand cartCommand, Principal principal, HttpServletResponse response) throws Exception {
        if (principal == null) return "redirect:/login/item.login";
        cartService.addItemToCart(principal.getName(), cartCommand.getGoodsNum(), cartCommand.getQty());
        return "redirect:/purchase/goodsBuy?nums=" + cartCommand.getGoodsNum();
    }

    // [페이지 이동] 위시리스트 목록 페이지
    @GetMapping("wishList")
    public String wishList(Principal principal, Model model) {
        if (principal == null) return "redirect:/login/item.login";
        model.addAttribute("list", wishlistService.getWishlist(principal.getName()));
        return "thymeleaf/wish/wishList";
    }

    // [페이지 이동] 장바구니 목록 페이지
    @RequestMapping("cartList")
    public String cartList(Principal principal, Model model) {
        if (principal == null) return "redirect:/login/item.login";
        Map<String, Object> cartMap = cartService.getCartList(principal.getName());
        model.addAttribute("list", cartMap.get("list"));
        model.addAttribute("totPri", cartMap.get("totalPrice"));
        model.addAttribute("totQty", cartMap.get("totalQty"));
        return "thymeleaf/item/cartList";
    }

    // [페이지 이동 후 리다이렉트] 장바구니 상품 삭제
    @GetMapping("cartDel")
    public String cartDel(@RequestParam("goodsNums") String[] goodsNums, Principal principal) {
        if (principal != null) {
            cartService.removeItemsFromCart(principal.getName(), goodsNums);
        }
        return "redirect:cartList";
    }
}