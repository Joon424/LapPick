package lappick.mapper;

import org.apache.ibatis.annotations.Mapper;

import lappick.domain.MemberDTO;

@Mapper
public interface UserMapper {
	public Integer userInsert(MemberDTO dto);
}