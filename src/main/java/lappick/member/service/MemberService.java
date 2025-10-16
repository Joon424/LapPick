package lappick.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lappick.member.dto.MemberResponse;
import lappick.member.dto.MemberUpdateRequest;
import lappick.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    // MemberMyInfoService의 로직 통합
    @Transactional(readOnly = true)
    public MemberResponse getMemberInfo(String memberId) {
        return memberMapper.selectMemberById(memberId);
    }

    // MemberMyUpdateService의 로직 통합
    public void updateMyInfo(MemberUpdateRequest request, String memberId) {
        // 현재 비밀번호 확인
        String encodedPassword = memberMapper.selectPwById(memberId);
        if (encodedPassword == null || !passwordEncoder.matches(request.getMemberPw(), encodedPassword)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // MemberUpdateRequest를 MemberResponse(DTO)로 변환하여 업데이트
        MemberResponse dto = new MemberResponse();
        dto.setMemberId(memberId);
        dto.setMemberName(request.getMemberName());
        dto.setMemberAddr(request.getMemberAddr());
        dto.setMemberAddrDetail(request.getMemberAddrDetail());
        dto.setMemberPost(request.getMemberPost());
        dto.setMemberPhone1(request.getMemberPhone1());
        dto.setMemberPhone2(request.getMemberPhone2());
        dto.setMemberEmail(request.getMemberEmail());
        dto.setMemberBirth(request.getMemberBirth());
        dto.setGender(request.getGender());

        memberMapper.memberUpdate(dto);
    }

    // MemberPwUpdateService의 로직 통합
    public void changePassword(String memberId, String oldPw, String newPw) {
        String encodedPassword = memberMapper.selectPwById(memberId);
        if (encodedPassword == null || !passwordEncoder.matches(oldPw, encodedPassword)) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        
        MemberResponse dto = new MemberResponse();
        dto.setMemberId(memberId);
        dto.setMemberPw(passwordEncoder.encode(newPw));
        
        memberMapper.memberPwUpdate(dto); 
    }
    
    // MemberDropService의 로직 통합
    public void withdrawMember(String memberId, String rawPassword) {
        String encodedPassword = memberMapper.selectPwById(memberId);

        if (encodedPassword == null || !passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        
        memberMapper.deleteMemberById(memberId);
    }
}