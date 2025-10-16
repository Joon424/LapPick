package lappick.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lappick.auth.mapper.AuthMapper;


@Service
public class IdCheckService {
	@Autowired
	AuthMapper authMapper;
	public Integer execute(String userId) {
		return authMapper.idCheckSelectOne(userId);
	}

}
