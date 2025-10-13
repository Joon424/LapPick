package lappick.service;

import org.springframework.stereotype.Service;

import lappick.mapper.AutoNumMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // final 필드 자동 주입
public class AutoNumService {
    private final AutoNumMapper autoNumMapper;

    public String execute(String tableName, String colName, String preFix) {
        String autoNum = autoNumMapper.AutoNumSelect(tableName, colName, preFix);
        return autoNum;
    }
}