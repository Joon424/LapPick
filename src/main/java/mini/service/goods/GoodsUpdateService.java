package mini.service.goods;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import mini.command.GoodsCommand;
import mini.domain.AuthInfoDTO;
import mini.domain.FileDTO;
import mini.domain.GoodsDTO;
import mini.mapper.EmployeeMapper;
import mini.mapper.GoodsMapper;

@Service
public class GoodsUpdateService {
    @Autowired
    EmployeeMapper employeeMapper;
    @Autowired
    GoodsMapper goodsMapper;

    public void execute(GoodsCommand goodsCommand, HttpSession session, BindingResult result, Model model) {
        GoodsDTO dto = new GoodsDTO();

        // 일반 정보 설정
        dto.setGoodsContents(goodsCommand.getGoodsContents());
        dto.setGoodsName(goodsCommand.getGoodsName());
        dto.setGoodsNum(goodsCommand.getGoodsNum());
        dto.setGoodsPrice(goodsCommand.getGoodsPrice());

        // 직원 정보 설정
        AuthInfoDTO auth = (AuthInfoDTO) session.getAttribute("auth");
        if (auth == null) {
            throw new IllegalArgumentException("로그인 정보가 없습니다."); // 예외 처리
        }
        String empNum = employeeMapper.getEmpNum(auth.getUserId());
        dto.setUpdateEmpNum(empNum);

        // 파일 저장 디렉토리 설정
     // 파일 저장 디렉토리 설정
        URL resource = getClass().getClassLoader().getResource("static/upload");
        String fileDir = resource.getFile() + File.separator; // 파일 경로 뒤에 슬래시 추가
        File uploadDir = new File(fileDir);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs(); // 디렉토리가 없으면 생성
        }

        // 대문 이미지 처리
        if (goodsCommand.getGoodsMainImage() != null) {
            MultipartFile mf = goodsCommand.getGoodsMainImage();
            String originalFile = mf.getOriginalFilename();
            String extension = originalFile.substring(originalFile.lastIndexOf("."));
            String storeName = UUID.randomUUID().toString().replace("-", "");
            String storeFileName = storeName + extension;

            try {
                File file = new File(fileDir + storeFileName);
                mf.transferTo(file);
                dto.setGoodsMainImage(originalFile);
                dto.setGoodsMainStoreImage(storeFileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 상세 이미지 처리
        StringBuilder originalTotal = new StringBuilder();
        StringBuilder storeTotal = new StringBuilder();

        for (MultipartFile mf : goodsCommand.getGoodsDetailImage()) {
            if (mf != null && !mf.getOriginalFilename().isEmpty()) {
                String originalFile = mf.getOriginalFilename();
                String extension = originalFile.substring(originalFile.lastIndexOf("."));
                String storeName = UUID.randomUUID().toString().replace("-", "");
                String storeFileName = storeName + extension;

                try {
                    File file = new File(fileDir + "/" + storeFileName);
                    mf.transferTo(file);
                    originalTotal.append(originalFile).append("/");
                    storeTotal.append(storeFileName).append("/");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // 세션에서 파일 리스트 가져오기
        List<FileDTO> list = (List<FileDTO>) session.getAttribute("fileList");
        GoodsDTO goodsDTO = goodsMapper.selectOne(goodsCommand.getGoodsNum());

        // null 체크 후 기존 이미지 가져오기
        List<String> dbOrg = new ArrayList<>();
        List<String> dbStore = new ArrayList<>();
        if (goodsDTO.getGoodsDetailImage() != null && goodsDTO.getGoodsDetailStoreImage() != null) {
            dbOrg = new ArrayList<>(Arrays.asList(goodsDTO.getGoodsDetailImage().split("[/`]")));
            dbStore = new ArrayList<>(Arrays.asList(goodsDTO.getGoodsDetailStoreImage().split("[/`]")));
        }

        // 세션 데이터와 기존 데이터 비교 후 삭제
        if (list != null) {
            for (FileDTO fdto : list) {
                Iterator<String> orgIterator = dbOrg.iterator();
                while (orgIterator.hasNext()) {
                    String img = orgIterator.next();
                    if (fdto.getOrgFile().equals(img)) {
                        orgIterator.remove();
                        dbStore.remove(fdto.getStoreFile());
                        break;
                    }
                }
            }
        }

        // 기존 이미지 추가
        for (String img : dbOrg) originalTotal.append(img).append("/");
        for (String img : dbStore) storeTotal.append(img).append("/");

        dto.setGoodsDetailImage(originalTotal.toString());
        dto.setGoodsDetailStoreImage(storeTotal.toString());

        // DB 업데이트
        int i = goodsMapper.goodsUpdate(dto);
        if (i > 0 && list != null) {
            for (FileDTO fd : list) {
                File file = new File(fileDir + fd.getStoreFile());
                if (file.exists()) file.delete();
            }
        }
    }
}












