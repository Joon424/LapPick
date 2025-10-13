package lappick.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import lappick.domain.ReviewDTO;
import lappick.domain.ReviewSummaryDTO;

@Repository
@Mapper
public interface ReviewMapper {
    void insertReview(ReviewDTO dto);
    // [추가] 특정 상품의 리뷰 목록 조회
    List<ReviewDTO> selectReviewsByGoodsNum(String goodsNum);
    // [추가] 특정 상품의 리뷰 요약 정보 조회
    ReviewSummaryDTO getReviewSummary(String goodsNum);
    // [추가] 특정 회원이 작성한 리뷰 개수 카운트
    int countReviewsByMemberNum(String memberNum);
    // [추가] 특정 회원이 작성한 리뷰 목록을 페이징하여 조회
    List<ReviewDTO> selectReviewsByMemberNum(java.util.Map<String, Object> params);
    
 // [추가] 삭제할 리뷰의 정보를 가져오는 메소드 (작성자 확인 및 파일 삭제용)
    ReviewDTO selectReview(Long reviewNum);

    // [추가] 리뷰를 DB에서 삭제하는 메소드
    void deleteReview(Long reviewNum);
    
 // [추가] 리뷰를 수정하는 메소드
    void updateReview(ReviewDTO dto);
 // [추가] 관리자용: 모든 리뷰 목록 조회
    List<ReviewDTO> findAllReviewsForAdmin();
    
 // [추가] 관리자용: 검색 조건에 맞는 리뷰 총 개수 카운트
    int countReviewsForAdmin(Map<String, Object> params);
    
    // [추가] 관리자용: 검색 조건에 맞는 리뷰 목록을 페이징하여 조회
    List<ReviewDTO> findReviewsForAdminPaginated(Map<String, Object> params);
    
 // [추가] 리뷰 상태를 업데이트하는 메소드
    void updateReviewStatus(Map<String, Object> params);
    
    // [추가] 상품 상세페이지의 리뷰 총 개수 카운트
    int countReviewsByGoodsNum(String goodsNum);

    // [추가] 상품 상세페이지의 리뷰 목록을 페이징하여 조회
    List<ReviewDTO> selectReviewsByGoodsNumPaginated(Map<String, Object> params);

}