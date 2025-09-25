package mini.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import mini.domain.DeliveryDTO;
import mini.domain.PurchaseDTO;
import mini.domain.PurchaseListDTO;

@Mapper
public interface PurchaseMapper {
    public void insertPurchase(PurchaseDTO dto);
    public void insertPurchaseList(PurchaseListDTO dto);
    public List<PurchaseDTO> selectPurchaseList(Map<String, Object> params);
    public int countMyOrders(Map<String, Object> params);
    public List<PurchaseDTO> selectAllPurchases(Map<String, Object> params);
    public int countAllPurchases(Map<String, Object> params);
    public void updatePurchaseStatus(@Param("purchaseNum") String purchaseNum, @Param("status") String status);
 // ▼▼▼ [이 코드 추가] ▼▼▼
    public void insertDelivery(DeliveryDTO dto);
    public PurchaseDTO selectPurchaseDetail(String purchaseNum);
    List<PurchaseListDTO> selectPurchasedItemsByMemberNum(String memberNum);

}