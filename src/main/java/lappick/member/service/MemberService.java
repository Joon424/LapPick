package lappick.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lappick.auth.mapper.AuthMapper;
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
    private final AuthMapper authMapper;

    @Transactional(readOnly = true)
    public MemberResponse getMemberInfo(String memberId) {
        return memberMapper.selectMemberById(memberId);
    }

    public void updateMyInfo(MemberUpdateRequest request, String memberId) {
        // 현재 비밀번호 확인
        String encodedPassword = authMapper.selectPwById(memberId);
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
    
    public void withdrawMember(String memberId, String rawPassword) {
        // 현재 비밀번호 확인
        String encodedPassword = authMapper.selectPwById(memberId);

        if (encodedPassword == null || !passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        
        memberMapper.deleteMemberById(memberId);
    }
}