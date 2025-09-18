package mini.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import mini.domain.MemberDTO;
import mini.domain.StartEndPageDTO;

@Mapper
public interface MemberMapper {
	public void memberInsert(MemberDTO dto);
	public List<MemberDTO> memberSelectList(StartEndPageDTO sepDTO); 
	public Integer memberCount();
	public MemberDTO memberSelectOne(String memberNum);
	public void memberUpdate(MemberDTO dto);
	int memberDelete(@Param("nums") List<String> nums);
	public Integer memberEmailCheckUpdate(String memberEmail);
	public String memberNumSelect(String memberId);
	String findNumById(@Param("memberId") String memberId);
}







