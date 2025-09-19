package mini.domain;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeListPage {
    private List<EmployeeDTO> items; // 현재 페이지의 직원 목록
    private int page;                // 현재 페이지 번호
    private int size;                // 페이지당 항목 수
    private int total;               // 전체 항목 수
    private int totalPages;          // 전체 페이지 수
    private String searchWord;       // 검색어
}