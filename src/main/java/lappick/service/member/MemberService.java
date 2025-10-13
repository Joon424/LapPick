package lappick.service.member;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 추가

import lappick.command.MemberCommand;
import lappick.domain.MemberDTO;
import lappick.domain.MemberListPage;
import lappick.domain.StartEndPageDTO;
import lappick.mapper.MemberMapper;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Transactional // 데이터 변경이 있는 서비스에는 클래스 레벨에 @Transactional을 붙이는 것이 좋습니다.
@RequiredArgsConstructor
public class MemberService {

	@Autowired
	private MemberMapper memberMapper; // MemberMapper 주입 필요

	@Autowired
	private PasswordEncoder passwordEncoder; // PasswordEncoder 주입 필요
	
	private final EmailService emailService;
	
	 // ▼▼▼ [추가] 아이디 찾기 서비스 로직 ▼▼▼
    @Transactional(readOnly = true)
    public String findIdByNameAndEmail(String memberName, String memberEmail) {
        return memberMapper.findIdByNameAndEmail(memberName, memberEmail);
    }

 // ▼▼▼ [수정] 비밀번호 재설정 서비스 로직 (테스트용) ▼▼▼
    public String resetPassword(String memberId, String memberEmail) {
        MemberDTO member = memberMapper.findByIdAndEmail(memberId, memberEmail);
        if (member == null) {
            return null; // 일치하는 회원이 없으면 null 반환
        }

        // 1. 임시 비밀번호 생성 (8자리 랜덤 문자열)
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        // 2. DB에 암호화된 임시 비밀번호로 업데이트
        member.setMemberPw(passwordEncoder.encode(tempPassword));
        memberMapper.memberPwUpdate(member);

        /*
        // 3. [중요] 실제 운영 시 주석 해제 필요!
        // 이메일 발송 로직을 잠시 주석 처리합니다.
        String subject = "[LapPick] 임시 비밀번호 안내";
        String text = "회원님의 임시 비밀번호는 " + tempPassword + " 입니다. 로그인 후 반드시 비밀번호를 변경해주세요.";
        emailService.sendSimpleMessage(member.getMemberEmail(), subject, text);
        */

        // 4. [수정] 이메일 발송 대신, 생성된 임시 비밀번호를 반환합니다.
        return tempPassword;
    }

    // 💥 [추가] 현재 로그인한 사용자의 ID로 상세 정보를 조회하는 메서드
    @Transactional(readOnly = true)
    public MemberDTO getMemberInfo(String memberId) {
        return memberMapper.selectMemberById(memberId);
    }

    
    
    
    // 💥 [수정] 마이페이지에서 '내 정보'를 수정하는 메서드
    public void updateMyInfo(MemberCommand command) {
        // 비밀번호 확인 로직 (현재 비밀번호를 맞게 입력했는지 체크)
        String encodedPassword = memberMapper.selectPwById(command.getMemberId());
        if (encodedPassword == null || !passwordEncoder.matches(command.getMemberPw(), encodedPassword)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // MemberCommand를 MemberDTO로 변환하여 업데이트
        MemberDTO dto = new MemberDTO();
        dto.setMemberNum(command.getMemberNum()); // command에 담긴 회원번호 사용
        dto.setMemberName(command.getMemberName());
        dto.setMemberAddr(command.getMemberAddr());
        dto.setMemberAddrDetail(command.getMemberAddrDetail());
        dto.setMemberPost(command.getMemberPost());
        dto.setMemberPhone1(command.getMemberPhone1());
        dto.setMemberEmail(command.getMemberEmail());
        dto.setMemberBirth(command.getMemberBirth());

        memberMapper.memberUpdate(dto);
    }

    // 💥 [추가] 비밀번호를 변경하는 메서드
    public void changePassword(String memberId, String oldPw, String newPw) {
        String encodedPassword = memberMapper.selectPwById(memberId);
        if (encodedPassword == null || !passwordEncoder.matches(oldPw, encodedPassword)) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        
        MemberDTO dto = new MemberDTO();
        dto.setMemberId(memberId);
        dto.setMemberPw(passwordEncoder.encode(newPw)); // 새 비밀번호 암호화
        
        // Mapper에 memberPwUpdate와 같은 ID의 update 쿼리 필요
        memberMapper.memberPwUpdate(dto); 
    }
    
    // 1. 회원 목록 조회 로직 통합 (기존 MemberListService)
    @Transactional(readOnly = true) // 조회 전용 메서드에는 readOnly=true 옵션으로 성능 향상
    public MemberListPage getMemberListPage(Integer page, Integer size, String searchWord) {
        int p = (page == null || page < 1) ? 1 : page;
        int s = 5; 

        long startRow = (p - 1L) * s + 1;
        long endRow   = p * 1L * s;

        StartEndPageDTO sep = new StartEndPageDTO(startRow, endRow, searchWord);
        List<MemberDTO> list = memberMapper.memberSelectList(sep);
        // XML의 memberCountBySearch 쿼리를 호출하도록 Mapper 인터페이스에 추가 필요
        int total = memberMapper.memberCountBySearch(searchWord); 
        int totalPages = (int)Math.ceil(total / (double)s);

        return MemberListPage.builder()
                .items(list)
                .page(p).size(s)
                .total(total).totalPages(totalPages)
                .searchWord(searchWord)
                .build();
    }

    // 2. 회원 상세 정보 조회 통합 (기존 MemberDetailService)
    @Transactional(readOnly = true)
    public MemberDTO getMemberDetail(String memberNum) {
        return memberMapper.memberSelectOne(memberNum);
    }
    
    // 3. 회원 등록 통합 (기존 MemberWriteService)
    public void createMember(MemberDTO dto) {
        memberMapper.memberInsert(dto);
    }

    // 4. 회원 수정 통합 (기존 MemberUpdateService)
    public void updateMember(MemberCommand command) {
        // DTO로 변환하여 Mapper에 전달 (또는 Mapper가 Command를 직접 받도록 수정)
        MemberDTO dto = new MemberDTO();
        dto.setMemberNum(command.getMemberNum());
        dto.setMemberName(command.getMemberName());
        dto.setMemberAddr(command.getMemberAddr());
        dto.setMemberAddrDetail(command.getMemberAddrDetail());
        dto.setMemberPost(command.getMemberPost());
        dto.setGender(command.getGender());
        dto.setMemberPhone1(command.getMemberPhone1());
        dto.setMemberEmail(command.getMemberEmail());
        dto.setMemberBirth(command.getMemberBirth());
        // 필요한 다른 필드들도 command에서 dto로 복사
        
        memberMapper.memberUpdate(dto);
    }
    
    // 5. 회원 단건/다건 삭제 통합 (기존 MemberDeleteService)
    public int deleteMembers(List<String> memberNums) {
        // Mapper의 memberDelete 메서드를 호출하도록 XML과 인터페이스 확인/수정 필요
        return memberMapper.memberDelete(memberNums);
    }
    
    public void withdrawMember(String memberId, String rawPassword) {
        // DB에 저장된 암호화된 비밀번호 조회
        String encodedPassword = memberMapper.selectPwById(memberId);

        // 비밀번호 검증
        if (encodedPassword == null || !passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 회원 번호 조회 후 삭제
        String memberNum = memberMapper.memberNumSelect(memberId);
        if (memberNum == null) {
            throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
        }
        
        // deleteMembers 메서드를 재사용 (하나의 아이디를 리스트로 만들어서 전달)
        deleteMembers(Collections.singletonList(memberNum));
    }
    

 // 모든 회원의 비밀번호를 BCrypt로 마이그레이션하는 메서드
    // 모든 회원의 비밀번호를 BCrypt로 마이그레이션하는 메서드
    public void migratePasswords() {
        List<MemberDTO> allMembers = memberMapper.selectAllMembers();
        for (MemberDTO member : allMembers) {
            // 이미 BCrypt로 암호화된 비밀번호는 건너뛰기
            // [수정] getUserPw() -> getMemberPw()
            if (member.getMemberPw() != null && !member.getMemberPw().startsWith("$2a$")) {
                String encodedPassword = passwordEncoder.encode(member.getMemberPw());
                // [수정] setUserPw() -> setMemberPw()
                member.setMemberPw(encodedPassword);
                memberMapper.updatePassword(member);

                // [참고] MemberMapper.xml의 updatePassword 쿼리도 memberPw를 사용해야 합니다.
                // <update id="updatePassword" ...> SET MEMBER_PW = #{memberPw} ... </update>
            }
        }
    }
}