package mini.command;

import java.util.List;
import lombok.Data;

@Data
public class GoodsFilterDTO {
    // 페이징 정보
    private long startRow;
    private long endRow;
    
    // 필터 정보
    private String searchWord;
    private List<String> brands;
    private Integer minPrice;
    private Integer maxPrice;
    // 여기에 용도, 무게 등 다른 필드를 추가할 수 있습니다.
}