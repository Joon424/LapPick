package mini.service.goods;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import mini.command.GoodsCommand;
import mini.command.GoodsFilterCommand;
import mini.domain.FileDTO;
import mini.domain.GoodsDTO;
import mini.domain.GoodsListPage;
import mini.mapper.EmployeeMapper;
import mini.mapper.GoodsMapper;

@Service
@Transactional
@RequiredArgsConstructor
public class GoodsService {

    // [추가] application.properties에서 설정한 파일 경로를 주입받습니다.
    @Value("${file.upload.dir}")
    private String fileDir;
	
    private final GoodsMapper goodsMapper;
    private final EmployeeMapper employeeMapper;

    /**
     * 상품 목록 조회 (직원용/사용자용)
     * Controller에서 받은 필터 조건(filter)을 DB 쿼리에 맞게 가공하여 Mapper에 전달합니다.
     */
    @Transactional(readOnly = true)
    public GoodsListPage getGoodsListPage(GoodsFilterCommand filter, int limit) {
        // 1. 페이징을 위한 시작/끝 행 번호 계산
        long startRow = (filter.getPage() - 1L) * limit + 1;
        long endRow = filter.getPage() * 1L * limit;
        filter.setStartRow(startRow);
        filter.setEndRow(endRow);


        // 3. 가공된 필터 객체로 DB에서 데이터 조회
        List<GoodsDTO> list = goodsMapper.allSelect(filter);
        int total = goodsMapper.goodsCount(filter);
        int totalPages = (int) Math.ceil(total / (double) limit);

        // 4. 최종 페이지 객체 생성하여 반환
        return GoodsListPage.builder()
                .items(list)
                .page(filter.getPage()).size(limit)
                .total(total).totalPages(totalPages)
                .searchWord(filter.getSearchWord())
                .build();
    }

    /**
     * 상품 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public GoodsDTO getGoodsDetail(String goodsNum) {
        return goodsMapper.selectOne(goodsNum);
    }
    /**
     * 상품 등록
     */
    public void createGoods(GoodsCommand command) {
        GoodsDTO dto = new GoodsDTO();
        
        // Command 객체(입력값)의 데이터를 DTO 객체(DB 저장용)로 옮깁니다.
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

        // 현재 로그인한 직원 정보를 가져와 등록 직원 번호를 설정합니다.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String empNum = employeeMapper.getEmpNum(auth.getName());
        dto.setEmpNum(empNum);

        // 이미지 파일들을 서버에 업로드하고, DTO에 파일명을 저장합니다.
        FileDTO mainImage = uploadFile(command.getGoodsMainImage());
        if (mainImage != null) {
            dto.setGoodsMainImage(mainImage.getOrgFile());
            dto.setGoodsMainStoreImage(mainImage.getStoreFile());
        }
        
        dto.setGoodsDetailStoreImage(uploadMultipleFiles(command.getGoodsDetailImage()));
        dto.setGoodsDetailStore(uploadMultipleFiles(command.getGoodsDetail()));
        
        // 최종적으로 완성된 DTO를 Mapper에 전달하여 DB에 삽입합니다.
        goodsMapper.goodsInsert(dto);
    }

    // [신규] 여러 파일을 업로드하고 파일명 문자열을 반환하는 헬퍼 메서드
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
    
    /**
     * 상품 수정
     */
    public void updateGoods(GoodsCommand command, List<String> imagesToDelete,
                          List<String> detailDescImagesToDelete) {
        // 1. DB에서 기존 상품 정보를 가져옵니다.
        GoodsDTO dto = goodsMapper.selectOne(command.getGoodsNum());
        
        // 2. command 객체에 담긴 새로운 정보로 dto를 업데이트합니다.
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

        // 3. 수정한 직원 정보를 기록합니다.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String empNum = employeeMapper.getEmpNum(auth.getName());
        dto.setUpdateEmpNum(empNum);

        // 4. 메인 이미지 파일 처리 (새 파일이 있으면 기존 파일 삭제 후 업로드)
        if (command.getGoodsMainImage() != null && !command.getGoodsMainImage().isEmpty()) {
            deleteFile(dto.getGoodsMainStoreImage());
            FileDTO mainImage = uploadFile(command.getGoodsMainImage());
            dto.setGoodsMainImage(mainImage.getOrgFile());
            dto.setGoodsMainStoreImage(mainImage.getStoreFile());
        }
        
        // 5. 상세 이미지 및 상세 설명 이미지 파일 처리 (삭제할 것 삭제하고, 새 파일 추가)
        dto.setGoodsDetailStoreImage(updateMultipleFiles(dto.getGoodsDetailStoreImage(), imagesToDelete, command.getGoodsDetailImage()));
        dto.setGoodsDetailStore(updateMultipleFiles(dto.getGoodsDetailStore(), detailDescImagesToDelete, command.getGoodsDetail()));

        // 6. 최종적으로 완성된 DTO를 Mapper에 전달하여 DB에 업데이트합니다.
        goodsMapper.goodsUpdate(dto);
    }
    

    // [신규] 기존 파일 삭제 및 신규 파일 추가를 한번에 처리하는 헬퍼 메서드
    private String updateMultipleFiles(String existingFileNames, List<String> filesToDelete, MultipartFile[] newFiles) {
        // 1. 기존 파일 목록을 리스트로 변환
        List<String> fileList = new ArrayList<>();
        if (existingFileNames != null && !existingFileNames.isEmpty()) {
            fileList.addAll(Arrays.asList(existingFileNames.split("/")));
        }

        // 2. 삭제할 파일 제거
        if (filesToDelete != null) {
            for (String fileToDelete : filesToDelete) {
                if (fileList.remove(fileToDelete)) {
                    deleteFile(fileToDelete);
                }
            }
        }
        
        // 3. 새로 추가된 파일 업로드
        if (newFiles != null && newFiles.length > 0) {
            Arrays.stream(newFiles)
                  .filter(f -> f != null && !f.isEmpty())
                  .map(this::uploadFile)
                  .forEach(fileDTO -> fileList.add(fileDTO.getStoreFile()));
        }

        // 4. 다시 '/'로 구분된 문자열로 합쳐서 반환
        return fileList.stream().collect(Collectors.joining("/"));
    }
    
    @Transactional
    public void deleteGoods(String[] nums) {
        // 1. String 배열을 List로 변환
        List<String> goodsNumList = Arrays.asList(nums);
        
        // 2. 삭제할 상품들의 파일 정보를 DB에서 한 번에 조회
        List<GoodsDTO> goodsForDelete = goodsMapper.selectGoodsByNumList(goodsNumList);

        // 3. 조회된 정보를 바탕으로 실제 파일 삭제
        for (GoodsDTO goods : goodsForDelete) {
            // 메인 이미지 파일 삭제
            if (goods.getGoodsMainStoreImage() != null) {
                File file = new File(fileDir + "/" + goods.getGoodsMainStoreImage());
                if (file.exists()) file.delete();
            }
            // 상세 썸네일 이미지 파일 삭제
            if (goods.getGoodsDetailStoreImage() != null) {
                for (String fileName : goods.getGoodsDetailStoreImage().split("/")) {
                    if (!fileName.isEmpty()) {
                        File file = new File(fileDir + "/" + fileName);
                        if (file.exists()) file.delete();
                    }
                }
            }
            // 상세 설명 이미지 파일 삭제
            if (goods.getGoodsDetailStore() != null) {
                for (String fileName : goods.getGoodsDetailStore().split("/")) {
                    if (!fileName.isEmpty()) {
                        File file = new File(fileDir + "/" + fileName);
                        if (file.exists()) file.delete();
                    }
                }
            }
        }

        // 4. DB에서 상품 정보들을 한 번에(일괄) 삭제
        goodsMapper.goodsDelete(goodsNumList);
    }

    // --- 파일 처리 Helper Methods (기존과 동일) ---
    private String getUploadDirectory() {
        URL resource = getClass().getClassLoader().getResource("static/upload");
        if (resource == null) {
            throw new RuntimeException("Upload directory 'static/upload' not found.");
        }
        return resource.getFile();
    }
    private void deleteFile(String storeFileName) {
        if (storeFileName == null || storeFileName.isBlank()) return;
        // [수정] 주입받은 fileDir 경로를 사용합니다.
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
        // [수정] 주입받은 fileDir 경로를 사용합니다.
        File file = new File(fileDir, storeFileName);
        try {
            // [추가] 만약 폴더가 없다면 자동으로 생성해줍니다.
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
}