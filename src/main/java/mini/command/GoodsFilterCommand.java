package mini.command;

import lombok.Data;
import java.util.List;

// [설명] 기존 GoodsFilterDTO와 GoodsFilterCommand의 기능을 통합한 클래스입니다.
// 페이징, 검색, 상세 필터 조건을 모두 여기서 관리합니다.
@Data
public class GoodsFilterCommand {
    // 1. 페이징 및 검색 정보
    private String searchWord;
    private int page = 1; // 현재 페이지 번호, 기본값 1
    private long startRow; // Service에서 계산 후 주입
    private long endRow;   // Service에서 계산 후 주입

    // 2. 상세 필터 조건 (HTML 폼에서 자동으로 바인딩)
    private List<String> brands;
    private String priceRange; // 예: "500000-1000000"
    private List<String> purposes;
    private List<Double> screenSizes;
    private String weightRange; // 예: "1.0-1.4"

    // 3. Mapper로 전달하기 위해 Service에서 가공할 필드
    private Integer minPrice;
    private Integer maxPrice;
    private Double minWeight;
    private Double maxWeight;
}