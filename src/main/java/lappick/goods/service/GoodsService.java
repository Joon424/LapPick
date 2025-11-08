package lappick.goods.service;

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
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lappick.admin.employee.mapper.EmployeeMapper;
import lappick.common.dto.FileDTO;
import lappick.common.dto.PageData;
import lappick.goods.dto.GoodsResponse;
import lappick.goods.dto.GoodsSalesResponse;
import lappick.goods.dto.GoodsStockResponse;
import lappick.goods.dto.StockHistoryPageResponse;
import lappick.goods.dto.GoodsFilterRequest;
import lappick.goods.dto.StockHistoryResponse;
import lappick.goods.mapper.GoodsMapper;
import lappick.goods.dto.GoodsPageResponse;
import lappick.goods.dto.GoodsRequest;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class GoodsService {

    @Value("${file.upload.dir}")
    private String fileDir;
	
    private final GoodsMapper goodsMapper;
    private final EmployeeMapper employeeMapper;

    @Transactional(readOnly = true)
    public GoodsPageResponse getGoodsListPage(GoodsFilterRequest filter, int limit) {
        long startRow = (filter.getPage() - 1L) * limit + 1;
        long endRow = filter.getPage() * 1L * limit;
        filter.setStartRow(startRow);
        filter.setEndRow(endRow);

        List<GoodsResponse> list = goodsMapper.allSelect(filter);
        int total = goodsMapper.goodsCount(filter);
        int totalPages = (int) Math.ceil(total / (double) limit);

        return GoodsPageResponse.builder()
                .items(list)
                .page(filter.getPage()).size(limit)
                .total(total).totalPages(totalPages)
                .searchWord(filter.getSearchWord())
                .build();
    }

    @Transactional(readOnly = true)
    public GoodsResponse getGoodsDetail(String goodsNum) {
        return goodsMapper.selectOne(goodsNum);
    }

    public void createGoods(GoodsRequest command) {
        GoodsResponse dto = new GoodsResponse();
        
        // 1. 파일 업로드 제한
        if (command.getGoodsDetailImage() != null && command.getGoodsDetailImage().length > 10) {
            throw new IllegalArgumentException("상세이미지(썸네일)는 최대 10개까지 등록 가능합니다.");
        }
        if (command.getGoodsDetail() != null && command.getGoodsDetail().length > 5) {
            throw new IllegalArgumentException("상세 설명 이미지는 최대 5개까지 등록 가능합니다.");
        }
        
        dto.setGoodsNum(command.getGoodsNum());
        dto.setGoodsName(command.getGoodsName());
        dto.setGoodsPrice(command.getGoodsPrice());
        dto.setGoodsContents(command.getGoodsContents());
        dto.setGoodsBrand(command.getGoodsBrand());
        dto.setGoodsPurpose(command.getGoodsPurpose());
        
        // 3. 소수점 자동 변환 (Integer -> Double)
        if (command.getGoodsScreenSize() != null) {
            dto.setGoodsScreenSize(command.getGoodsScreenSize() / 10.0); // 156 -> 15.6
        }
        if (command.getGoodsWeight() != null) {
            dto.setGoodsWeight(command.getGoodsWeight() / 100.0); // 125 -> 1.25
        }

        dto.setGoodsKeyword1(command.getGoodsKeyword1());
        dto.setGoodsKeyword2(command.getGoodsKeyword2());
        dto.setGoodsKeyword3(command.getGoodsKeyword3());

        // 5. 배송 정보 기본값
        if (command.getGoodsShippingInfo() == null || command.getGoodsShippingInfo().isBlank()) {
            dto.setGoodsShippingInfo("999,000원 이상 무료배송");
        } else {
            dto.setGoodsShippingInfo(command.getGoodsShippingInfo());
        }
        
        dto.setGoodsSellerInfo(command.getGoodsSellerInfo());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String empNum = employeeMapper.getEmpNum(auth.getName());
        dto.setEmpNum(empNum);

        // 6. 대표/상세 썸네일 통합
        List<String> detailStoreImages = new ArrayList<>();
        
        FileDTO mainImage = uploadFile(command.getGoodsMainImage());
        if (mainImage != null) {
            dto.setGoodsMainImage(mainImage.getOrgFile());
            dto.setGoodsMainStoreImage(mainImage.getStoreFile());
            detailStoreImages.add(mainImage.getStoreFile());
        }
        
        if (command.getGoodsDetailImage() != null && command.getGoodsDetailImage().length > 0) {
            Arrays.stream(command.getGoodsDetailImage())
                  .filter(f -> f != null && !f.isEmpty())
                  .map(this::uploadFile)
                  .map(FileDTO::getStoreFile)
                  .forEach(detailStoreImages::add);
        }
        
        dto.setGoodsDetailStoreImage(String.join("/", detailStoreImages));
        dto.setGoodsDetailStore(uploadMultipleFiles(command.getGoodsDetail()));
        
        goodsMapper.goodsInsert(dto);
        
        if (command.getInitialStock() != null && command.getInitialStock() > 0) {
            goodsMapper.insertGoodsIpgo(command.getGoodsNum(), command.getInitialStock(), "신규 등록");
        }
    }
    
    private String uploadMultipleFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return null;
        }
        return Arrays.stream(files)
                     .filter(f -> f != null && !f.isEmpty())
                     .map(this::uploadFile)
                     .map(FileDTO::getStoreFile)
                     .collect(Collectors.joining("/"));
    }
    
    public void updateGoods(GoodsRequest command, List<String> imagesToDelete,
            List<String> detailDescImagesToDelete) {

        // 1. 파일 업로드 제한
        int existingDetailCount = 0;
        if(command.getGoodsDetailImage() != null) { 
        existingDetailCount += command.getGoodsDetailImage().length;
        }
        
        GoodsResponse dto = goodsMapper.selectOne(command.getGoodsNum());
        
        if(dto.getGoodsDetailStoreImage() != null && !dto.getGoodsDetailStoreImage().isEmpty()) { 
        List<String> existingList = new ArrayList<>(Arrays.asList(dto.getGoodsDetailStoreImage().split("/")));
        if(imagesToDelete != null) { 
          existingList.removeAll(imagesToDelete);
        }
        existingDetailCount += existingList.size();
        }
        
        if (existingDetailCount > 11) { 
        throw new IllegalArgumentException("상세이미지(썸네일)는 최대 10개 (대표 이미지 포함 총 11개)까지 등록 가능합니다.");
        }
        if (command.getGoodsDetail() != null && command.getGoodsDetail().length > 5) {
        throw new IllegalArgumentException("상세 설명 이미지는 최대 5개까지 등록 가능합니다.");
        }
        
        dto.setGoodsName(command.getGoodsName());
        dto.setGoodsPrice(command.getGoodsPrice());
        dto.setGoodsContents(command.getGoodsContents());
        dto.setGoodsBrand(command.getGoodsBrand());
        dto.setGoodsPurpose(command.getGoodsPurpose());
        
        // 3. 소수점 자동 변환 (Integer -> Double)
        if (command.getGoodsScreenSize() != null) {
        dto.setGoodsScreenSize(command.getGoodsScreenSize() / 10.0); // 156 -> 15.6
        }
        if (command.getGoodsWeight() != null) {
        dto.setGoodsWeight(command.getGoodsWeight() / 100.0); // 125 -> 1.25
        }
        
        dto.setGoodsKeyword1(command.getGoodsKeyword1());
        dto.setGoodsKeyword2(command.getGoodsKeyword2());
        dto.setGoodsKeyword3(command.getGoodsKeyword3());
        
        // 5. 배송 정보 기본값
        if (command.getGoodsShippingInfo() == null || command.getGoodsShippingInfo().isBlank()) {
        dto.setGoodsShippingInfo("999,000원 이상 무료배송");
        } else {
        dto.setGoodsShippingInfo(command.getGoodsShippingInfo());
        }
        
        dto.setGoodsSellerInfo(command.getGoodsSellerInfo());
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String empNum = employeeMapper.getEmpNum(auth.getName());
        dto.setUpdateEmpNum(empNum);
        
        // 6. 대표/상세 썸네일 통합
        String newMainStoreImage = dto.getGoodsMainStoreImage(); 
        
        if (command.getGoodsMainImage() != null && !command.getGoodsMainImage().isEmpty()) {
        deleteFile(dto.getGoodsMainStoreImage()); 
        FileDTO mainImage = uploadFile(command.getGoodsMainImage());
        dto.setGoodsMainImage(mainImage.getOrgFile());
        dto.setGoodsMainStoreImage(mainImage.getStoreFile());
        newMainStoreImage = mainImage.getStoreFile(); 
        }
        
        dto.setGoodsDetailStoreImage(updateMultipleFiles(
          dto.getGoodsDetailStoreImage(), 
          imagesToDelete, 
          command.getGoodsDetailImage(),
          newMainStoreImage 
        ));
        
        dto.setGoodsDetailStore(updateMultipleFiles(
          dto.getGoodsDetailStore(), 
          detailDescImagesToDelete, 
          command.getGoodsDetail(),
          null 
        ));
        
        goodsMapper.goodsUpdate(dto);
    }
    

    private String updateMultipleFiles(String existingFileNames, List<String> filesToDelete, MultipartFile[] newFiles, String firstImage) {
        List<String> fileList = new ArrayList<>();
        
        // 1. 1순위 이미지(대표 이미지)가 있으면 먼저 추가
        if (firstImage != null && !firstImage.isBlank()) {
            fileList.add(firstImage);
        }

        // 2. 기존 이미지 처리
        if (existingFileNames != null && !existingFileNames.isEmpty()) {
            Arrays.stream(existingFileNames.split("/"))
                  .filter(img -> {
                      // 1순위 이미지(대표 이미지)와 중복되거나, 삭제 목록에 있으면 제외
                      boolean isFirstImage = img.equals(firstImage);
                      boolean isToDelete = (filesToDelete != null && filesToDelete.contains(img));
                      
                      if (isToDelete && !isFirstImage) { // (대표 이미지가 아닌데 삭제 목록에 있으면)
                          deleteFile(img); // 서버에서 파일 삭제
                          return false; // 리스트에 추가 안 함
                      }
                      
                      return !isFirstImage && !isToDelete; // 대표이미지가 아니고, 삭제목록에도 없으면 리스트에 추가
                  })
                  .forEach(fileList::add);
        }
        
        // 3. 새 이미지 처리
        if (newFiles != null && newFiles.length > 0) {
            Arrays.stream(newFiles)
                  .filter(f -> f != null && !f.isEmpty())
                  .map(this::uploadFile)
                  .forEach(fileDTO -> fileList.add(fileDTO.getStoreFile()));
        }

        // 4. 중복 제거 (대표 이미지가 썸네일 목록에 중복으로 들어가는 것 방지)
        return fileList.stream().distinct().collect(Collectors.joining("/"));
    }
    
    private String updateMultipleFiles(String existingFileNames, List<String> filesToDelete, MultipartFile[] newFiles) {
        List<String> fileList = new ArrayList<>();
        if (existingFileNames != null && !existingFileNames.isEmpty()) {
            fileList.addAll(Arrays.asList(existingFileNames.split("/")));
        }

        if (filesToDelete != null) {
            for (String fileToDelete : filesToDelete) {
                if (fileList.remove(fileToDelete)) {
                    deleteFile(fileToDelete);
                }
            }
        }
        
        if (newFiles != null && newFiles.length > 0) {
            Arrays.stream(newFiles)
                  .filter(f -> f != null && !f.isEmpty())
                  .map(this::uploadFile)
                  .forEach(fileDTO -> fileList.add(fileDTO.getStoreFile()));
        }

        return fileList.stream().collect(Collectors.joining("/"));
    }
    
    @Transactional
    public void deleteGoods(String[] nums) {
        List<String> goodsNumList = Arrays.asList(nums);
        List<GoodsResponse> goodsForDelete = goodsMapper.selectGoodsByNumList(goodsNumList);

        // 1. 이미지 파일 삭제
        for (GoodsResponse goods : goodsForDelete) {
            if (goods.getGoodsMainStoreImage() != null) {
                File file = new File(fileDir + "/" + goods.getGoodsMainStoreImage());
                if (file.exists()) file.delete();
            }
            if (goods.getGoodsDetailStoreImage() != null) {
                for (String fileName : goods.getGoodsDetailStoreImage().split("/")) {
                    if (!fileName.isEmpty()) {
                        File file = new File(fileDir + "/" + fileName);
                        if (file.exists()) file.delete();
                    }
                }
            }
            if (goods.getGoodsDetailStore() != null) {
                for (String fileName : goods.getGoodsDetailStore().split("/")) {
                    if (!fileName.isEmpty()) {
                        File file = new File(fileDir + "/" + fileName);
                        if (file.exists()) file.delete();
                    }
                }
            }
        }

        // 관련 재고 이력(GOODS_IPGO) 먼저 삭제
        goodsMapper.deleteGoodsIpgo(goodsNumList);

        // 3. 상품 정보(GOODS) 삭제
        goodsMapper.goodsDelete(goodsNumList);
    }

    private String getUploadDirectory() {
        // 이 메서드는 현재 사용되지 않음 (fileDir 필드 사용)
        throw new UnsupportedOperationException("This method is deprecated.");
    }
    
    private void deleteFile(String storeFileName) {
        if (storeFileName == null || storeFileName.isBlank()) return;
        File file = new File(fileDir, storeFileName);
        if (file.exists()) {
            file.delete();
        }
    }
    
    private FileDTO uploadFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }
        String originalFile = multipartFile.getOriginalFilename();
        String extension = originalFile.substring(originalFile.lastIndexOf("."));
        String storeName = UUID.randomUUID().toString().replace("-", "");
        String storeFileName = storeName + extension;
        File file = new File(fileDir, storeFileName);
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            multipartFile.transferTo(file);
            return new FileDTO(originalFile, storeFileName);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Transactional
    public void changeStock(String goodsNum, int quantity, String memo) {
        if (quantity == 0) {
            throw new IllegalArgumentException("변경 수량은 0이 될 수 없습니다.");
        }
        
        String finalMemo = memo;

        // 입고(양수) 시 메모가 없으면 기본값 설정
        if (quantity > 0 && (memo == null || memo.isBlank())) {
            finalMemo = "정기 입고";
        } 
        // 출고(음수) 시 메모가 없으면 오류 발생
        else if (quantity < 0 && (memo == null || memo.isBlank())) {
            throw new IllegalArgumentException("재고 차감 시에는 변경 사유를 반드시 입력해야 합니다.");
        }
        
        goodsMapper.insertGoodsIpgo(goodsNum, quantity, finalMemo);
    }
    
    @Transactional(readOnly = true)
    public GoodsStockResponse getGoodsDetailWithStock(String goodsNum) {
        return goodsMapper.selectOneWithStock(goodsNum);
    }
    
    /**
     * 특정 상품의 재고 변경 이력을 페이징하여 가져오는 메소드
     */
    @Transactional(readOnly = true)
    public StockHistoryPageResponse getStockHistoryPage(String goodsNum, int page, int size) {
        // 1. 전체 이력 개수 조회
        int total = goodsMapper.countIpgoHistory(goodsNum);
        int totalPages = (int) Math.ceil(total / (double) size);

        // 2. 페이징을 위한 시작/끝 행 번호 계산
        long startRow = (page - 1L) * size + 1;
        long endRow = page * 1L * size;
        
        Map<String, Object> params = new HashMap<>();
        params.put("goodsNum", goodsNum);
        params.put("startRow", startRow);
        params.put("endRow", endRow);
        
        // 3. DB에서 페이징된 이력 목록 조회
        List<StockHistoryResponse> items = goodsMapper.selectIpgoHistoryPaged(params);
        
        // 4. 페이지네이션 UI를 위한 정보 계산
        int paginationRange = 5;
        int startPage = (int) (Math.floor((page - 1) / paginationRange) * paginationRange + 1);
        int endPage = Math.min(startPage + paginationRange - 1, totalPages);
        
        // 5. 최종 페이지 객체 생성하여 반환
        return StockHistoryPageResponse.builder()
                .items(items)
                .page(page).size(size)
                .total(total).totalPages(totalPages)
                .startPage(startPage).endPage(endPage)
                .hasPrev(startPage > 1)
                .hasNext(endPage < totalPages)
                .build();
    }
    
    /**
     * 필터링용: 모든 상품의 번호와 이름을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<GoodsResponse> getAllGoodsForFilter() {
        return goodsMapper.selectAllForFilter();
    }
    
    /**
     * 메인 페이지에 표시될 베스트 상품 6개를 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<GoodsResponse> getBestGoodsList() {
        return goodsMapper.selectBestGoodsList();
    }
    
    /**
     * 상품별 판매 현황 통계를 페이징 및 검색 기능과 함께 조회합니다.
     */
    @Transactional(readOnly = true)
    public PageData<GoodsSalesResponse> getGoodsSalesStatusPage(String sortBy, String sortDir, String searchWord, int page, int size) {
        String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";

        Map<String, Object> params = new HashMap<>();
        params.put("sortBy", sortBy);
        params.put("sortDir", direction);
        params.put("searchWord", searchWord);

        int total = goodsMapper.countGoodsSalesStatus(params);
        int totalPages = (int) Math.ceil(total / (double) size);

        long startRow = (page - 1L) * size + 1;
        long endRow = page * 1L * size;
        params.put("startRow", startRow);
        params.put("endRow", endRow);

        List<GoodsSalesResponse> items = goodsMapper.findGoodsSalesStatusPaginated(params);
        
        return new PageData<>(items, page, size, total, searchWord);
    }
    
    public GoodsStockResponse getGoodsDetailForView(String goodsNum, HttpServletRequest request, HttpServletResponse response) {
        // 조회수 증가 로직 (쿠키 기반)
        Cookie goodsCookie = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals("viewGoods")) {
                    goodsCookie = c;
                }
            }
        }

        boolean shouldIncreaseViewCount = false;
        if (goodsCookie != null) {
            if (!goodsCookie.getValue().contains("[" + goodsNum + "]")) {
                goodsCookie.setValue(goodsCookie.getValue() + "_[" + goodsNum + "]");
                shouldIncreaseViewCount = true;
            }
        } else {
            goodsCookie = new Cookie("viewGoods", "[" + goodsNum + "]");
            shouldIncreaseViewCount = true;
        }

        if (shouldIncreaseViewCount) {
            // 여기에 조회수 업데이트 DB 로직을 추가할 수 있습니다. (예: goodsMapper.updateViewCount(goodsNum);)
            // 현재는 쿠키만 처리하므로 그대로 둡니다.
            goodsCookie.setPath("/");
            goodsCookie.setMaxAge(60 * 60 * 24 * 30); // 30일
            response.addCookie(goodsCookie);
        }

        // DB에서 모든 상품 정보와 재고를 조회하여 반환
        return goodsMapper.selectOneWithStock(goodsNum); 
    }
}