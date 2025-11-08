package lappick.purchase.dto;

import lombok.Data;

@Data
public class DeliveryRequest {
    private String purchaseNum;
    private String deliveryCompany;
    private String deliveryNum;
}