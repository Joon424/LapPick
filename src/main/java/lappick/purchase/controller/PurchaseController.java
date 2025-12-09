package lappick.purchase.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lappick.cart.domain.Cart;
import lappick.cart.dto.CartItemResponse;
import lappick.cart.mapper.CartMapper;
import lappick.goods.dto.GoodsResponse;
import lappick.goods.mapper.GoodsMapper;
import lappick.member.dto.MemberResponse;
import lappick.member.mapper.MemberMapper;
import lappick.purchase.dto.DeliveryRequest;
import lappick.purchase.dto.PurchasePageResponse;
import lappick.purchase.dto.PurchaseRequest;
import lappick.purchase.dto.PurchaseResponse;
import lappick.purchase.service.PurchaseService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final MemberMapper memberMapper;
    private final CartMapper cartMapper;
    private final GoodsMapper goodsMapper;

    //==================== 사용자 기능 ====================//

    @PostMapping("/purchases/order")
    public String purchaseForm(@RequestParam("nums") String[] goodsNums, Authentication auth, Model model) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        MemberResponse memberDTO = memberMapper.selectOneById(userDetails.getUsername());
        
        List<CartItemResponse> items = cartMapper.cartSelectList(memberDTO.getMemberNum(), goodsNums, null);
        
        model.addAttribute("member", memberDTO);
        model.addAttribute("items", items);
        model.addAttribute("totalItemCount", items.size());
        
        model.addAttribute("totalQuantity", items.stream().mapToInt(item -> item.getCart().getCartQty()).sum());
        model.addAttribute("totalPayment", items.stream().mapToInt(item -> item.getGoods().getGoodsPrice() * item.getCart().getCartQty()).sum());
        
        return "user/purchase/order-form";
    }
    
    @GetMapping("/purchases/order-direct")
    public String purchaseFormDirect(@RequestParam("goodsNum") String goodsNum, @RequestParam(value = "qty", defaultValue = "1") int qty, Authentication auth, Model model) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        MemberResponse memberDTO = memberMapper.selectOneById(userDetails.getUsername());
        GoodsResponse goodsDTO = goodsMapper.selectOne(goodsNum);
        
        Cart cart = new Cart();
        cart.setCartQty(qty);
        
        CartItemResponse item = new CartItemResponse();
        item.setGoods(goodsDTO);
        item.setCart(cart);
        
        model.addAttribute("member", memberDTO);
        model.addAttribute("items", Collections.singletonList(item));
        model.addAttribute("totalItemCount", 1);
        model.addAttribute("totalQuantity", qty);
        model.addAttribute("totalPayment", goodsDTO.getGoodsPrice() * qty);
        return "user/purchase/order-form";
    }

    @PostMapping("/purchases")
    public String placeOrder(PurchaseRequest command, Authentication auth, RedirectAttributes ra) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        MemberResponse memberDTO = memberMapper.selectOneById(userDetails.getUsername());
        try {
            String purchaseNum = purchaseService.placeOrder(command, memberDTO.getMemberNum());
            return "redirect:/purchases/complete?purchaseNum=" + purchaseNum;
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart/cartList";
        }
    }
    
    @GetMapping("/purchases/complete")
    public String purchaseComplete(@RequestParam("purchaseNum") String purchaseNum, Model model) {
        model.addAttribute("purchaseNum", purchaseNum);
        return "user/purchase/order-complete";
    }
    
    @GetMapping("/purchases/my-orders")
    public String myOrderList(Authentication auth, Model model, @RequestParam(value="searchWord", required=false) String searchWord, @RequestParam(value="page", defaultValue = "1") int page) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        MemberResponse memberDTO = memberMapper.selectOneById(userDetails.getUsername());
        
        // 1. 서비스 호출 (페이지당 5개씩)
        PurchasePageResponse pageData = purchaseService.getMyOrderListPage(memberDTO.getMemberNum(), searchWord, page, 5);
        
        // 2. 페이지네이션 계산 (5페이지 블록)
        int paginationRange = 5;
        int startPage = (int) (Math.floor((pageData.getPage() - 1.0) / paginationRange) * paginationRange + 1);
        int endPage = Math.min(startPage + paginationRange - 1, pageData.getTotalPages());
        boolean hasPrev = startPage > 1;
        boolean hasNext = endPage < pageData.getTotalPages();

        // 3. 모델에 추가
        model.addAttribute("pageData", pageData);
        model.addAttribute("searchWord", searchWord);
        
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("hasPrev", hasPrev);
        model.addAttribute("hasNext", hasNext);
        
        return "user/purchase/order-list";
    } 
    
    @GetMapping("/purchases/{purchaseNum}")
    public String orderDetail(@PathVariable("purchaseNum") String purchaseNum, Model model) {
        PurchaseResponse order = purchaseService.getOrderDetail(purchaseNum);
        model.addAttribute("order", order);
        return "user/purchase/order-detail";
    }
    
    @PostMapping("/purchases/{purchaseNum}/cancel")
    public String cancelOrderRequest(@PathVariable("purchaseNum") String purchaseNum, Authentication auth) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        MemberResponse memberDTO = memberMapper.selectOneById(userDetails.getUsername());
        purchaseService.requestCancelOrder(purchaseNum, memberDTO.getMemberNum());
        return "redirect:/purchases/" + purchaseNum;
    }

    //==================== 관리자 기능 ====================//

    @GetMapping("/admin/purchases")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    public String orderList(@RequestParam(value="page", defaultValue="1") int page, @RequestParam(value="status", required=false) String status, @RequestParam(value="searchWord", required=false) String searchWord, Model model) {
        Map<String, Object> params = new HashMap<>();
        params.put("status", status);
        params.put("searchWord", searchWord);
        PurchasePageResponse pageData = purchaseService.getAllOrders(params, page, 5);
        model.addAttribute("pageData", pageData);
        model.addAttribute("status", status);
        model.addAttribute("searchWord", searchWord);
        return "admin/purchase/order-list";
    }
    
    @GetMapping("/admin/purchases/{purchaseNum}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    public String empOrderDetail(@PathVariable("purchaseNum") String purchaseNum, Model model) {
        PurchaseResponse order = purchaseService.getOrderDetail(purchaseNum);
        model.addAttribute("order", order);
        return "admin/purchase/order-detail";
    }

    @PostMapping("/admin/purchases/process-shipping")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    public String processShipping(DeliveryRequest dto, RedirectAttributes ra) {
        purchaseService.processShipping(dto);
        ra.addFlashAttribute("message", "송장 정보가 등록되었으며, 주문 상태가 '배송중'으로 변경되었습니다.");
        return "redirect:/admin/purchases";
    }
    
    @PostMapping("/admin/purchases/update-status")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    public String updateStatus(@RequestParam("purchaseNum") String purchaseNum, @RequestParam("status") String status, RedirectAttributes ra) {
        purchaseService.updateOrderStatus(purchaseNum, status);
        ra.addFlashAttribute("message", "주문 상태가 '" + status + "'(으)로 성공적으로 변경되었습니다.");
        return "redirect:/admin/purchases";
    }
    
    @PostMapping("/test/purchase")
    @ResponseBody
    public ResponseEntity<?> testPurchase(@RequestBody PurchaseRequest request) {
        try {
            // 고정 회원으로 주문 (인증 우회)
            String purchaseNum = purchaseService.placeOrder(request, "mem_100041");
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("purchaseNum", purchaseNum);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
}
