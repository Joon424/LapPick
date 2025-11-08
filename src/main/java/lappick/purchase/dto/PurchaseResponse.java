package lappick.purchase.dto;

import java.util.Date;
import java.util.List;
import lappick.member.dto.MemberResponse;
import lombok.Data;

@Data
public class PurchaseResponse {
    String purchaseNum;
    String memberNum;
    Date purchaseDate;
    Integer purchaseTotal;
    String receiverName;
    String receiverPhone;
    String purchaseAddr;
    String purchaseMsg;
    String purchaseStatus;
    String paymentMethod;
    String cardCompany;
    String cardNumber;
    private String bankName;
    private String depositorName;
    private List<PurchaseItemResponse> purchaseItems;
    private MemberResponse member;
    private DeliveryResponse delivery;
}