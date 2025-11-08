package lappick.admin.employee.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lappick.admin.employee.dto.AdminEmployeePageResponse;
import lappick.admin.employee.dto.EmployeeResponse;
import lappick.admin.employee.dto.EmployeeUpdateRequest;
import lappick.admin.employee.mapper.EmployeeMapper;
import lappick.common.dto.StartEndPageDTO;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminEmployeeService {

    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AdminEmployeePageResponse getEmployeeListPage(String searchWord, int page) {
        int limit = 5;
        long startRow = (page - 1L) * limit + 1;
        long endRow = page * 1L * limit;

        StartEndPageDTO sepDTO = new StartEndPageDTO(startRow, endRow, searchWord);
        List<EmployeeResponse> list = employeeMapper.employeeAllSelect(sepDTO);
        int total = employeeMapper.employeeCount(searchWord);
        
        int totalPages = (total > 0) ? (int) Math.ceil((double) total / limit) : 0;
        int pageBlock = 5; // 페이지 블록 크기
        int startPage = ((page - 1) / pageBlock) * pageBlock + 1;
        int endPage = Math.min(startPage + pageBlock - 1, totalPages);

        // 엣지 케이스 처리 (total=0 일 때 endPage < startPage 방지)
        if (totalPages == 0 || endPage < startPage) {
            endPage = startPage;
        }

        return AdminEmployeePageResponse.builder().items(list).page(page).size(limit)
                .total(total).totalPages(totalPages).searchWord(searchWord)
                .startPage(startPage)
                .endPage(endPage)
                // .hasPrev()/.hasNext()는 DTO의 메서드가 자동 계산
                .build();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeDetail(String empNum) {
        return employeeMapper.selectByEmpNum(empNum);
    }

    public void createEmployee(EmployeeUpdateRequest command) {
        EmployeeResponse dto = new EmployeeResponse();
        
        dto.setEmpNum(command.getEmpNum());
        dto.setEmpId(command.getEmpId());
        dto.setEmpPw(passwordEncoder.encode(command.getEmpPw()));
        dto.setEmpName(command.getEmpName());
        dto.setEmpJumin(command.getEmpJumin());
        dto.setEmpPhone(command.getEmpPhone());
        dto.setEmpEmail(command.getEmpEmail());
        dto.setEmpAddr(command.getEmpAddr());
        dto.setEmpAddrDetail(command.getEmpAddrDetail());
        dto.setEmpPost(command.getEmpPost());
        dto.setEmpHireDate(command.getEmpHireDate());
        
        employeeMapper.employeeInsert(dto);
    }

    public void updateEmployee(EmployeeUpdateRequest command) {
        // 1. DB에서 기존의 완전한 정보를 가져옵니다.
        EmployeeResponse existingInfo = employeeMapper.selectByEmpNum(command.getEmpNum());
        
        // 2. 폼에서 넘어온 수정된 값들만 기존 정보(existingInfo)에 덮어씁니다.
        existingInfo.setEmpName(command.getEmpName());
        existingInfo.setEmpJumin(command.getEmpJumin());
        existingInfo.setEmpPhone(command.getEmpPhone());
        existingInfo.setEmpEmail(command.getEmpEmail());
        existingInfo.setEmpAddr(command.getEmpAddr());
        existingInfo.setEmpAddrDetail(command.getEmpAddrDetail());
        existingInfo.setEmpPost(command.getEmpPost());
        existingInfo.setEmpHireDate(command.getEmpHireDate());

        // 3. 완전한 데이터가 담긴 DTO로 업데이트를 수행합니다.
        employeeMapper.employeeUpdate(existingInfo);
    }

    public void deleteEmployees(String[] empNums) {
        employeeMapper.employeesDelete(empNums);
    }
}