package mini.service.goods;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import mini.command.GoodsCommand;
import mini.command.GoodsFilterDTO;
import mini.domain.FileDTO;
import mini.domain.GoodsDTO;
import mini.domain.GoodsListPage;
import mini.domain.StartEndPageDTO;
import mini.mapper.EmployeeMapper;
import mini.mapper.GoodsMapper;

@Service
@Transactional
@RequiredArgsConstructor
public class GoodsService {

    private final GoodsMapper goodsMapper;
    private final EmployeeMapper employeeMapper;

    // --- Private Helper Methods for File Handling ---

    private String getUploadDirectory() {
        URL resource = getClass().getClassLoader().getResource("static/upload");
        if (resource == null) {
            throw new RuntimeException("Upload directory 'static/upload' not found.");
        }
        return resource.getFile();
    }

    private void deleteFile(String storeFileName) {
        if (storeFileName == null || storeFileName.isBlank()) return;
        File file = new File(getUploadDirectory(), storeFileName);
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
        File file = new File(getUploadDirectory(), storeFileName);
        try {
            multipartFile.transferTo(file);
            return new FileDTO(originalFile, storeFileName);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- Public Service Methods ---

    /**
     * 상품 목록 조회 (직원용/사용자용)
     */
    @Transactional(readOnly = true)
    public GoodsListPage getGoodsListPage(GoodsFilterDTO filter, int page, int limit) {
        long startRow = (page - 1L) * limit + 1;
        long endRow = page * 1L * limit;
        
        filter.setStartRow(startRow);
        filter.setEndRow(endRow);

        List<GoodsDTO> list = goodsMapper.allSelect(filter);
        int total = goodsMapper.goodsCount(filter);
        int totalPages = (int) Math.ceil(total / (double) limit);

        return GoodsListPage.builder()
                .items(list)
                .page(page).size(limit)
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
        dto.setGoodsNum(command.getGoodsNum());
        dto.setGoodsName(command.getGoodsName());
        dto.setGoodsPrice(command.getGoodsPrice());
        dto.setGoodsContents(command.getGoodsContents());

        // 현재 로그인한 직원 정보 설정
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String empNum = employeeMapper.getEmpNum(auth.getName());
        dto.setEmpNum(empNum);

        // 메인 이미지 업로드
        FileDTO mainImage = uploadFile(command.getGoodsMainImage());
        if (mainImage != null) {
            dto.setGoodsMainImage(mainImage.getOrgFile());
            dto.setGoodsMainStoreImage(mainImage.getStoreFile());
        }

        // 상세 이미지 업로드
        if (command.getGoodsDetailImage() != null && command.getGoodsDetailImage()[0] != null && !command.getGoodsDetailImage()[0].isEmpty()) {
            StringBuilder originalTotal = new StringBuilder();
            StringBuilder storeTotal = new StringBuilder();
            for (MultipartFile mf : command.getGoodsDetailImage()) {
                FileDTO detailImage = uploadFile(mf);
                if (detailImage != null) {
                    originalTotal.append(detailImage.getOrgFile()).append("/");
                    storeTotal.append(detailImage.getStoreFile()).append("/");
                }
            }
            dto.setGoodsDetailImage(originalTotal.toString());
            dto.setGoodsDetailStoreImage(storeTotal.toString());
        }
        
        goodsMapper.goodsInsert(dto);
    }

    /**
     * 상품 수정
     */
    public void updateGoods(GoodsCommand command, List<String> imagesToDelete) {
        GoodsDTO dto = goodsMapper.selectOne(command.getGoodsNum());
        dto.setGoodsName(command.getGoodsName());
        dto.setGoodsPrice(command.getGoodsPrice());
        dto.setGoodsContents(command.getGoodsContents());

        // 현재 로그인한 직원 정보 설정
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String empNum = employeeMapper.getEmpNum(auth.getName());
        dto.setUpdateEmpNum(empNum);
        
        // 메인 이미지 변경 시 기존 파일 삭제 후 업로드
        if (command.getGoodsMainImage() != null && !command.getGoodsMainImage().isEmpty()) {
            deleteFile(dto.getGoodsMainStoreImage());
            FileDTO mainImage = uploadFile(command.getGoodsMainImage());
            dto.setGoodsMainImage(mainImage.getOrgFile());
            dto.setGoodsMainStoreImage(mainImage.getStoreFile());
        }
        
        // 기존 상세 이미지 정보
        List<String> orgImages = new ArrayList<>(Arrays.asList(dto.getGoodsDetailImage().split("/")));
        List<String> storeImages = new ArrayList<>(Arrays.asList(dto.getGoodsDetailStoreImage().split("/")));
        
        // 삭제할 이미지 제거
        if (imagesToDelete != null) {
            for (String storeFileToDelete : imagesToDelete) {
                int index = storeImages.indexOf(storeFileToDelete);
                if (index != -1) {
                    deleteFile(storeImages.get(index));
                    storeImages.remove(index);
                    orgImages.remove(index);
                }
            }
        }
        
        // 새로 추가된 상세 이미지 업로드
        if (command.getGoodsDetailImage() != null && command.getGoodsDetailImage()[0] != null && !command.getGoodsDetailImage()[0].isEmpty()) {
            for (MultipartFile mf : command.getGoodsDetailImage()) {
                FileDTO detailImage = uploadFile(mf);
                if (detailImage != null) {
                    orgImages.add(detailImage.getOrgFile());
                    storeImages.add(detailImage.getStoreFile());
                }
            }
        }

        dto.setGoodsDetailImage(String.join("/", orgImages));
        dto.setGoodsDetailStoreImage(String.join("/", storeImages));

        goodsMapper.goodsUpdate(dto);
    }
    
    /**
     * 상품 삭제 (개별/다중)
     */
    public void deleteGoods(String[] goodsNums) {
        for (String goodsNum : goodsNums) {
            GoodsDTO dto = goodsMapper.selectOne(goodsNum);
            if (dto != null) {
                deleteFile(dto.getGoodsMainStoreImage());
                if (dto.getGoodsDetailStoreImage() != null) {
                    for (String storeFile : dto.getGoodsDetailStoreImage().split("/")) {
                        deleteFile(storeFile);
                    }
                }
                goodsMapper.goodsDelete(goodsNum);
            }
        }
    }
}