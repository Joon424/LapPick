package lappick.goods.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockHistoryPageResponse {
    private List<StockHistoryResponse> items;
    private int page;
    private int size;
    private int total;
    private int totalPages;
    private int startPage;
    private int endPage;
    private boolean hasPrev;
    private boolean hasNext;
}