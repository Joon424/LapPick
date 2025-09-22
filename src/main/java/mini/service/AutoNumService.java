package mini.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import mini.mapper.AutoNumMapper;

@Service
@RequiredArgsConstructor // final 필드 자동 주입
public class AutoNumService {
    private final AutoNumMapper autoNumMapper;

    public String execute(String tableName, String colName, String preFix) {
        String autoNum = autoNumMapper.AutoNumSelect(tableName, colName, preFix);
        return autoNum;
    }
}