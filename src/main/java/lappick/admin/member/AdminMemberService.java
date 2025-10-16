package lappick.admin.member;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lappick.admin.member.dto.AdminMemberPageResponse;
import lappick.auth.mapper.AuthMapper; // [수정] AuthMapper 의존성 주입 추가
import lappick.domain.StartEndPageDTO;
import lappick.member.dto.MemberResponse;
import lappick.member.dto.MemberUpdateRequest;
import lappick.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminMemberService {

    private final MemberMapper memberMapper;
    private final AuthMapper authMapper; // [수정] AuthMapper 의존성 주입 추가
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AdminMemberPageResponse getMemberListPage(Integer page, Integer size, String searchWord) {
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? 10 : size;

        long startRow = (p - 1L) * s + 1;
        long endRow = p * 1L * s;

        StartEndPageDTO sep = new StartEndPageDTO(startRow, endRow, searchWord);
        List<MemberResponse> list = memberMapper.memberSelectList(sep);
        int total = memberMapper.memberCountBySearch(searchWord);
        int totalPages = (int) Math.ceil(total / (double) s);

        return AdminMemberPageResponse.builder()
                .items(list)
                .page(p).size(s)
                .total(total).totalPages(totalPages)
                .searchWord(searchWord)
                .build();
    }

    @Transactional(readOnly = true)
    public MemberResponse getMemberDetail(String memberNum) {
        return memberMapper.memberSelectOneByNum(memberNum);
    }

    // [수정] MemberUpdateRequest를 직접 받아 처리하도록 개선
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

        // [수정] AuthMapper를 사용하여 회원 생성
        authMapper.userInsert(dto); 
    }

    public void updateMember(MemberUpdateRequest command) {
        MemberResponse dto = new MemberResponse();
        
        // [수정] 모든 필드를 command에서 dto로 복사하는 로직 완성
        dto.setMemberNum(command.getMemberNum());
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
        
        memberMapper.memberUpdate(dto);
    }

    public int deleteMembers(List<String> memberNums) {
        return memberMapper.memberDelete(memberNums);
    }
}