package lappick.review.dto;

import lombok.Data;

@Data
public class ReviewSummaryResponse {
    private int reviewCount;
    private double avgRating;
}