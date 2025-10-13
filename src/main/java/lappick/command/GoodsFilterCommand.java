package lappick.command;

import lombok.Data;
import java.util.List;

// [설명] 기존 GoodsFilterDTO와 GoodsFilterCommand의 기능을 통합한 클래스입니다.
// 페이징, 검색, 상세 필터 조건을 모두 여기서 관리합니다.
@Data
public class GoodsFilterCommand {
    private int page = 1;
    private Long startRow;
    private Long endRow;
    private String searchWord;

    // [수정] 정렬 기준 필드 추가
    private String sortBy = "popular"; // 기본값 '인기순'
    
    // 필터링을 위한 필드 추가
    private List<String> goodsBrand;
    
    private Integer minPrice;
    private Integer maxPrice;
    
    private List<String> goodsPurpose;
    
    private Double minScreenSize;
    private Double maxScreenSize;
    private Double minWeight;
    private Double maxWeight;
}