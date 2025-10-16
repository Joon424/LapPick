package lappick.auth.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lappick.auth.dto.RegisterRequest;
import lappick.auth.mapper.AuthMapper;
import lappick.member.dto.MemberResponse;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    
    // MemberJoinService의 로직을 가져옴
    public void joinMember(RegisterRequest registerRequest) {
    	MemberResponse dto = new MemberResponse();
        dto.setGender(registerRequest.getGender());
        dto.setMemberAddr(registerRequest.getMemberAddr());
        dto.setMemberAddrDetail(registerRequest.getMemberAddrDetail());
        dto.setMemberBirth(registerRequest.getMemberBirth());
        dto.setMemberEmail(registerRequest.getMemberEmail());
        dto.setMemberId(registerRequest.getMemberId());
        dto.setMemberName(registerRequest.getMemberName());
        dto.setMemberPhone1(registerRequest.getMemberPhone1());
        dto.setMemberPhone2(registerRequest.getMemberPhone2());
        dto.setMemberPost(registerRequest.getMemberPost());

        dto.setMemberPw(passwordEncoder.encode(registerRequest.getMemberPw()));
        
        authMapper.userInsert(dto);
    }
    
    // ▼▼▼▼▼ [추가] 아이디 찾기 기능 (기존 MemberService에서 이동) ▼▼▼▼▼
    @Transactional(readOnly = true)
    public String findIdByNameAndEmail(String memberName, String memberEmail) {
        return authMapper.findIdByNameAndEmail(memberName, memberEmail);
    }

    // ▼▼▼▼▼ [추가] 비밀번호 재설정 기능 (기존 MemberService에서 이동) ▼▼▼▼▼
    public String resetPassword(String memberId, String memberEmail) {
        MemberResponse member = authMapper.findByIdAndEmail(memberId, memberEmail);
        if (member == null) {
            return null;
        }

        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        member.setMemberPw(passwordEncoder.encode(tempPassword));
        authMapper.memberPwUpdate(member);

        /* // 실제 운영 시 주석 해제
        String subject = "[LapPick] 임시 비밀번호 안내";
        String text = "회원님의 임시 비밀번호는 " + tempPassword + " 입니다. 로그인 후 반드시 비밀번호를 변경해주세요.";
        emailService.sendSimpleMessage(member.getMemberEmail(), subject, text);
        */
        return tempPassword;
    }
}