package lappick.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import lappick.auth.dto.AuthDetails;
import lappick.member.dto.MemberResponse; // MemberResponse로 경로 변경

@Mapper
@Repository
public interface AuthMapper {
    // ===== 회원 가입 =====
    public Integer userInsert(MemberResponse dto); // MemberDTO -> MemberResponse

    // ===== 로그인 및 중복 체크 =====
    public Integer idCheckSelectOne(@Param("userId") String userId);
    public Integer emailCheckSelectOne(@Param("userEmail") String userEmail);
    public AuthDetails loginSelectOne(String userId);

    // ▼▼▼▼▼ [추가] MemberMapper에서 이동해 온 인증 관련 메소드들 ▼▼▼▼▼
    public String findIdByNameAndEmail(@Param("memberName") String memberName, @Param("memberEmail") String memberEmail);
    public MemberResponse findByIdAndEmail(@Param("memberId") String memberId, @Param("memberEmail") String memberEmail);
    public void memberPwUpdate(MemberResponse dto); // MemberDTO -> MemberResponse
    // ▲▲▲▲▲ [추가] MemberMapper에서 이동해 온 인증 관련 메소드들 ▲▲▲▲▲
}