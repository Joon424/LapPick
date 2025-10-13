package lappick.service.goods;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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

import lappick.command.GoodsCommand;
import lappick.command.GoodsFilterCommand;
import lappick.command.PageData;
import lappick.domain.FileDTO;
import lappick.domain.GoodsDTO;
import lappick.domain.GoodsIpgoDTO;
import lappick.domain.GoodsListPage;
import lappick.domain.GoodsSalesDTO;
import lappick.domain.GoodsStockDTO;
import lappick.domain.StockHistoryPageDTO;
import lappick.mapper.EmployeeMapper;
import lappick.mapper.GoodsMapper;
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
    public GoodsListPage getGoodsListPage(GoodsFilterCommand filter, int limit) {
        long startRow = (filter.getPage() - 1L) * limit + 1;
        long endRow = filter.getPage() * 1L * limit;
        filter.setStartRow(startRow);
        filter.setEndRow(endRow);

        List<GoodsDTO> list = goodsMapper.allSelect(filter);
        int total = goodsMapper.goodsCount(filter);
        int totalPages = (int) Math.ceil(total / (double) limit);

        return GoodsListPage.builder()
                .items(list)
                .page(filter.getPage()).size(limit)
                .total(total).totalPages(totalPages)
                .searchWord(filter.getSearchWord())
                .build();
    }

    @Transactional(readOnly = true)
    public GoodsDTO getGoodsDetail(String goodsNum) {
        return goodsMapper.selectOne(goodsNum);
    }

    public void createGoods(GoodsCommand command) {
        GoodsDTO dto = new GoodsDTO();
        
        dto.setGoodsNum(command.getGoodsNum());
        dto.setGoodsName(command.getGoodsName());
        dto.setGoodsPrice(command.getGoodsPrice());
        dto.setGoodsContents(command.getGoodsContents());
        dto.setGoodsBrand(command.getGoodsBrand());
        dto.setGoodsPurpose(command.getGoodsPurpose());
        dto.setGoodsScreenSize(command.getGoodsScreenSize());
        dto.setGoodsWeight(command.getGoodsWeight());
        dto.setGoodsKeyword1(command.getGoodsKeyword1());
        dto.setGoodsKeyword2(command.getGoodsKeyword2());
        dto.setGoodsKeyword3(command.getGoodsKeyword3());
        dto.setGoodsShippingInfo(command.getGoodsShippingInfo());
        dto.setGoodsSellerInfo(command.getGoodsSellerInfo());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String empNum = employeeMapper.getEmpNum(auth.getName());
        dto.setEmpNum(empNum);

        FileDTO mainImage = uploadFile(command.getGoodsMainImage());
        if (mainImage != null) {
            dto.setGoodsMainImage(mainImage.getOrgFile());
            dto.setGoodsMainStoreImage(mainImage.getStoreFile());
        }
        
        dto.setGoodsDetailStoreImage(uploadMultipleFiles(command.getGoodsDetailImage()));
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
    
    public void updateGoods(GoodsCommand command, List<String> imagesToDelete,
                          List<String> detailDescImagesToDelete) {
        GoodsDTO dto = goodsMapper.selectOne(command.getGoodsNum());
        
        dto.setGoodsName(command.getGoodsName());
        dto.setGoodsPrice(command.getGoodsPrice());
        dto.setGoodsContents(command.getGoodsContents());
        dto.setGoodsBrand(command.getGoodsBrand());
        dto.setGoodsPurpose(command.getGoodsPurpose());
        dto.setGoodsScreenSize(command.getGoodsScreenSize());
        dto.setGoodsWeight(command.getGoodsWeight());
        dto.setGoodsKeyword1(command.getGoodsKeyword1());
        dto.setGoodsKeyword2(command.getGoodsKeyword2());
        dto.setGoodsKeyword3(command.getGoodsKeyword3());
        dto.setGoodsShippingInfo(command.getGoodsShippingInfo());
        dto.setGoodsSellerInfo(command.getGoodsSellerInfo());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String empNum = employeeMapper.getEmpNum(auth.getName());
        dto.setUpdateEmpNum(empNum);

        if (command.getGoodsMainImage() != null && !command.getGoodsMainImage().isEmpty()) {
            deleteFile(dto.getGoodsMainStoreImage());
            FileDTO mainImage = uploadFile(command.getGoodsMainImage());
            dto.setGoodsMainImage(mainImage.getOrgFile());
            dto.setGoodsMainStoreImage(mainImage.getStoreFile());
        }
        
        dto.setGoodsDetailStoreImage(updateMultipleFiles(dto.getGoodsDetailStoreImage(), imagesToDelete, command.getGoodsDetailImage()));
        dto.setGoodsDetailStore(updateMultipleFiles(dto.getGoodsDetailStore(), detailDescImagesToDelete, command.getGoodsDetail()));

        goodsMapper.goodsUpdate(dto);
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
        List<GoodsDTO> goodsForDelete = goodsMapper.selectGoodsByNumList(goodsNumList);

        for (GoodsDTO goods : goodsForDelete) {
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

        goodsMapper.goodsDelete(goodsNumList);
    }

    private String getUploadDirectory() {
        URL resource = getClass().getClassLoader().getResource("static/upload");
        if (resource == null) {
            throw new RuntimeException("Upload directory 'static/upload' not found.");
        }
        return resource.getFile();
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

        // [추가] 입고(양수) 시 메모가 없으면 기본값 설정
        if (quantity > 0 && (memo == null || memo.isBlank())) {
            finalMemo = "정기 입고";
        } 
        // [추가] 출고(음수) 시 메모가 없으면 오류 발생
        else if (quantity < 0 && (memo == null || memo.isBlank())) {
            throw new IllegalArgumentException("재고 차감 시에는 변경 사유를 반드시 입력해야 합니다.");
        }
        
        goodsMapper.insertGoodsIpgo(goodsNum, quantity, finalMemo);
    }
    
    @Transactional(readOnly = true)
    public GoodsStockDTO getGoodsDetailWithStock(String goodsNum) {
        return goodsMapper.selectOneWithStock(goodsNum);
    }
    
    /**
     * [수정] 특정 상품의 재고 변경 이력을 페이징하여 가져오는 메소드
     */
    @Transactional(readOnly = true)
    public StockHistoryPageDTO getStockHistoryPage(String goodsNum, int page, int size) {
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
        List<GoodsIpgoDTO> items = goodsMapper.selectIpgoHistoryPaged(params);
        
        // 4. 페이지네이션 UI를 위한 정보 계산
        int paginationRange = 5; // 한 번에 보여줄 페이지 번호 개수
        int startPage = (int) (Math.floor((page - 1) / paginationRange) * paginationRange + 1);
        int endPage = Math.min(startPage + paginationRange - 1, totalPages);
        
        // 5. 최종 페이지 객체 생성하여 반환
        return StockHistoryPageDTO.builder()
                .items(items)
                .page(page).size(size)
                .total(total).totalPages(totalPages)
                .startPage(startPage).endPage(endPage)
                .hasPrev(startPage > 1)
                .hasNext(endPage < totalPages)
                .build();
    }
    
    /**
     * [추가] 필터링용: 모든 상품의 번호와 이름을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<GoodsDTO> getAllGoodsForFilter() {
        return goodsMapper.selectAllForFilter();
    }
    
    /**
     * [추가] 메인 페이지에 표시될 베스트 상품 6개를 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<GoodsDTO> getBestGoodsList() {
        return goodsMapper.selectBestGoodsList();
    }
    
    /**
     * [수정] 상품별 판매 현황 통계를 페이징 및 검색 기능과 함께 조회합니다.
     */
    @Transactional(readOnly = true)
    public PageData<GoodsSalesDTO> getGoodsSalesStatusPage(String sortBy, String sortDir, String searchWord, int page, int size) {
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

        List<GoodsSalesDTO> items = goodsMapper.findGoodsSalesStatusPaginated(params);
        
        return new PageData<>(items, page, size, total, searchWord);
    }
}