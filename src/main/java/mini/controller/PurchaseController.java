package mini.controller;

import lombok.RequiredArgsConstructor;
import mini.command.PurchaseCommand;
import mini.domain.CartDTO;
import mini.domain.GoodsCartDTO;
import mini.domain.GoodsDTO;
import mini.domain.MemberDTO;
import mini.domain.PurchaseDTO;
import mini.domain.PurchaseListPage;
import mini.mapper.CartMapper;
import mini.mapper.GoodsMapper;
import mini.mapper.MemberMapper;
import mini.service.purchase.PurchaseService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final MemberMapper memberMapper;
    private final CartMapper cartMapper;
    private final PurchaseService purchaseService;
    private final GoodsMapper goodsMapper; // GoodsMapper 의존성 주입 추가


    // 장바구니 -> 주문 페이지로 이동
    @PostMapping("/order")
    public String purchaseForm(@RequestParam("nums") String[] goodsNums, Authentication auth, Model model) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        MemberDTO memberDTO = memberMapper.selectOneById(userDetails.getUsername());
        
        // 주문할 상품 목록 정보 가져오기
        List<GoodsCartDTO> items = cartMapper.cartSelectList(memberDTO.getMemberNum(), goodsNums, null);
        
        int totalItemCount = items.size();
        int totalQuantity = items.stream().mapToInt(item -> item.getCartDTO().getCartQty()).sum();
        int totalPayment = items.stream()
                .mapToInt(item -> item.getGoodsDTO().getGoodsPrice() * item.getCartDTO().getCartQty())
                .sum();
        
        model.addAttribute("member", memberDTO);
        model.addAttribute("items", items);
        model.addAttribute("totalItemCount", totalItemCount); // [추가]
        model.addAttribute("totalQuantity", totalQuantity); 
        model.addAttribute("totalPayment", totalPayment);
        
        return "thymeleaf/purchase/order";
    }
    
    // ▼▼▼▼▼ [신규] '바로 구매'를 위한 메소드 추가 ▼▼▼▼▼
    @GetMapping("/orderDirect")
    public String purchaseFormDirect(@RequestParam("goodsNum") String goodsNum,
                                     @RequestParam(value = "qty", defaultValue = "1") int qty,
                                     Authentication auth, Model model) {

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        MemberDTO memberDTO = memberMapper.selectOneById(userDetails.getUsername());

        // GoodsMapper를 사용해 상품 정보를 직접 조회
        GoodsDTO goodsDTO = goodsMapper.selectOne(goodsNum);

        // View(order.html)와 데이터 구조를 맞추기 위해 GoodsCartDTO 형태로 가공
        CartDTO cartDTO = new CartDTO();
        cartDTO.setCartQty(qty);

        GoodsCartDTO item = new GoodsCartDTO();
        item.setGoodsDTO(goodsDTO);
        item.setCartDTO(cartDTO);

        // 상품이 1개이므로 List로 감싸서 모델에 추가
        List<GoodsCartDTO> items = Collections.singletonList(item);

     // [수정] 총 상품 종류, 총 수량, 총 결제 금액 계산
        int totalItemCount = 1;
        int totalQuantity = qty;
        int totalPayment = goodsDTO.getGoodsPrice() * qty;

        model.addAttribute("member", memberDTO);
        model.addAttribute("items", items);
        model.addAttribute("totalItemCount", totalItemCount);
        model.addAttribute("totalQuantity", totalQuantity); 
        model.addAttribute("totalPayment", totalPayment);

        return "thymeleaf/purchase/order";
    }

    // 주문하기 (결제)
    @PostMapping("/placeOrder")
    public String placeOrder(PurchaseCommand command, Authentication auth) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        MemberDTO memberDTO = memberMapper.selectOneById(userDetails.getUsername());
        
        String purchaseNum = purchaseService.placeOrder(command, memberDTO.getMemberNum());
        
        return "redirect:/purchase/complete?purchaseNum=" + purchaseNum;
    }
    
    // 주문 완료 페이지
    @GetMapping("/complete")
    public String purchaseComplete(@RequestParam("purchaseNum") String purchaseNum, Model model) {
        model.addAttribute("purchaseNum", purchaseNum);
        return "thymeleaf/purchase/orderComplete";
    }
    
    @GetMapping("/my-orders")
    public String myOrderList(Authentication auth, Model model,
                              @RequestParam(value="searchWord", required=false) String searchWord,
                              @RequestParam(value="page", defaultValue = "1") int page) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        MemberDTO memberDTO = memberMapper.selectOneById(userDetails.getUsername());
        
        // 한 페이지에 5개씩 표시
        int pageSize = 5;
        PurchaseListPage pageData = purchaseService.getMyOrderListPage(memberDTO.getMemberNum(), searchWord, page, pageSize);
        
        model.addAttribute("pageData", pageData);
        model.addAttribute("searchWord", searchWord); // 검색어 유지를 위해 추가
        return "thymeleaf/purchase/orderList";
    }   
    
    @GetMapping("/detail/{purchaseNum}")
    public String orderDetail(@PathVariable("purchaseNum") String purchaseNum, Model model) {
        PurchaseDTO order = purchaseService.getOrderDetail(purchaseNum);
        model.addAttribute("order", order);
        return "thymeleaf/purchase/orderDetail";
    }
    
    @GetMapping("/cancel/{purchaseNum}")
    public String cancelOrderRequest(@PathVariable("purchaseNum") String purchaseNum, Authentication auth) {
        // 본인의 주문이 맞는지 간단히 확인 (선택적이지만 권장)
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        MemberDTO memberDTO = memberMapper.selectOneById(userDetails.getUsername());
        
        purchaseService.requestCancelOrder(purchaseNum, memberDTO.getMemberNum());
        
        // 처리 후 다시 상세 페이지로 리다이렉트
        return "redirect:/purchase/detail/" + purchaseNum;
    }

}