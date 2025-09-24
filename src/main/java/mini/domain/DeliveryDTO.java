package mini.domain;

import java.util.Date;
import lombok.Data;

@Data
public class DeliveryDTO {
    String purchaseNum;
    String deliveryCompany;
    String trackingNumber;
    Date deliveryStartDate;
    Date deliveryCompleteDate;
}