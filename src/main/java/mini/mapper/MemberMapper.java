package mini.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import mini.domain.MemberDTO;
import mini.domain.StartEndPageDTO;

@Mapper
public interface MemberMapper {
	public void memberInsert(MemberDTO dto);
	public List<MemberDTO> memberSelectList(StartEndPageDTO sepDTO); 
	public Integer memberCount();
	
    Integer memberCountBySearch(@Param("searchWord") String searchWord);

    MemberDTO memberSelectOne(@Param("memberNum") String memberNum);
    
	public void memberUpdate(MemberDTO dto);
	public Integer memberEmailCheckUpdate(String memberEmail);
	public String memberNumSelect(String memberId);
	
    /** 단건 삭제 */
    int memberDeleteOne(@Param("memberNum") String memberNum);

    /** 다건 삭제 */
    int memberDeleteMany(@Param("nums") List<String> nums);
	
	int memberDelete(@Param("nums") List<String> nums);
    String selectPwById(@Param("memberId") String memberId);
    int existsByMemberId(@Param("memberId") String memberId);
    
    int memberIdExists(@Param("memberId") String memberId);

    int memberCount(@Param("searchWord") String searchWord);

    List<MemberDTO> memberList(Map<String, Object> params);
    String findNumById(@Param("memberId") String memberId);
	public int idCheck(String memberId);
    

}







