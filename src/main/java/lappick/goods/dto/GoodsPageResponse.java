package lappick.goods.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
public class GoodsPageResponse {

    private final List<GoodsResponse> items;

    private final int page;
    private final int size;
    private final int total;
    private final int totalPages;

    private final int startPage;
    private final int endPage;
    private final boolean hasPrev;
    private final boolean hasNext;

    private final String searchWord;

    @Builder
    public GoodsPageResponse(List<GoodsResponse> items, int page, int size, int total, int totalPages, String searchWord) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = totalPages;
        this.searchWord = searchWord;

        int pageBlock = 5;
        this.startPage = (int) (Math.floor((page - 1.0) / pageBlock) * pageBlock) + 1;
        int tempEndPage = this.startPage + pageBlock - 1;
        this.endPage = Math.min(tempEndPage, totalPages);

        this.hasPrev = this.startPage > 1;
        this.hasNext = this.endPage < totalPages;
    }
}