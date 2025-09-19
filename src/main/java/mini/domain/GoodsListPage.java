package mini.domain;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoodsListPage {
    private List<GoodsDTO> items;
    private int page;
    private int size;
    private int total;
    private int totalPages;
    private String searchWord;
}