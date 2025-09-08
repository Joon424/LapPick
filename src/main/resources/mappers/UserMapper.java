package mini.mapper;

import org.apache.ibatis.annotations.Mapper;

import mini.domain.MemberDTO;

@Mapper
public interface UserMapper {
	public Integer userInsert(MemberDTO dto);
}