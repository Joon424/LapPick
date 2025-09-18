package mini.domain;

import java.util.List;
import lombok.*;
import mini.domain.MemberDTO;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MemberListPage {
    private List<MemberDTO> items; // 목록 데이터
    private int page;              // 현재 페이지
    private int size;              // 페이지 크기
    private int total;             // 전체 건수
    private int totalPages;        // 전체 페이지 수
    private String searchWord;     // 검색어(이름/아이디)
    private long startRow;         // 조회 시작 rn
    private long endRow;           // 조회 끝 rn

    public boolean isHasPrev() { return page > 1; }
    public boolean isHasNext() { return page < totalPages; }
}
