package mini.service.purchase;

import lombok.RequiredArgsConstructor;
import mini.command.PurchaseCommand;
import mini.domain.DeliveryDTO;
import mini.domain.PurchaseDTO;
import mini.domain.PurchaseListDTO;
import mini.domain.PurchaseListPage;
import mini.mapper.CartMapper;
import mini.mapper.PurchaseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseMapper purchaseMapper;
    private final CartMapper cartMapper;

    @Transactional
    public String placeOrder(PurchaseCommand command, String memberNum) {
        // 1. 주문번호 생성 (날짜-UUID)
        String purchaseNum = new SimpleDateFormat("yyyyMMdd").format(new Date()) + "-" + UUID.randomUUID().toString().substring(0, 8);

        // 2. 주문 정보(PurchaseDTO) 생성 및 DB 저장
        PurchaseDTO purchase = new PurchaseDTO();
        purchase.setPurchaseNum(purchaseNum);
        purchase.setMemberNum(memberNum);
        purchase.setReceiverName(command.getReceiverName());
        purchase.setReceiverPhone(command.getReceiverPhone());
        String fullAddress = "(" + command.getPurchasePost() + ") " + command.getPurchaseAddr() + " " + command.getPurchaseAddrDetail();
        purchase.setPurchaseAddr(fullAddress);
        purchase.setPurchaseMsg(command.getPurchaseMsg());
        purchase.setPurchaseTotal(command.getTotalPayment());
        
        // 결제 정보 DTO에 저장
        purchase.setPaymentMethod(command.getPaymentMethod());
        if ("신용카드".equals(command.getPaymentMethod())) {
            purchase.setCardCompany(command.getCardCompany());
            purchase.setCardNumber(command.getCardNumber());
        } else if ("무통장입금".equals(command.getPaymentMethod())) {
            purchase.setBankName(command.getBankName());
            purchase.setDepositorName(command.getDepositorName());
        }
        
        purchaseMapper.insertPurchase(purchase);

        // 3. 주문 상품 목록(PurchaseListDTO) 생성 및 DB 저장
        for (int i = 0; i < command.getGoodsNums().length; i++) {
            PurchaseListDTO item = new PurchaseListDTO();
            item.setPurchaseNum(purchaseNum);
            item.setGoodsNum(command.getGoodsNums()[i]);
            item.setPurchaseQty(Integer.parseInt(command.getGoodsQtys()[i]));
            item.setPurchasePrice(Integer.parseInt(command.getGoodsPrices()[i]));
            purchaseMapper.insertPurchaseList(item);
        }

        // 4. 장바구니에서 구매한 상품 삭제
        Map<String, Object> condition = new HashMap<>();
        condition.put("memberNum", memberNum);
        condition.put("goodsNums", command.getGoodsNums());
        cartMapper.goodsNumsDelete(condition);

        return purchaseNum; // 컨트롤러에 주문번호 반환
    }
    
    @Transactional(readOnly = true)
    public PurchaseListPage getMyOrderListPage(String memberNum, String searchWord, int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("memberNum", memberNum);
        params.put("searchWord", searchWord);
        params.put("startRow", (page - 1L) * size + 1);
        params.put("endRow", page * 1L * size);
        
        List<PurchaseDTO> list = purchaseMapper.selectPurchaseList(params);
        int total = purchaseMapper.countMyOrders(params);
        
        return PurchaseListPage.builder()
                .items(list)
                .page(page).size(size)
                .total(total)
                .build();
    }
    
    
    @Transactional(readOnly = true)
    public PurchaseListPage getAllOrders(Map<String, Object> params, int page, int size) {
        params.put("startRow", (page - 1L) * size + 1);
        params.put("endRow", page * 1L * size);
        
        List<PurchaseDTO> list = purchaseMapper.selectAllPurchases(params);
        int total = purchaseMapper.countAllPurchases(params);
        
        return PurchaseListPage.builder()
                .items(list)
                .page(page).size(size)
                .total(total)
                .build();
    }
        
    @Transactional
    public void processShipping(DeliveryDTO dto) {
        // 1. DELIVERY 테이블에 배송 정보(택배사, 송장번호) 등록
        purchaseMapper.insertDelivery(dto);
        // 2. PURCHASE 테이블의 상태를 '배송중'으로 변경
        purchaseMapper.updatePurchaseStatus(dto.getPurchaseNum(), "배송중");
    }
    
    @Transactional
    public void updateOrderStatus(String purchaseNum, String status) {
        purchaseMapper.updatePurchaseStatus(purchaseNum, status);
    }
    
    @Transactional(readOnly = true)
    public PurchaseDTO getOrderDetail(String purchaseNum) {
        return purchaseMapper.selectPurchaseDetail(purchaseNum);
    }
    
    @Transactional
    public void requestCancelOrder(String purchaseNum, String memberNum) {
        // 주문 정보를 가져와 본인 주문이 맞는지, 취소 가능한 상태인지 확인
        PurchaseDTO purchase = purchaseMapper.selectPurchaseDetail(purchaseNum);
        
        if (purchase != null && purchase.getMemberNum().equals(memberNum)) {
            if ("결제완료".equals(purchase.getPurchaseStatus()) || "상품준비중".equals(purchase.getPurchaseStatus())) {
                purchaseMapper.updatePurchaseStatus(purchaseNum, "취소요청");
            } else {
                // 이미 배송이 시작되었거나 취소된 주문에 대한 예외 처리 (선택)
                throw new IllegalStateException("주문 취소가 불가능한 상태입니다.");
            }
        } else {
             throw new SecurityException("주문 정보가 없거나 취소할 권한이 없습니다.");
        }
    }
    
    @Transactional(readOnly = true)
    public List<PurchaseListDTO> getPurchasedItems(String memberNum) {
        return purchaseMapper.selectPurchasedItemsByMemberNum(memberNum);
    }
    
}