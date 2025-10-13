package lappick.service.employee;

import java.util.List;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lappick.command.EmployeeCommand;
import lappick.domain.EmployeeDTO;
import lappick.domain.EmployeeListPage;
import lappick.domain.StartEndPageDTO;
import lappick.mapper.EmployeeMapper;

@Service
@Transactional
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 직원 목록 조회 (from EmployeeListService)
     * @return EmployeeListPage : 페이징 정보가 포함된 DTO
     */
    @Transactional(readOnly = true)
    public EmployeeListPage getEmployeeListPage(String searchWord, int page) {
        int limit = 5; // 페이지당 3개씩 표시
        long startRow = (page - 1L) * limit + 1;
        long endRow = page * 1L * limit;

        StartEndPageDTO sepDTO = new StartEndPageDTO(startRow, endRow, searchWord);
        List<EmployeeDTO> list = employeeMapper.employeeAllSelect(sepDTO);
        int total = employeeMapper.employeeCount(searchWord);
        int totalPages = (int) Math.ceil(total / (double) limit);

        return EmployeeListPage.builder()
                .items(list)
                .page(page).size(limit)
                .total(total).totalPages(totalPages)
                .searchWord(searchWord)
                .build();
    }

    /**
     * 직원 상세 정보 조회 (from EmployeeDetailService)
     * @return EmployeeDTO
     */
    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeDetail(String empNum) {
        return employeeMapper.employeeOneSelect(empNum);
    }

    /**
     * 직원 등록 (from EmployeeInsertService)
     */
    public void createEmployee(EmployeeCommand command) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmpNum(command.getEmpNum());
        dto.setEmpId(command.getEmpId());
        dto.setEmpName(command.getEmpName());
        dto.setEmpAddr(command.getEmpAddr());
        dto.setEmpAddrDetail(command.getEmpAddrDetail());
        dto.setEmpPost(command.getEmpPost());
        dto.setEmpPhone(command.getEmpPhone());
        dto.setEmpEmail(command.getEmpEmail());
        dto.setEmpJumin(command.getEmpJumin());
        // 비밀번호는 반드시 암호화하여 저장
        dto.setEmpPw(passwordEncoder.encode(command.getEmpPw()));
        
        employeeMapper.employeeInsert(dto);
    }

    /**
     * 직원 정보 수정 (from EmployeeUpdateService)
     */
    public void updateEmployee(EmployeeCommand command) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmpNum(command.getEmpNum());
        dto.setEmpName(command.getEmpName());
        dto.setEmpAddr(command.getEmpAddr());
        dto.setEmpAddrDetail(command.getEmpAddrDetail());
        dto.setEmpPost(command.getEmpPost());
        dto.setEmpPhone(command.getEmpPhone());
        dto.setEmpEmail(command.getEmpEmail());
        dto.setEmpJumin(command.getEmpJumin());
        dto.setEmpHireDate(command.getEmpHireDate());
        
        employeeMapper.employeeUpdate(dto);
    }

    /**
     * 직원 삭제 (from EmployeeDeleteService, EmployeesDeleteService)
     */
    public void deleteEmployees(String[] empNums) {
        employeeMapper.employeesDelete(empNums);
    }
    
    /**
     * [추가] 직원 ID로 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeDetailById(String empId) {
        return employeeMapper.selectEmployeeDetailById(empId);
    }
    
 // [추가] 비밀번호 변경 메소드
    @Transactional
    public void changePassword(String empId, String oldPw, String newPw) {
        EmployeeDTO emp = employeeMapper.selectEmployeeDetailById(empId); // ID로 직원 정보 조회
        if (emp == null) {
            throw new IllegalArgumentException("사용자 정보를 찾을 수 없습니다.");
        }
        
        // 현재 비밀번호가 일치하는지 확인
        if (!passwordEncoder.matches(oldPw, emp.getEmpPw())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호를 암호화하여 업데이트
        String encodedNewPw = passwordEncoder.encode(newPw);
        employeeMapper.updatePassword(empId, encodedNewPw);
    }
}