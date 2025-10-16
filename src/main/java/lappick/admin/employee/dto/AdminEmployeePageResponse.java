package lappick.admin.employee.dto;

import java.util.List;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AdminEmployeePageResponse { // 클래스 이름 변경
    private List<EmployeeResponse> items;
    private int page;
    private int size;
    private int total;
    private int totalPages;
    private String searchWord;
    private long startRow;
    private long endRow;

    public boolean isHasPrev() { return page > 1; }
    public boolean isHasNext() { return page < totalPages; }
}