package mini.domain;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
public class PurchaseListPage {
    private List<PurchaseDTO> items;
    private int page;
    private int size;
    private int total;
    private int totalPages;
    private int startPage;
    private int endPage;
    private boolean hasPrev;
    private boolean hasNext;

    @Builder
    public PurchaseListPage(List<PurchaseDTO> items, int page, int size, int total) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;

        if (total > 0) {
            this.totalPages = (int) Math.ceil((double) total / size);
            int paginationRange = 5;
            this.startPage = (int) (Math.floor((page - 1.0) / paginationRange) * paginationRange + 1);
            this.endPage = Math.min(this.startPage + paginationRange - 1, this.totalPages);
            this.hasPrev = this.startPage > 1;
            this.hasNext = this.endPage < this.totalPages;
        }
    }
}