package lappick.admin.employee.service;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lappick.admin.employee.dto.AdminEmployeePageResponse;
import lappick.admin.employee.dto.EmployeeResponse;
import lappick.admin.employee.dto.EmployeeUpdateRequest;
import lappick.admin.employee.mapper.EmployeeMapper;
import lappick.domain.StartEndPageDTO;
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
        int totalPages = (int) Math.ceil(total / (double) limit);

        return AdminEmployeePageResponse.builder().items(list).page(page).size(limit)
                .total(total).totalPages(totalPages).searchWord(searchWord).build();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeDetail(String empNum) {
        return employeeMapper.selectByEmpNum(empNum);
    }

    public void createEmployee(EmployeeUpdateRequest command) {
        EmployeeResponse dto = new EmployeeResponse();
        // ... command -> dto 필드 복사 로직 ...
        dto.setEmpPw(passwordEncoder.encode(command.getEmpPw()));
        employeeMapper.employeeInsert(dto);
    }

    public void updateEmployee(EmployeeUpdateRequest command) {
        EmployeeResponse dto = new EmployeeResponse();
        // ... command -> dto 필드 복사 로직 ...
        employeeMapper.employeeUpdate(dto);
    }

    public void deleteEmployees(String[] empNums) {
        employeeMapper.employeesDelete(empNums);
    }
}