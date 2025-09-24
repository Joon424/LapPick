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
    public List<PurchaseDTO> getMyOrderList(String memberNum, String searchWord) {
        Map<String, Object> params = new HashMap<>();
        params.put("memberNum", memberNum);
        params.put("searchWord", searchWord);
        return purchaseMapper.selectPurchaseList(params);
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
    
}