package lappick.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import lappick.auth.dto.AuthDetails;
import lappick.member.dto.MemberResponse;

@Mapper
@Repository
public interface AuthMapper {
    
    public Integer userInsert(MemberResponse dto);

    public Integer idCheckSelectOne(@Param("userId") String userId);
    public Integer emailCheckSelectOne(@Param("userEmail") String userEmail);
    public AuthDetails loginSelectOne(String userId);

    public String findIdByNameAndEmail(@Param("memberName") String memberName, @Param("memberEmail") String memberEmail);
    public MemberResponse findByIdAndEmail(@Param("memberId") String memberId, @Param("memberEmail") String memberEmail);
    public void memberPwUpdate(MemberResponse dto);
    
    public String selectPwById(String memberId);
}