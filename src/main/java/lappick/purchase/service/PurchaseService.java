package lappick.purchase.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lappick.cart.mapper.CartMapper;
import lappick.goods.dto.GoodsStockResponse;
import lappick.goods.service.GoodsService;
import lappick.member.mapper.MemberMapper;
import lappick.purchase.dto.*;
import lappick.purchase.mapper.PurchaseMapper;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseService {

    private static final Logger log = LoggerFactory.getLogger(PurchaseService.class);
    private final PurchaseMapper purchaseMapper;
    private final CartMapper cartMapper;
    private final GoodsService goodsService;
    private final MemberMapper memberMapper;

    public String placeOrder(PurchaseRequest command, String memberNum) {
        for (int i = 0; i < command.getGoodsNums().length; i++) {
            String goodsNum = command.getGoodsNums()[i];
            int quantity = Integer.parseInt(command.getGoodsQtys()[i]);
            GoodsStockResponse goodsStock = goodsService.getGoodsDetailWithStock(goodsNum);
            if (goodsStock == null) {
                throw new IllegalStateException("주문 처리 중 오류가 발생했습니다. (상품 정보 없음: " + goodsNum + ")");
            }
            if (goodsStock.getStockQty() < quantity) {
                throw new IllegalStateException("재고 부족: '" + goodsStock.getGoodsName() + "' 상품의 재고가 부족합니다. (요청: " + quantity + ", 현재: " + goodsStock.getStockQty() + ")");
            }
        }

        String purchaseNum = new SimpleDateFormat("yyyyMMdd").format(new Date()) + "-" + UUID.randomUUID().toString().substring(0, 8);

        PurchaseResponse purchase = new PurchaseResponse();
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
            PurchaseItemResponse item = new PurchaseItemResponse();
            item.setPurchaseNum(purchaseNum);
            item.setGoodsNum(command.getGoodsNums()[i]);
            item.setPurchaseQty(Integer.parseInt(command.getGoodsQtys()[i]));
            item.setPurchasePrice(Integer.parseInt(command.getGoodsPrices()[i]));
            purchaseMapper.insertPurchaseItem(item);
            String memo = "주문 출고 (#" + purchaseNum + ")";
            goodsService.changeStock(item.getGoodsNum(), -item.getPurchaseQty(), memo);
        }

        Map<String, Object> condition = new HashMap<>();
        condition.put("memberNum", memberNum);
        condition.put("goodsNums", command.getGoodsNums());
        cartMapper.goodsNumsDelete(condition);

        return purchaseNum;
    }

    @Transactional(readOnly = true)
    public PurchasePageResponse getMyOrderListPage(String memberNum, String searchWord, int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("memberNum", memberNum);
        params.put("searchWord", searchWord);
        params.put("startRow", (long)(page - 1) * size + 1);
        params.put("endRow", (long)page * size);

        List<PurchaseResponse> rawList = purchaseMapper.selectMyPurchases(params);
        int total = purchaseMapper.countMyPurchases(params);

        Map<String, List<PurchaseItemResponse>> itemsGroupedByPurchaseNum = rawList.stream()
            .filter(p -> p.getPurchaseItems() != null && !p.getPurchaseItems().isEmpty())
            .flatMap(p -> p.getPurchaseItems().stream())
            .collect(Collectors.groupingBy(PurchaseItemResponse::getPurchaseNum));

        List<PurchaseResponse> finalList = rawList.stream()
            .collect(Collectors.toMap(PurchaseResponse::getPurchaseNum, p -> p, (p1, p2) -> p1, LinkedHashMap::new))
            .values().stream()
            .peek(order -> {
                order.setPurchaseItems(itemsGroupedByPurchaseNum.get(order.getPurchaseNum()));
                // 배송 정보가 여러 상품에 걸쳐 중복될 수 있으므로, 첫 번째 non-null delivery 정보를 사용
                if (order.getDelivery() == null) {
                    rawList.stream()
                        .filter(raw -> raw.getPurchaseNum().equals(order.getPurchaseNum()) && raw.getDelivery() != null)
                        .findFirst()
                        .ifPresent(raw -> order.setDelivery(raw.getDelivery()));
                }
            })
            .collect(Collectors.toList());

        return buildPageResponse(finalList, page, size, total);
    }

    @Transactional(readOnly = true)
    public PurchasePageResponse getAllOrders(Map<String, Object> params, int page, int size) {
        params.put("startRow", (long)(page - 1) * size + 1);
        params.put("endRow", (long)page * size);
        List<PurchaseResponse> list = purchaseMapper.selectAllPurchases(params);
        int total = purchaseMapper.countAllPurchases(params);
        return buildPageResponse(list, page, size, total);
    }

    public void processShipping(DeliveryRequest dto) {
        purchaseMapper.insertDelivery(dto);
        purchaseMapper.updatePurchaseStatus(dto.getPurchaseNum(), "배송중");
    }

    public void updateOrderStatus(String purchaseNum, String status) {
        log.info("주문 상태 변경 시작. 주문번호: {}, 변경할 상태: '{}'", purchaseNum, status);
        String trimmedStatus = (status != null) ? status.trim() : null;
        if ("주문취소".equals(trimmedStatus)) {
            log.info("'주문취소' 상태 확인. 재고 복원 로직을 실행합니다.");
            PurchaseResponse purchase = purchaseMapper.selectPurchaseDetail(purchaseNum);
            if (purchase == null) {
                log.warn("주문번호 {}에 해당하는 주문 정보를 찾을 수 없습니다.", purchaseNum);
                return;
            }
            if (!"주문취소".equals(purchase.getPurchaseStatus())) {
                List<PurchaseItemResponse> items = purchase.getPurchaseItems();
                if (items != null && !items.isEmpty()) {
                    log.info("재고를 복원할 상품 {}건을 찾았습니다.", items.size());
                    for (PurchaseItemResponse item : items) {
                        String memo = "주문 취소 재고 복원 (#" + purchaseNum + ")";
                        goodsService.changeStock(item.getGoodsNum(), item.getPurchaseQty(), memo);
                        log.info("  - 상품번호: {}, 수량: {} 복원 완료", item.getGoodsNum(), item.getPurchaseQty());
                    }
                }
            }
        }
        log.info("DB 주문 상태를 '{}'(으)로 최종 업데이트합니다.", trimmedStatus);
        purchaseMapper.updatePurchaseStatus(purchaseNum, trimmedStatus);
    }

    @Transactional(readOnly = true)
    public PurchaseResponse getOrderDetail(String purchaseNum) {
        return purchaseMapper.selectPurchaseDetail(purchaseNum);
    }

    public void requestCancelOrder(String purchaseNum, String memberNum) {
        PurchaseResponse purchase = purchaseMapper.selectPurchaseDetail(purchaseNum);
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
    public List<PurchaseItemResponse> getPurchasedItems(String memberNum) {
        return purchaseMapper.selectPurchasedItemsByMemberNum(memberNum);
    }

    @Transactional(readOnly = true)
    public List<PurchaseItemResponse> getPurchasedItemsOfProduct(String memberNum, String goodsNum) {
        List<PurchaseItemResponse> allPurchases = purchaseMapper.selectPurchasedItemsByMemberNum(memberNum);
        return allPurchases.stream()
                .filter(item -> item.getGoodsNum().equals(goodsNum))
                .collect(Collectors.toList());
    }

    private PurchasePageResponse buildPageResponse(List<PurchaseResponse> items, int page, int size, int total) {
        int totalPages = (total > 0) ? (int) Math.ceil((double) total / size) : 0;
        int paginationRange = 5;
        int startPage = (int) (Math.floor((page - 1.0) / paginationRange) * paginationRange + 1);
        int endPage = Math.min(startPage + paginationRange - 1, totalPages);

        return PurchasePageResponse.builder()
                .items(items).page(page).size(size).total(total)
                .totalPages(totalPages).startPage(startPage).endPage(endPage)
                .hasPrev(startPage > 1).hasNext(endPage < totalPages)
                .build();
    }
}