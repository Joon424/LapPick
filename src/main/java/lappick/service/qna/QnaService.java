package lappick.service.qna;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lappick.command.PageData;
import lappick.domain.PurchaseDTO;
import lappick.domain.QnaDTO;
import lappick.mapper.PurchaseMapper;
import lappick.mapper.QnaMapper;
import lappick.member.dto.MemberResponse;
import lappick.member.mapper.MemberMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QnaService {

    private final QnaMapper qnaMapper;
    private final PurchaseMapper purchaseMapper;
    private final MemberMapper memberMapper;

    @Transactional(readOnly = true)
    public PageData<QnaDTO> getMyQnaList(String memberId, String searchWord, String status, int page, int size) {
    	MemberResponse member = memberMapper.selectOneById(memberId);
        
        Map<String, Object> params = new HashMap<>();
        params.put("memberNum", member.getMemberNum());
        params.put("searchWord", searchWord);
        params.put("status", status); // [추가] status 파라미터 전달
        params.put("startRow", (long)(page - 1) * size + 1);
        params.put("endRow", (long)page * size);

        List<QnaDTO> list = qnaMapper.selectQnaByMemberNum(params);
        int total = qnaMapper.countMyQna(params);
        
        return new PageData<>(list, page, size, total, searchWord);
    }

    // [수정] purchaseNum과 goodsNum을 모두 받도록 시그니처 변경
    @Transactional
    public void writeQna(QnaDTO qnaDTO, String purchaseNum, String goodsNum, String memberId) {
        // 1. purchaseNum으로 구매 건 전체를 조회 (소유권 검증 목적)
        PurchaseDTO purchase = purchaseMapper.selectPurchaseDetail(purchaseNum);

        // 2. memberId로 현재 사용자 정보 조회
        MemberResponse member = memberMapper.selectOneById(memberId);

        // 3. 본인의 구매 건이 맞는지 검증
        if (member == null || !member.getMemberNum().equals(purchase.getMemberNum())) {
            throw new SecurityException("문의를 작성할 권한이 없습니다.");
        }

        // 4. [수정] 전달받은 goodsNum과 memberNum을 QnaDTO에 정확히 설정
        qnaDTO.setGoodsNum(goodsNum);
        qnaDTO.setMemberNum(member.getMemberNum());

        // 5. QNA를 DB에 저장
        qnaMapper.insertQna(qnaDTO);
    }
    
    
    @Transactional(readOnly = true)
    public List<QnaDTO> getQnaListByGoodsNum(String goodsNum) {
        return qnaMapper.selectQnaByGoodsNum(goodsNum);
    }
    
 // QnaService.java 에 아래 메소드를 추가합니다.

    /**
     * [추가] 상품 상세 페이지에서 작성된 문의를 처리합니다. (구매 이력 없이)
     */
    @Transactional
    public void writeQnaFromProductPage(QnaDTO qnaDTO, String goodsNum, String memberId) {
        // 1. 현재 로그인한 사용자의 정보를 조회합니다.
    	MemberResponse member = memberMapper.selectOneById(memberId);
        if (member == null) {
            throw new SecurityException("사용자 정보를 찾을 수 없습니다.");
        }

        // 2. QnaDTO에 상품번호와 회원번호를 설정합니다.
        qnaDTO.setGoodsNum(goodsNum);
        qnaDTO.setMemberNum(member.getMemberNum()); 

        // 3. QNA를 DB에 저장합니다.
        qnaMapper.insertQna(qnaDTO);
    }
    
 // [교체] getAllQnaList 메소드
    @Transactional(readOnly = true)
    public PageData<QnaDTO> getAllQnaList(String searchWord, String status, int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("searchWord", searchWord);
        params.put("status", status); // [추가] status 파라미터 전달
        params.put("startRow", (long)(page - 1) * size + 1);
        params.put("endRow", (long)page * size);

        List<QnaDTO> list = qnaMapper.selectAllQna(params);
        int total = qnaMapper.countAllQna(params);
        
        // PageData 생성자에도 searchWord를 전달해야 합니다.
        return new PageData<>(list, page, size, total, searchWord); 
    }
    
    @Transactional
    public void addAnswer(int qnaNum, String answerContent) {
        QnaDTO dto = new QnaDTO();
        dto.setQnaNum(qnaNum);
        dto.setAnswerContent(answerContent);
        qnaMapper.updateAnswer(dto);
    }
}