package lappick.common.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class PageData<T> {

    private final List<T> items;
    private final int page;
    private final int size;
    private final long total;
    private final String searchWord;

    private int totalPages;
    private int startPage;
    private int endPage;

    public PageData(List<T> items, int page, int size, long total, String searchWord) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
        this.searchWord = searchWord;

        if (total == 0) {
            this.totalPages = 0;
            this.startPage = 0; // 1 대신 0으로 초기화하는 것이 더 명확
            this.endPage = 0;
        } else {
            this.totalPages = (int) Math.ceil((double) total / size);

            int pageBlock = 5; 
            this.startPage = ((page - 1) / pageBlock) * pageBlock + 1;
            int calculatedEndPage = startPage + pageBlock - 1;
            this.endPage = Math.min(calculatedEndPage, totalPages);

            if (this.startPage < 1) {
                this.startPage = 1;
            }
             // 엣지 케이스 방지 (total=0 등)
            if (this.endPage < this.startPage) {
                this.endPage = this.startPage;
            }
        }
    }

    public boolean isHasPrev() {
        return this.startPage > 1;
    }

    public boolean isHasNext() {
        return this.endPage < this.totalPages;
    }
}