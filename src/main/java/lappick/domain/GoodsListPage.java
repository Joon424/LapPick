package lappick.domain;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class GoodsListPage {
    // DB 조회 결과
    private final List<GoodsDTO> items;

    // 페이징 정보
    private final int page;       // 현재 페이지
    private final int size;       // 페이지당 항목 수
    private final int total;      // 전체 항목 수
    private final int totalPages; // 전체 페이지 수

    // 페이지네이션 UI를 위한 계산 결과
    private final int startPage;  // 시작 페이지 번호
    private final int endPage;    // 끝 페이지 번호
    private final boolean hasPrev; // 이전 블록 존재 여부
    private final boolean hasNext; // 다음 블록 존재 여부

    // 검색어 (유지 목적)
    private final String searchWord;

    @Builder
    public GoodsListPage(List<GoodsDTO> items, int page, int size, int total, int totalPages, String searchWord) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = totalPages;
        this.searchWord = searchWord;

        // --- 페이지네이션 UI 계산 로직 ---
        int pageBlock = 5; // 한 번에 보여줄 페이지 번호 개수 (예: 1 2 3 4 5)
        this.startPage = (int) (Math.floor((page - 1.0) / pageBlock) * pageBlock) + 1;
        int tempEndPage = this.startPage + pageBlock - 1;
        this.endPage = Math.min(tempEndPage, totalPages);

        this.hasPrev = this.startPage > 1;
        this.hasNext = this.endPage < totalPages;
    }
}