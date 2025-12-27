package lappick.review.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lappick.common.dto.PageData;
import lappick.goods.dto.GoodsResponse;
import lappick.goods.service.GoodsService;
import lappick.member.mapper.MemberMapper;
import lappick.purchase.mapper.PurchaseMapper;
import lappick.review.domain.Review;
import lappick.review.dto.ReviewPageResponse;
import lappick.review.dto.ReviewSummaryResponse;
import lappick.review.dto.ReviewWriteRequest;
import lappick.review.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewMapper reviewMapper;
    private final MemberMapper memberMapper;
    private final GoodsService goodsService;
    private final PurchaseMapper purchaseMapper;

    @Value("${file.upload.dir}")
    private String fileDir;

    public void writeReview(ReviewWriteRequest command, String userId) {

        // 0) 인증 사용자 → memberNum 조회
        String memberNum = memberMapper.memberNumSelect(userId);
        if (memberNum == null) {
            throw new SecurityException("사용자를 찾을 수 없거나 인증 정보가 올바르지 않습니다.");
        }

        // 1) 필수 파라미터 방어 (DTO에 @NotBlank가 없으므로 서버에서 1차 방어)
        String purchaseNum = command.getPurchaseNum();
        String goodsNum = command.getGoodsNum();

        if (purchaseNum == null || purchaseNum.isBlank() || goodsNum == null || goodsNum.isBlank()) {
            throw new IllegalArgumentException("주문번호 또는 상품번호가 올바르지 않습니다.");
        }

        // 2) 배송완료 + 구매자 본인 + 해당 주문에 해당 상품 포함 검증
        // (2차에서 추가한 purchaseMapper.countDeliveredPurchaseItemByMember(...) 사용)
        int eligibleCount = purchaseMapper.countDeliveredPurchaseItemByMember(purchaseNum, goodsNum, memberNum);
        if (eligibleCount <= 0) {
            throw new IllegalStateException("배송완료된 구매 항목만 리뷰 작성이 가능합니다.");
        }

        // 3) 중복 리뷰 검증 (1차에서 추가한 reviewMapper.countReviewsByPurchaseGoodsMember(...) 사용)
        int duplicateCount = reviewMapper.countReviewsByPurchaseGoodsMember(purchaseNum, goodsNum, memberNum);
        if (duplicateCount > 0) {
            throw new IllegalStateException("이미 작성한 리뷰입니다.");
        }

        // 4) 여기까지 통과하면 실제 리뷰 저장 진행
        Review dto = new Review();
        dto.setPurchaseNum(purchaseNum);
        dto.setGoodsNum(goodsNum);
        dto.setMemberNum(memberNum);
        dto.setReviewRating(command.getReviewRating());
        dto.setReviewContent(command.getReviewContent());
        dto.setReviewStatus("PUBLISHED"); // 기존 정책 유지

        // 이미지 업로드(검증 통과 후에만 수행 → 불필요 업로드 방지)
        if (command.getReviewImages() != null && command.getReviewImages().length > 0) {
            String storeFileNames = Arrays.stream(command.getReviewImages())
                .filter(mf -> mf != null && !mf.isEmpty())
                .map(this::uploadFile)
                .filter(fileName -> fileName != null)
                .collect(Collectors.joining("/"));

            if (!storeFileNames.isEmpty()) {
                dto.setReviewImage(storeFileNames);
            }
        }

        reviewMapper.insertReview(dto);

        log.info("리뷰 저장 완료: reviewNum={}, goodsNum={}, memberNum={}", dto.getReviewNum(), dto.getGoodsNum(), dto.getMemberNum());
    }


    // ===== 파일 처리 헬퍼 =====

    /**
     * 단일 파일 업로드 헬퍼
     * @param mf MultipartFile
     * @return 저장된 파일명 (실패 시 null)
     */
    private String uploadFile(MultipartFile mf) {
        if (mf == null || mf.isEmpty()) {
            return null;
        }
        String originalFile = mf.getOriginalFilename();
        String extension = "";
        int lastDot = originalFile.lastIndexOf(".");
        if (lastDot > 0) {
            extension = originalFile.substring(lastDot);
        }
        String storeName = UUID.randomUUID().toString().replace("-", "");
        String storeFileName = storeName + extension;
        File file = new File(fileDir, storeFileName);
        try {
            // 업로드 경로에 디렉토리가 존재하지 않으면 생성합니다.
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            mf.transferTo(file);
            log.debug("파일 업로드 성공: {}", storeFileName);
            return storeFileName;
        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", originalFile, e);
            return null;
        }
    }

    /**
     * 단일 파일 삭제 헬퍼
     * @param storeFileName 서버에 저장된 파일명
     */
    private void deleteFile(String storeFileName) {
        if (storeFileName == null || storeFileName.isBlank()) return;
        File file = new File(fileDir, storeFileName);
        if (file.exists()) {
            if(file.delete()) {
                log.debug("파일 삭제 성공: {}", storeFileName);
            } else {
                log.warn("파일 삭제 실패: {}", storeFileName);
            }
        } else {
             log.warn("삭제할 파일 없음: {}", storeFileName);
        }
    }

    /**
     * 리뷰 관련(DB에 저장된) 모든 파일 삭제 헬퍼
     */
    private void deleteReviewFiles(Review review) {
         if (review.getReviewImage() != null && !review.getReviewImage().isEmpty()) {
            Arrays.stream(review.getReviewImage().split("/"))
                  .forEach(this::deleteFile);
        }
    }
    
    // ===== 사용자 기능 (MyPage, Goods) =====

    @Transactional(readOnly = true)
    public Map<String, Object> getReviewsForProduct(String goodsNum) {
        Map<String, Object> reviewData = new HashMap<>();
        // selectReviewsByGoodsNum은 '게시됨' 상태의 리뷰만 조회한다고 가정합니다.
        reviewData.put("list", reviewMapper.selectReviewsByGoodsNum(goodsNum));
        reviewData.put("summary", reviewMapper.getReviewSummary(goodsNum));
        return reviewData;
    }

    @Transactional(readOnly = true)
    public ReviewPageResponse getMyReviewsPage(String memberNum, int page, int size) {
        int total = reviewMapper.countReviewsByMemberNum(memberNum);
        Map<String, Object> params = createPaginationParams(memberNum, page, size);
        List<Review> items = reviewMapper.selectReviewsByMemberNum(params);
        return buildPageResponse(items, page, size, total);
    }

    @Transactional
    public void deleteReview(Long reviewNum) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentMemberNum = memberMapper.memberNumSelect(auth.getName());
        Review review = reviewMapper.selectReview(reviewNum);

        if (review == null) {
             log.warn("삭제할 리뷰 없음: reviewNum={}", reviewNum);
             throw new IllegalArgumentException("삭제할 리뷰를 찾을 수 없습니다.");
        }
        // 본인 확인
        if (!review.getMemberNum().equals(currentMemberNum)) {
             log.warn("리뷰 삭제 권한 없음: reviewNum={}, memberNum={}, currentMemberNum={}", reviewNum, review.getMemberNum(), currentMemberNum);
             throw new SecurityException("리뷰를 삭제할 권한이 없습니다.");
        }

        deleteReviewFiles(review);
        reviewMapper.deleteReview(reviewNum);
        log.info("리뷰 삭제 완료: reviewNum={}", reviewNum);
    }

    @Transactional(readOnly = true)
    public Review getReviewForEdit(Long reviewNum) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentMemberNum = memberMapper.memberNumSelect(auth.getName());
        Review review = reviewMapper.selectReview(reviewNum);

        if (review == null) {
            log.warn("수정할 리뷰 없음: reviewNum={}", reviewNum);
            throw new IllegalArgumentException("수정할 리뷰를 찾을 수 없습니다.");
        }
        // 본인 확인
        if (!review.getMemberNum().equals(currentMemberNum)) {
            log.warn("리뷰 수정 권한 없음: reviewNum={}, memberNum={}, currentMemberNum={}", reviewNum, review.getMemberNum(), currentMemberNum);
            throw new SecurityException("리뷰를 수정할 권한이 없습니다.");
        }
        return review;
    }

    @Transactional
    public void updateReview(ReviewWriteRequest command, Long reviewNum, String[] imagesToDelete) {
        // getReviewForEdit을 호출하여 수정 권한을 먼저 확인합니다. (권한 없으면 예외 발생)
        Review review = getReviewForEdit(reviewNum);

        review.setReviewRating(command.getReviewRating());
        review.setReviewContent(command.getReviewContent());

        List<String> remainingImages = new ArrayList<>();
        
        // 1. 기존 이미지 처리 (삭제할 이미지 제거)
        if (review.getReviewImage() != null && !review.getReviewImage().isEmpty()) {
            List<String> existingImages = new ArrayList<>(Arrays.asList(review.getReviewImage().split("/")));
            if (imagesToDelete != null) {
                for (String toDelete : imagesToDelete) {
                    if (existingImages.remove(toDelete)) {
                         deleteFile(toDelete); // 서버에서 실제 파일 삭제
                         log.debug("삭제 요청된 이미지 파일 삭제: {}", toDelete);
                    }
                }
            }
            remainingImages.addAll(existingImages);
        }

        // 2. 새 이미지 처리 (새로 추가된 이미지 업로드)
        if (command.getReviewImages() != null && command.getReviewImages().length > 0) {
            Arrays.stream(command.getReviewImages())
                .filter(mf -> mf != null && !mf.isEmpty())
                .map(this::uploadFile)
                .filter(fileName -> fileName != null)
                .forEach(remainingImages::add);
        }

        // 3. 최종 이미지 목록을 '/' 구분자로 합쳐서 DB에 저장
        review.setReviewImage(String.join("/", remainingImages));
        reviewMapper.updateReview(review);
        log.info("리뷰 수정 완료: reviewNum={}", reviewNum);
    }

    // --- 관리자 기능 ---

    @Transactional(readOnly = true)
    public PageData<Review> getReviewPageForAdmin(String searchWord, Integer rating, String goodsNum, int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("searchWord", searchWord);
        params.put("rating", rating);
        params.put("goodsNum", goodsNum);

        int total = reviewMapper.countReviewsForAdmin(params);

        long startRow = (page - 1L) * size + 1;
        long endRow = page * 1L * size;
        params.put("startRow", startRow);
        params.put("endRow", endRow);

        List<Review> items = reviewMapper.findReviewsForAdminPaginated(params);

        // PageData는 제네릭을 사용하여 List<Review>를 포함하는 DTO입니다.
        return new PageData<>(items, page, size, total, searchWord);
    }


    @Transactional(readOnly = true)
    public List<GoodsResponse> getAllGoods() {
        // goodsService에서 제공하는, 필터링용 상품 목록 조회 메소드를 사용합니다.
        return goodsService.getAllGoodsForFilter();
    }

    @Transactional
    public void updateReviewStatus(List<Long> reviewNums, String status) {
        if (reviewNums == null || reviewNums.isEmpty()) return;
        Map<String, Object> params = new HashMap<>();
        params.put("reviewNums", reviewNums);
        params.put("status", status);
        int updatedCount = reviewMapper.updateReviewStatus(params);
        log.info("{}개 리뷰 상태 변경 완료: status={}, reviewNums={}", updatedCount, status, reviewNums);
    }

    @Transactional
    public void deleteReviewsByAdmin(List<Long> reviewNums) {
        if (reviewNums == null || reviewNums.isEmpty()) return;
        int deletedCount = 0;
        for (Long reviewNum : reviewNums) {
            Review review = reviewMapper.selectReview(reviewNum);
            if (review != null) {
                deleteReviewFiles(review); // 관련 파일 삭제
                reviewMapper.deleteReview(reviewNum); // DB에서 삭제
                deletedCount++;
            } else {
                log.warn("관리자 삭제: 삭제할 리뷰 없음 (이미 삭제됨?): reviewNum={}", reviewNum);
            }
        }
         log.info("관리자 리뷰 삭제 완료: {}/{} 건", deletedCount, reviewNums.size());
    }

    // --- 상품 상세 페이지 메소드 ---

    @Transactional(readOnly = true)
    public ReviewSummaryResponse getReviewSummary(String goodsNum) {
        return reviewMapper.getReviewSummary(goodsNum);
    }

    @Transactional(readOnly = true)
    public ReviewPageResponse findReviewsByGoodsNum(String goodsNum, int page, int size) {
        int total = reviewMapper.countReviewsByGoodsNum(goodsNum);
        Map<String, Object> params = createPaginationParams(goodsNum, page, size);
        List<Review> items = reviewMapper.selectReviewsByGoodsNumPaginated(params);
        return buildPageResponse(items, page, size, total);
    }

    // --- 헬퍼 메소드 ---

    /**
     * 페이지네이션 파라미터 맵 생성 헬퍼
     * (MyBatis에서 startRow, endRow 및 식별자(memberNum 또는 goodsNum)를 전달하기 위해 사용)
     */
    private Map<String, Object> createPaginationParams(String identifier, int page, int size) {
        long startRow = (page - 1L) * size + 1;
        long endRow = page * 1L * size;
        Map<String, Object> params = new HashMap<>();
        
        // TODO: memberNum과 goodsNum을 식별하는 더 나은 방법 고안 필요
        // 현재는 getMyReviewsPage (memberNum)와 findReviewsByGoodsNum (goodsNum)에서 사용
        if (identifier != null && identifier.startsWith("goods_")) {
             params.put("goodsNum", identifier);
             log.trace("Pagination params for goodsNum: {}", identifier);
        } else if (identifier != null) {
             params.put("memberNum", identifier);
             log.trace("Pagination params for memberNum: {}", identifier);
        }
        params.put("startRow", startRow);
        params.put("endRow", endRow);
        return params;
    }


    /**
     * ReviewPageResponse 빌드 헬퍼
     */
    private ReviewPageResponse buildPageResponse(List<Review> items, int page, int size, int total) {
        int totalPages = (total > 0) ? (int) Math.ceil((double) total / size) : 0;
        int paginationRange = 5;
        // 시작 페이지 계산: 1보다 작아지지 않도록
        int startPage = Math.max(1, (int) (Math.floor((page - 1.0) / paginationRange) * paginationRange + 1));
        // 끝 페이지 계산: totalPages를 넘지 않도록
        int endPage = Math.min(totalPages, startPage + paginationRange - 1);

        // endPage가 startPage보다 작아지는 경우 방지 (total=0 또는 totalPages=0일 때)
        if (endPage < startPage) {
            endPage = startPage;
        }

        return ReviewPageResponse.builder()
                .items(items)
                .page(page).size(size).total(total)
                .totalPages(totalPages).startPage(startPage).endPage(endPage)
                .hasPrev(startPage > 1).hasNext(endPage < totalPages)
                .build();
    }
}