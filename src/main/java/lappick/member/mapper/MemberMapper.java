package lappick.member.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import lappick.common.dto.StartEndPageDTO;
import lappick.member.dto.MemberResponse;

@Mapper
@Repository
public interface MemberMapper {

    // ===== 관리자용 회원 관리 기능 =====
    public List<MemberResponse> memberSelectList(StartEndPageDTO sepDTO); 
    public Integer memberCountBySearch(@Param("searchWord") String searchWord);
    public MemberResponse memberSelectOneByNum(String memberNum);
    public void adminMemberUpdate(MemberResponse dto);
    public int memberDelete(List<String> memberNums);
    
    // ===== 사용자(MyPage) 기능 =====
    public MemberResponse selectMemberById(String memberId);
    public void memberUpdate(MemberResponse dto);
    public void deleteMemberById(String memberId);

    // ===== 기타 (다른 서비스에서 사용) =====
    public String memberNumSelect(String memberId);
    public MemberResponse selectOneById(String memberId);
}