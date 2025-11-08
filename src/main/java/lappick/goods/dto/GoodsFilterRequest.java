package lappick.goods.dto;

import lombok.Data;
import java.util.List;

@Data
public class GoodsFilterRequest {
    private int page = 1;
    private Long startRow;
    private Long endRow;
    private String searchWord;

    private String sortBy = "popular";
    
    private List<String> goodsBrand;
    
    private Integer minPrice;
    private Integer maxPrice;
    
    private List<String> goodsPurpose;
    
    private Double minScreenSize;
    private Double maxScreenSize;
    private Double minWeight;
    private Double maxWeight;
}