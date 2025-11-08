package lappick.admin.member.service;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lappick.admin.member.dto.AdminMemberPageResponse;
import lappick.auth.mapper.AuthMapper;
import lappick.common.dto.StartEndPageDTO;
import lappick.member.dto.MemberResponse;
import lappick.member.dto.MemberUpdateRequest;
import lappick.member.mapper.MemberMapper;
import lappick.qna.mapper.QnaMapper;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminMemberService {

    private final MemberMapper memberMapper;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final QnaMapper qnaMapper;

    @Transactional(readOnly = true)
    public AdminMemberPageResponse getMemberListPage(Integer page, Integer size, String searchWord) {
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? 5 : size; // 기본값 5 (컨트롤러와 일치)

        long startRow = (p - 1L) * s + 1;
        long endRow = p * 1L * s;

        StartEndPageDTO sep = new StartEndPageDTO(startRow, endRow, searchWord);
        List<MemberResponse> list = memberMapper.memberSelectList(sep);
        int total = memberMapper.memberCountBySearch(searchWord);
        
        int totalPages = (total > 0) ? (int) Math.ceil((double) total / s) : 0;
        int pageBlock = 5;
        int startPage = ((p - 1) / pageBlock) * pageBlock + 1;
        int endPage = Math.min(startPage + pageBlock - 1, totalPages);
        
        // 엣지 케이스 처리 (total=0 일 때 endPage < startPage 방지)
        if (totalPages == 0 || endPage < startPage) {
            endPage = startPage;
        }

        return AdminMemberPageResponse.builder()
                .items(list)
                .page(p).size(s)
                .total(total).totalPages(totalPages)
                .searchWord(searchWord)
                .startPage(startPage)
                .endPage(endPage)
                // .hasPrev()/.hasNext()는 DTO의 메서드가 자동 계산
                .build();
    }
    @Transactional(readOnly = true)
    public MemberResponse getMemberDetail(String memberNum) {
        return memberMapper.memberSelectOneByNum(memberNum);
    }

    public void createMember(MemberUpdateRequest command) {
        MemberResponse dto = new MemberResponse();
        dto.setMemberId(command.getMemberId());
        dto.setMemberName(command.getMemberName());
        dto.setMemberAddr(command.getMemberAddr());
        dto.setMemberAddrDetail(command.getMemberAddrDetail());
        dto.setMemberPost(command.getMemberPost());
        dto.setGender(command.getGender());
        dto.setMemberPhone1(command.getMemberPhone1());
        dto.setMemberPhone2(command.getMemberPhone2());
        dto.setMemberEmail(command.getMemberEmail());
        dto.setMemberBirth(command.getMemberBirth());
        
        // 비밀번호 암호화
        String hashedPassword = passwordEncoder.encode(command.getMemberPw());
        dto.setMemberPw(hashedPassword);

        authMapper.userInsert(dto); 
    }

    public void updateMember(MemberUpdateRequest command) {
        // 1. DB에서 기존의 완전한 회원 정보를 가져옵니다.
        MemberResponse existingInfo = memberMapper.memberSelectOneByNum(command.getMemberNum());
        
        // 2. 폼에서 넘어온 수정된 값들만 기존 정보(existingInfo)에 덮어씁니다.
        existingInfo.setMemberName(command.getMemberName());
        existingInfo.setMemberAddr(command.getMemberAddr());
        existingInfo.setMemberAddrDetail(command.getMemberAddrDetail());
        existingInfo.setMemberPost(command.getMemberPost());
        existingInfo.setGender(command.getGender());
        existingInfo.setMemberPhone1(command.getMemberPhone1());
        existingInfo.setMemberPhone2(command.getMemberPhone2());
        existingInfo.setMemberEmail(command.getMemberEmail());
        existingInfo.setMemberBirth(command.getMemberBirth());
        
        // 3. 완전한 데이터가 담긴 DTO로 업데이트를 수행합니다.
        memberMapper.memberUpdate(existingInfo);
    }

    public int deleteMembers(List<String> memberNums) {
        // 1. 회원 삭제 전에 관련 QnA 데이터를 먼저 삭제합니다.
        qnaMapper.deleteQnaByMemberNums(memberNums);
        
        // 2. QnA 삭제 후 회원 데이터를 삭제합니다.
        return memberMapper.memberDelete(memberNums);
    }
}