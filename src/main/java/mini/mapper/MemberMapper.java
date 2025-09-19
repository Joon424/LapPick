package mini.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
	
	public Integer memberCountBySearch(@Param("searchWord") String searchWord);

	// 💥 [추가] 아이디로 비밀번호를 조회하는 메서드
	public String selectPwById(@Param("memberId") String memberId);
	
	// MemberMapper.java 파일 안에 아래 두 줄을 추가해주세요.
	public MemberDTO selectMemberById(@Param("memberId") String memberId);
	public void memberPwUpdate(MemberDTO dto);

}







