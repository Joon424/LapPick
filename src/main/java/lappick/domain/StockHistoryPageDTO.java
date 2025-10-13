package lappick.domain;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockHistoryPageDTO {
    private List<GoodsIpgoDTO> items;
    private int page;
    private int size;
    private int total;
    private int totalPages;
    private int startPage;
    private int endPage;
    private boolean hasPrev;
    private boolean hasNext;
}