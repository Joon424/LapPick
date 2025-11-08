package lappick.review.dto;

import java.util.List;
import lappick.review.domain.Review;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewPageResponse {
    private List<Review> items;
    private int page;
    private int size;
    private int total;
    private int totalPages;
    private int startPage;
    private int endPage;
    private boolean hasPrev;
    private boolean hasNext;
}