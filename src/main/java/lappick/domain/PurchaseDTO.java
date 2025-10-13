package lappick.domain;

import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class PurchaseDTO {
    String purchaseNum;
    String memberNum;
    Date purchaseDate;
    Integer purchaseTotal;
    String receiverName;
    String receiverPhone;
    String purchaseAddr;
    String purchaseMsg;
    String purchaseStatus;
    
    // JOIN해서 가져올 주문 상품 목록
    List<PurchaseListDTO> purchaseList;
    // ▼▼▼ [이 코드 추가] 주문한 회원 정보를 담기 위한 변수 ▼▼▼
    private MemberDTO memberDTO;
    private DeliveryDTO deliveryDTO;
    
 // ▼▼▼ [이 코드 추가] ▼▼▼
    String paymentMethod;
    String cardCompany;
    String cardNumber;
    
    // ▼▼▼ [추가] 무통장입금 정보 필드 ▼▼▼
    private String bankName;
    private String depositorName;
    
}