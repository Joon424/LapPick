package mini.service.purchase;

import lombok.RequiredArgsConstructor;
import mini.command.PurchaseCommand;
import mini.domain.DeliveryDTO;
import mini.domain.GoodsStockDTO;
import mini.domain.PurchaseDTO;
import mini.domain.PurchaseListDTO;
import mini.domain.PurchaseListPage;
import mini.mapper.CartMapper;
import mini.mapper.PurchaseMapper;
import mini.service.goods.GoodsService;

// [추가] 로깅을 위한 import
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    
    // [추가] 로거(Logger) 선언
    private static final Logger log = LoggerFactory.getLogger(PurchaseService.class);

    private final PurchaseMapper purchaseMapper;
    private final CartMapper cartMapper;
    private final GoodsService goodsService;

    @Transactional
    public String placeOrder(PurchaseCommand command, String memberNum) {
        for (int i = 0; i < command.getGoodsNums().length; i++) {
            String goodsNum = command.getGoodsNums()[i];
            int quantity = Integer.parseInt(command.getGoodsQtys()[i]);
            
            GoodsStockDTO goodsStock = goodsService.getGoodsDetailWithStock(goodsNum);
            
            if (goodsStock == null) {
                throw new IllegalStateException("주문 처리 중 오류가 발생했습니다. (상품 정보 없음: " + goodsNum + ")");
            }
            if (goodsStock.getStockQty() < quantity) {
                throw new IllegalStateException("재고 부족: '" + goodsStock.getGoodsName() + "' 상품의 재고가 부족합니다. (요청: " + quantity + ", 현재: " + goodsStock.getStockQty() + ")");
            }
        }
        
        String purchaseNum = new SimpleDateFormat("yyyyMMdd").format(new Date()) + "-" + UUID.randomUUID().toString().substring(0, 8);

        PurchaseDTO purchase = new PurchaseDTO();
        purchase.setPurchaseNum(purchaseNum);
        purchase.setMemberNum(memberNum);
        purchase.setReceiverName(command.getReceiverName());
        purchase.setReceiverPhone(command.getReceiverPhone());
        String fullAddress = "(" + command.getPurchasePost() + ") " + command.getPurchaseAddr() + " " + command.getPurchaseAddrDetail();
        purchase.setPurchaseAddr(fullAddress);
        purchase.setPurchaseMsg(command.getPurchaseMsg());
        purchase.setPurchaseTotal(command.getTotalPayment());
        
        purchase.setPaymentMethod(command.getPaymentMethod());
        if ("신용카드".equals(command.getPaymentMethod())) {
            purchase.setCardCompany(command.getCardCompany());
            purchase.setCardNumber(command.getCardNumber());
        } else if ("무통장입금".equals(command.getPaymentMethod())) {
            purchase.setBankName(command.getBankName());
            purchase.setDepositorName(command.getDepositorName());
        }
        
        purchaseMapper.insertPurchase(purchase);

        for (int i = 0; i < command.getGoodsNums().length; i++) {
            PurchaseListDTO item = new PurchaseListDTO();
            item.setPurchaseNum(purchaseNum);
            item.setGoodsNum(command.getGoodsNums()[i]);
            item.setPurchaseQty(Integer.parseInt(command.getGoodsQtys()[i]));
            item.setPurchasePrice(Integer.parseInt(command.getGoodsPrices()[i]));
            purchaseMapper.insertPurchaseList(item);
        }

        for (int i = 0; i < command.getGoodsNums().length; i++) {
            String goodsNum = command.getGoodsNums()[i];
            int quantity = Integer.parseInt(command.getGoodsQtys()[i]);
            
            String memo = "주문 출고 (#" + purchaseNum + ")";
            goodsService.changeStock(goodsNum, -quantity, memo);
        }

        Map<String, Object> condition = new HashMap<>();
        condition.put("memberNum", memberNum);
        condition.put("goodsNums", command.getGoodsNums());
        cartMapper.goodsNumsDelete(condition);

        return purchaseNum;
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
        purchaseMapper.insertDelivery(dto);
        purchaseMapper.updatePurchaseStatus(dto.getPurchaseNum(), "배송중");
    }
    
    @Transactional
    public void updateOrderStatus(String purchaseNum, String status) {
        log.info("주문 상태 변경 시작. 주문번호: {}, 변경할 상태: '{}'", purchaseNum, status);
        
        String trimmedStatus = (status != null) ? status.trim() : null;
        
        // [수정] 조건을 "취소완료"에서 "주문취소"로 변경
        if ("주문취소".equals(trimmedStatus)) {
            log.info("'주문취소' 상태 확인. 재고 복원 로직을 실행합니다.");
            
            PurchaseDTO purchase = purchaseMapper.selectPurchaseDetail(purchaseNum);
            
            if (purchase == null) {
                log.warn("주문번호 {}에 해당하는 주문 정보를 찾을 수 없습니다.", purchaseNum);
                return;
            }
            log.info("주문 정보 조회 완료. 현재 DB 상태: {}", purchase.getPurchaseStatus());

            // [수정] 중복 복원 방지 조건도 "주문취소"로 변경
            if (!"주문취소".equals(purchase.getPurchaseStatus())) {
                List<PurchaseListDTO> items = purchase.getPurchaseList();
                
                if (items != null && !items.isEmpty()) {
                    log.info("재고를 복원할 상품 {}건을 찾았습니다.", items.size());
                    for (PurchaseListDTO item : items) {
                        String memo = "주문 취소 재고 복원 (#" + purchaseNum + ")";
                        goodsService.changeStock(item.getGoodsNum(), item.getPurchaseQty(), memo);
                        log.info("  - 상품번호: {}, 수량: {} 복원 완료", item.getGoodsNum(), item.getPurchaseQty());
                    }
                } else {
                    log.warn("주문번호 {}에 복원할 상품 목록이 없습니다.", purchaseNum);
                }
            } else {
                log.warn("주문번호 {}는 이미 '주문취소' 상태이므로 재고를 중복으로 복원하지 않습니다.", purchaseNum);
            }
        }
        
        log.info("DB 주문 상태를 '{}'(으)로 최종 업데이트합니다.", trimmedStatus);
        purchaseMapper.updatePurchaseStatus(purchaseNum, trimmedStatus);
    }
    
    @Transactional(readOnly = true)
    public PurchaseDTO getOrderDetail(String purchaseNum) {
        return purchaseMapper.selectPurchaseDetail(purchaseNum);
    }
    
    @Transactional
    public void requestCancelOrder(String purchaseNum, String memberNum) {
        PurchaseDTO purchase = purchaseMapper.selectPurchaseDetail(purchaseNum);
        
        if (purchase != null && purchase.getMemberNum().equals(memberNum)) {
            if ("결제완료".equals(purchase.getPurchaseStatus()) || "상품준비중".equals(purchase.getPurchaseStatus())) {
                purchaseMapper.updatePurchaseStatus(purchaseNum, "취소요청");
            } else {
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
    
    @Transactional(readOnly = true)
    public List<PurchaseListDTO> getPurchasedItemsOfProduct(String memberNum, String goodsNum) {
        List<PurchaseListDTO> allPurchases = purchaseMapper.selectPurchasedItemsByMemberNum(memberNum);
        
        return allPurchases.stream()
                .filter(item -> item.getGoodsNum().equals(goodsNum))
                .collect(Collectors.toList());
    }
}