package lappick.service.review;

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

import lappick.command.PageData;
import lappick.command.ReviewCommand;
import lappick.domain.ReviewDTO;
import lappick.domain.ReviewPageDTO;
import lappick.domain.ReviewSummaryDTO;
import lappick.goods.GoodsService;
import lappick.goods.dto.GoodsResponse;
import lappick.mapper.ReviewMapper;
import lappick.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;


@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final MemberMapper memberMapper;
    private final GoodsService goodsService; 
    
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
    
    /**
     * [추가] 관리자용: 모든 리뷰 목록을 가져오는 메소드
     */
    @Transactional(readOnly = true)
    public List<ReviewDTO> getAllReviewsForAdmin() {
        return reviewMapper.findAllReviewsForAdmin();
    }

    /**
     * [추가] 관리자용: 특정 리뷰를 삭제하는 메소드 (권한 확인 없음)
     */
    @Transactional
    public void deleteReviewByAdmin(Long reviewNum) {
        // 1. 삭제할 리뷰 정보 조회 (첨부 파일 정보 확인용)
        ReviewDTO review = reviewMapper.selectReview(reviewNum);

        if (review != null) {
            // 2. 첨부된 이미지 파일이 있으면 서버에서 삭제
            if (review.getReviewImage() != null && !review.getReviewImage().isEmpty()) {
                String[] imageFiles = review.getReviewImage().split("/");
                for (String fileName : imageFiles) {
                    File file = new File(fileDir, fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
            // 3. DB에서 리뷰 데이터 삭제
            reviewMapper.deleteReview(reviewNum);
        }
    }
    


    /**
     * [교체] 관리자용: 리뷰 목록 조회 (필터링 기능 포함)
     */
    @Transactional(readOnly = true)
    public PageData<ReviewDTO> getReviewPageForAdmin(String searchWord, Integer rating, String goodsNum, int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("searchWord", searchWord);
        params.put("rating", rating);
        params.put("goodsNum", goodsNum);

        int total = reviewMapper.countReviewsForAdmin(params);

        long startRow = (page - 1L) * size + 1;
        long endRow = page * 1L * size;
        params.put("startRow", startRow);
        params.put("endRow", endRow);
        
        List<ReviewDTO> items = reviewMapper.findReviewsForAdminPaginated(params);
        
        return new PageData<>(items, page, size, total, searchWord);
    }
    
    
    /**
     * [추가] 모든 상품 목록을 가져오는 서비스 (필터링용)
     * 이제 GoodsService에 실제 구현된 메서드를 호출합니다.
     */
     public List<GoodsResponse> getAllGoods() {
        return goodsService.getAllGoodsForFilter();
     }

    /**
     * [추가] 리뷰 상태를 변경하는 서비스 (단일/일괄 처리 겸용)
     */
    @Transactional
    public void updateReviewStatus(List<Long> reviewNums, String status) {
        if (reviewNums == null || reviewNums.isEmpty()) return;
        Map<String, Object> params = new HashMap<>();
        params.put("reviewNums", reviewNums);
        params.put("status", status);
        reviewMapper.updateReviewStatus(params);
    }

    /**
     * [추가] 여러 리뷰를 삭제하는 서비스 (일괄 처리용)
     */
    @Transactional
    public void deleteReviewsByAdmin(List<Long> reviewNums) {
        if (reviewNums == null || reviewNums.isEmpty()) return;
        for (Long reviewNum : reviewNums) {
            deleteReviewByAdmin(reviewNum); // 기존의 단일 삭제 메서드 재활용
        }
    }
    
    @Transactional(readOnly = true)
    public ReviewSummaryDTO getReviewSummary(String goodsNum) {
        return reviewMapper.getReviewSummary(goodsNum);
    }

    /**
     * [추가] 특정 상품의 리뷰 목록을 페이징하여 가져오는 메소드
     */
    @Transactional(readOnly = true)
    public ReviewPageDTO findReviewsByGoodsNum(String goodsNum, int page, int size) {
        int total = reviewMapper.countReviewsByGoodsNum(goodsNum);
        int totalPages = (int) Math.ceil(total / (double) size);

        long startRow = (page - 1L) * size + 1;
        long endRow = page * 1L * size;

        Map<String, Object> params = new HashMap<>();
        params.put("goodsNum", goodsNum);
        params.put("startRow", startRow);
        params.put("endRow", endRow);

        List<ReviewDTO> items = reviewMapper.selectReviewsByGoodsNumPaginated(params);

        int paginationRange = 5; // 페이지네이션 범위
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
}