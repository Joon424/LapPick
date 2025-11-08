package lappick.purchase.dto;

import java.util.Date;
import lombok.Data;

@Data
public class DeliveryResponse {
    String purchaseNum;
    String deliveryCompany;
    String trackingNumber;
    Date deliveryStartDate;
    Date deliveryCompleteDate;
}