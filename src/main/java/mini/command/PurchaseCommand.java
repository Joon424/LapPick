package mini.command;

import lombok.Data;

@Data
public class PurchaseCommand {
    // 배송지 정보
    private String receiverName;
    private String receiverPhone;
    private String purchasePost; // 우편번호
    private String purchaseAddr; // 기본주소
    private String purchaseAddrDetail; // 상세주소
    private String purchaseMsg;

    // 구매할 상품 정보 (hidden input으로 전달)
    private String[] goodsNums;
    private String[] goodsQtys;
    private String[] goodsPrices;
    private Integer totalPayment;
}