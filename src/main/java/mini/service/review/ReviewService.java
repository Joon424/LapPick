package mini.service.review;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mini.command.ReviewCommand;
import mini.domain.ReviewDTO;
import mini.domain.ReviewPageDTO;
import mini.mapper.MemberMapper;
import mini.mapper.ReviewMapper;


@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final MemberMapper memberMapper;
    
    @Value("${file.upload.dir}")
    private String fileDir;

    public void writeReview(ReviewCommand command) {
        ReviewDTO dto = new ReviewDTO();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String memberNum = memberMapper.memberNumSelect(auth.getName());
        
        dto.setPurchaseNum(command.getPurchaseNum());
        dto.setGoodsNum(command.getGoodsNum());
        dto.setMemberNum(memberNum);
        dto.setReviewRating(command.getReviewRating());
        dto.setReviewContent(command.getReviewContent());
        
        // [수정] 여러 이미지 파일을 처리하는 로직으로 변경
        if (command.getReviewImages() != null && command.getReviewImages().length > 0) {
            String storeFileNames = Arrays.stream(command.getReviewImages())
                .filter(mf -> mf != null && !mf.isEmpty()) // 비어있지 않은 파일만 필터링
                .map(mf -> {
                    // 각 파일에 대해 고유한 파일명 생성
                    String originalFile = mf.getOriginalFilename();
                    String extension = originalFile.substring(originalFile.lastIndexOf("."));
                    String storeName = UUID.randomUUID().toString().replace("-", "");
                    String storeFileName = storeName + extension;
                    
                    File file = new File(fileDir, storeFileName);
                    try {
                        mf.transferTo(file); // 파일 저장
                        return storeFileName; // 저장된 파일명 반환
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null; // 실패 시 null 반환
                    }
                })
                .filter(fileName -> fileName != null) // 저장 실패한 파일 제외
                .collect(Collectors.joining("/")); // 파일명을 '/'로 구분하여 하나의 문자열로 합침
            
            if (!storeFileNames.isEmpty()) {
                dto.setReviewImage(storeFileNames);
            }
        }
        
        reviewMapper.insertReview(dto);
    }
    
    /**
     * [추가] 특정 상품의 리뷰 목록과 요약 정보를 가져오는 메소드
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getReviewsForProduct(String goodsNum) {
        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("list", reviewMapper.selectReviewsByGoodsNum(goodsNum));
        reviewData.put("summary", reviewMapper.getReviewSummary(goodsNum));
        return reviewData;
    }
    
    /**
     * [추가] 특정 회원이 작성한 리뷰 목록을 페이징하여 가져오는 메소드
     */
    @Transactional(readOnly = true)
    public ReviewPageDTO getMyReviewsPage(String memberNum, int page, int size) {
        int total = reviewMapper.countReviewsByMemberNum(memberNum);
        int totalPages = (int) Math.ceil(total / (double) size);

        long startRow = (page - 1L) * size + 1;
        long endRow = page * 1L * size;
        
        Map<String, Object> params = new HashMap<>();
        params.put("memberNum", memberNum);
        params.put("startRow", startRow);
        params.put("endRow", endRow);
        
        List<ReviewDTO> items = reviewMapper.selectReviewsByMemberNum(params);
        
        int paginationRange = 5;
        int startPage = (int) (Math.floor((page - 1) / paginationRange) * paginationRange + 1);
        int endPage = Math.min(startPage + paginationRange - 1, totalPages);
        
        return ReviewPageDTO.builder()
                .items(items)
                .page(page).size(size)
                .total(total).totalPages(totalPages)
                .startPage(startPage).endPage(endPage)
                .hasPrev(startPage > 1)
                .hasNext(endPage < totalPages)
                .build();
    }
    
    /**
     * [추가] 리뷰 삭제 메소드
     */
    @Transactional
    public void deleteReview(Long reviewNum) {
        // 1. 현재 로그인한 사용자의 memberNum 조회
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentMemberNum = memberMapper.memberNumSelect(auth.getName());
        
        // 2. 삭제할 리뷰 정보 조회 (작성자 memberNum 포함)
        ReviewDTO review = reviewMapper.selectReview(reviewNum);
        
        // 3. 본인이 작성한 리뷰가 맞는지 확인
        if (review == null || !review.getMemberNum().equals(currentMemberNum)) {
            throw new SecurityException("리뷰를 삭제할 권한이 없습니다.");
        }
        
        // 4. 첨부된 이미지 파일이 있으면 서버에서 삭제
        if (review.getReviewImage() != null && !review.getReviewImage().isEmpty()) {
            String[] imageFiles = review.getReviewImage().split("/");
            for (String fileName : imageFiles) {
                File file = new File(fileDir, fileName);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
        
        // 5. DB에서 리뷰 데이터 삭제
        reviewMapper.deleteReview(reviewNum);
    }
    
    /**
     * [추가] 수정할 리뷰 정보를 가져오는 메소드 (작성자 확인 포함)
     */
    @Transactional(readOnly = true)
    public ReviewDTO getReviewForEdit(Long reviewNum) {
        // 1. 현재 로그인한 사용자의 memberNum 조회
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentMemberNum = memberMapper.memberNumSelect(auth.getName());

        // 2. 수정할 리뷰 정보 조회
        ReviewDTO review = reviewMapper.selectReview(reviewNum);

        // 3. 본인이 작성한 리뷰가 맞는지 확인
        if (review == null || !review.getMemberNum().equals(currentMemberNum)) {
            throw new SecurityException("리뷰를 수정할 권한이 없습니다.");
        }
        
        return review;
    }
    
    /**
     * [추가] 리뷰 수정 처리 메소드
     */
    @Transactional
    public void updateReview(ReviewCommand command, Long reviewNum, String[] imagesToDelete) {
        // 1. 현재 로그인한 사용자의 memberNum과 수정할 리뷰의 정보를 가져옴 (권한 확인 포함)
        ReviewDTO review = getReviewForEdit(reviewNum); // 기존 메소드 재활용

        // 2. Command 객체의 새로운 정보로 DTO를 업데이트
        review.setReviewRating(command.getReviewRating());
        review.setReviewContent(command.getReviewContent());

        // 3. 이미지 파일 처리
        List<String> remainingImages = new ArrayList<>();
        // 3-1. 기존 이미지가 있었으면, 삭제되지 않은 이미지만 remainingImages 리스트에 추가
        if (review.getReviewImage() != null && !review.getReviewImage().isEmpty()) {
            List<String> existingImages = new ArrayList<>(Arrays.asList(review.getReviewImage().split("/")));
            if (imagesToDelete != null) {
                for (String toDelete : imagesToDelete) {
                    existingImages.remove(toDelete);
                    // 실제 서버에서 파일 삭제
                    File file = new File(fileDir, toDelete);
                    if (file.exists()) file.delete();
                }
            }
            remainingImages.addAll(existingImages);
        }

        // 3-2. 새로 추가된 이미지가 있으면 업로드하고 remainingImages 리스트에 추가
        if (command.getReviewImages() != null && command.getReviewImages().length > 0) {
            Arrays.stream(command.getReviewImages())
                .filter(mf -> mf != null && !mf.isEmpty())
                .forEach(mf -> {
                    String originalFile = mf.getOriginalFilename();
                    String extension = originalFile.substring(originalFile.lastIndexOf("."));
                    String storeName = UUID.randomUUID().toString().replace("-", "");
                    String storeFileName = storeName + extension;
                    
                    File file = new File(fileDir, storeFileName);
                    try {
                        mf.transferTo(file);
                        remainingImages.add(storeFileName); // 업로드 성공 시 리스트에 추가
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        }
        
        // 3-3. 최종 이미지 목록을 '/'로 구분된 문자열로 변환하여 DTO에 설정
        review.setReviewImage(String.join("/", remainingImages));

        // 4. DB에 최종 리뷰 정보 업데이트
        reviewMapper.updateReview(review);
    }
    
}