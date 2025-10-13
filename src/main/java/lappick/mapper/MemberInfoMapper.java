package lappick.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import lappick.domain.MemberDTO;

@Mapper
public interface MemberInfoMapper {
	public MemberDTO memberSelectOne(String memberId);
	public Integer memberUpdate(MemberDTO dto);
	public Integer memberPwUpdate(@Param("newPw") String newPw, @Param("memberId")String memberId);
	public Integer memberDelete(String memberId);
}
