package lappick.purchase.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lappick.cart.mapper.CartMapper;
import lappick.goods.dto.GoodsStockResponse;
import lappick.goods.mapper.GoodsMapper;
import lappick.goods.service.GoodsService;
import lappick.member.mapper.MemberMapper;
import lappick.purchase.dto.*;
import lappick.purchase.mapper.PurchaseMapper;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private static final Logger log = LoggerFactory.getLogger(PurchaseService.class);
    private final PurchaseMapper purchaseMapper;
    private final CartMapper cartMapper;
    private final GoodsService goodsService;
    private final MemberMapper memberMapper;
    private final GoodsMapper goodsMapper;

    
    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation = Isolation.READ_COMMITTED,
            timeout = 10,
            rollbackFor = Exception.class
    )
    public String placeOrder(PurchaseRequest command, String memberNum) {
        try {
            // ====== STEP 1: 재고 검증 (비관적 락 획득) ======
            log.info("주문 시작: 회원번호={}, 상품개수={}", memberNum, command.getGoodsNums().length);
            
            for (int i = 0; i < command.getGoodsNums().length; i++) {
                String goodsNum = command.getGoodsNums()[i];
                int quantity = Integer.parseInt(command.getGoodsQtys()[i]);
                
                // 1-1. 비관적 락 획득 (FOR UPDATE WAIT 5)
                // → 다른 트랜잭션이 이 상품을 주문 중이면 최대 5초 대기
                try {
                    goodsMapper.selectGoodsForUpdate(goodsNum);
                    log.debug("락 획득 성공: 상품번호={}", goodsNum);
                } catch (Exception e) {
                    log.error("락 획득 실패: 상품번호={}", goodsNum, e);
                    throw new CannotAcquireLockException(
                        "현재 많은 주문이 몰려 재고 확인이 지연되고 있습니다. 잠시 후 다시 시도해주세요.", e
                    );
                }
                
                // 1-2. 재고 확인
                GoodsStockResponse goodsStock = goodsService.getGoodsDetailWithStock(goodsNum);
                if (goodsStock == null) {
                    throw new IllegalStateException("상품 정보를 찾을 수 없습니다. (상품번호: " + goodsNum + ")");
                }
                
                // 1-3. 재고 검증
                if (goodsStock.getStockQty() < quantity) {
                    throw new IllegalStateException(
                        String.format("재고 부족: '%s' 상품의 재고가 부족합니다. (요청: %d개, 현재: %d개)", 
                            goodsStock.getGoodsName(), quantity, goodsStock.getStockQty())
                    );
                }
            }
            
            // ====== STEP 2: 주문 생성 ======
            String purchaseNum = new SimpleDateFormat("yyyyMMdd").format(new Date()) 
                + "-" + UUID.randomUUID().toString().substring(0, 8);
            
            PurchaseResponse purchase = new PurchaseResponse();
            purchase.setPurchaseNum(purchaseNum);
            purchase.setMemberNum(memberNum);
            purchase.setReceiverName(command.getReceiverName());
            purchase.setReceiverPhone(command.getReceiverPhone());
            
            String fullAddress = "(" + command.getPurchasePost() + ") " 
                + command.getPurchaseAddr() + " " + command.getPurchaseAddrDetail();
            purchase.setPurchaseAddr(fullAddress);
            purchase.setPurchaseMsg(command.getPurchaseMsg());
            purchase.setPurchaseTotal(command.getTotalPayment());
            purchase.setPaymentMethod(command.getPaymentMethod());
            
            if ("신용카드".equals(command.getPaymentMethod())) {
                purchase.setCardCompany(command.getCardCompany());
                purchase.setCardNumber(command.getCardNumber());
            } else if ("가상계좌".equals(command.getPaymentMethod())) {
                purchase.setBankName(command.getBankName());
                purchase.setDepositorName(command.getDepositorName());
            }
            
            purchaseMapper.insertPurchase(purchase);
            log.info("주문 헤더 생성 완료: {}", purchaseNum);
            
            // ====== STEP 3: 주문 상세 + 재고 차감 ======
            for (int i = 0; i < command.getGoodsNums().length; i++) {
                PurchaseItemResponse item = new PurchaseItemResponse();
                item.setPurchaseNum(purchaseNum);
                item.setGoodsNum(command.getGoodsNums()[i]);
                item.setPurchaseQty(Integer.parseInt(command.getGoodsQtys()[i]));
                item.setPurchasePrice(Integer.parseInt(command.getGoodsPrices()[i]));
                
                purchaseMapper.insertPurchaseItem(item);
                
                // 재고 차감 (수불부 INSERT)
                // ★ 중요: changeStock은 Propagation.REQUIRED이므로
                //         이 트랜잭션 안에서 실행됨 (별도 트랜잭션이 아님)
                String memo = "주문 출고 (#" + purchaseNum + ")";
                goodsService.changeStock(item.getGoodsNum(), -item.getPurchaseQty(), memo);
                
                log.debug("재고 차감 완료: 상품번호={}, 수량={}", item.getGoodsNum(), item.getPurchaseQty());
            }
            
            // ====== STEP 4: 장바구니 삭제 ======
            Map<String, Object> condition = new HashMap<>();
            condition.put("memberNum", memberNum);
            condition.put("goodsNums", command.getGoodsNums());
            cartMapper.goodsNumsDelete(condition);
            
            log.info("주문 완료: {}", purchaseNum);
            return purchaseNum;
            
        } catch (CannotAcquireLockException e) {
            // 락 획득 실패 - 사용자에게 재시도 안내
            log.warn("락 타임아웃: 회원번호={}", memberNum, e);
            throw e;
        } catch (IllegalStateException e) {
            // 재고 부족 등 비즈니스 예외
            log.warn("주문 실패: 회원번호={}, 사유={}", memberNum, e.getMessage());
            throw e;
        } catch (Exception e) {
            // 예상치 못한 예외
            log.error("주문 처리 중 오류 발생: 회원번호={}", memberNum, e);
            throw new RuntimeException("주문 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", e);
        }
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

    @Transactional(
            propagation = Propagation.REQUIRED,
            timeout = 10,
            rollbackFor = Exception.class
        )
        public void updateOrderStatus(String purchaseNum, String status) {
            log.info("주문 상태 변경 시작. 주문번호: {}, 변경할 상태: '{}'", purchaseNum, status);
            String trimmedStatus = (status != null) ? status.trim() : null;
            
            if ("주문취소".equals(trimmedStatus)) {
                log.info("'주문취소' 상태 감지. 재고 복원 프로세스 시작");
                PurchaseResponse purchase = purchaseMapper.selectPurchaseDetail(purchaseNum);
                
                if (purchase == null) {
                    log.warn("주문번호 {}에 해당하는 주문 정보를 찾을 수 없습니다.", purchaseNum);
                    return;
                }
                
                if (!"주문취소".equals(purchase.getPurchaseStatus())) {
                    List<PurchaseItemResponse> items = purchase.getPurchaseItems();
                    if (items != null && !items.isEmpty()) {
                        log.info("재고를 복원할 상품 {}건의 처리 시작", items.size());
                        for (PurchaseItemResponse item : items) {
                            String memo = "주문 취소 재고 복원 (#" + purchaseNum + ")";
                            
                            // 재고 복원 (양수로 INSERT)
                            goodsService.changeStock(item.getGoodsNum(), item.getPurchaseQty(), memo);
                            
                            log.info("  - 상품번호: {}, 수량: {} 복원 완료", item.getGoodsNum(), item.getPurchaseQty());
                        }
                    }
                }
            }
            
            log.info("DB 주문 상태를 '{}'(으)로 업데이트 수행", trimmedStatus);
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