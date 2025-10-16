package lappick.admin.member.dto;

import java.util.List;
import lappick.member.dto.MemberResponse;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AdminMemberPageResponse { // MemberListPage -> AdminMemberPageResponse
    private List<MemberResponse> items;
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