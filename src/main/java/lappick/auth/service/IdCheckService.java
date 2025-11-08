package lappick.auth.service;

import org.springframework.stereotype.Service;
import lappick.auth.mapper.AuthMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdCheckService {
	
	private final AuthMapper authMapper;
	
	public Integer execute(String userId) {
		return authMapper.idCheckSelectOne(userId);
	}

}
