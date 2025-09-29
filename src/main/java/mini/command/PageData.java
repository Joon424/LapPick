package mini.command;

import lombok.Getter;

import java.util.List;

@Getter
public class PageData<T> {

    private final List<T> items;
    private final int page;
    private final int size;
    private final long total;
    private final String searchWord; // [추가] 검색어 필드

    private final int totalPages;
    private final int startPage;
    private final int endPage;
    
    // [수정] 생성자에 searchWord 파라미터 추가
    public PageData(List<T> items, int page, int size, long total, String searchWord) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
        this.searchWord = searchWord; // [추가]

        if (total == 0) {
            this.totalPages = 0;
            this.startPage = 0;
            this.endPage = 0;
        } else {
            this.totalPages = (int) Math.ceil((double) total / size);

            int pageBlock = 5;
            this.startPage = ((page - 1) / pageBlock) * pageBlock + 1;
            int calculatedEndPage = startPage + pageBlock - 1;

            this.endPage = Math.min(calculatedEndPage, totalPages);
        }
    }
}