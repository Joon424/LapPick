package lappick.admin.employee.dto;

import java.util.List;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AdminEmployeePageResponse {
    private List<EmployeeResponse> items;
    private int page;
    private int size;
    private int total;
    private int totalPages;
    private String searchWord;
    private long startRow;
    private long endRow;

    private int startPage;
    private int endPage;

    public boolean isHasPrev() { return startPage > 1; }
    public boolean isHasNext() { return endPage < totalPages; }
}