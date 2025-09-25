package mini.controller;

import lombok.RequiredArgsConstructor;
import mini.domain.DeliveryDTO;
import mini.domain.PurchaseDTO;
import mini.domain.PurchaseListPage;
import mini.service.purchase.PurchaseService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/employee/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_EMP')") // 직원만 접근 가능
public class EmployeePurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping
    public String orderList(@RequestParam(value="page", defaultValue="1") int page,
                            @RequestParam(value="status", required=false) String status,
                            @RequestParam(value="searchWord", required=false) String searchWord,
                            Model model) {

        Map<String, Object> params = new HashMap<>();
        params.put("status", status);
        params.put("searchWord", searchWord);
        
        PurchaseListPage pageData = purchaseService.getAllOrders(params, page, 10); // 한 페이지에 10개씩 표시
        
        model.addAttribute("pageData", pageData);
        model.addAttribute("status", status);
        model.addAttribute("searchWord", searchWord);
        
        return "thymeleaf/purchase/empOrderList";
    }

    @PostMapping("/process-shipping")
    public String processShipping(DeliveryDTO dto) {
        purchaseService.processShipping(dto);
        return "redirect:/employee/orders";
    }
    
    @PostMapping("/update-status")
    public String updateStatus(@RequestParam("purchaseNum") String purchaseNum,
                               @RequestParam("status") String status) {
        purchaseService.updateOrderStatus(purchaseNum, status);
        return "redirect:/employee/orders";
    }
    
 // ▼▼▼ [추가] 직원용 주문 상세 페이지 이동 메소드 ▼▼▼
    @GetMapping("/{purchaseNum}")
    public String empOrderDetail(@PathVariable("purchaseNum") String purchaseNum, Model model) {
        PurchaseDTO order = purchaseService.getOrderDetail(purchaseNum);
        model.addAttribute("order", order);
        return "thymeleaf/employee/empOrderDetail";
    }
}