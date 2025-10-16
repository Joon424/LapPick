package lappick.admin.employee.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lappick.admin.employee.dto.EmployeeResponse;
import lappick.admin.employee.dto.EmployeeUpdateRequest;
import lappick.admin.employee.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class EmployeeMyPageService {

    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeInfo(String empId) {
        return employeeMapper.selectByEmpId(empId);
    }
    
    public void changePassword(String empId, String oldPw, String newPw) {
        EmployeeResponse emp = employeeMapper.selectByEmpId(empId);
        if (emp == null || !passwordEncoder.matches(oldPw, emp.getEmpPw())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        String encodedNewPw = passwordEncoder.encode(newPw);
        employeeMapper.employeePwUpdate(encodedNewPw, empId);
    }
    
    public void updateMyInfo(EmployeeUpdateRequest command, String empId) {
        // 수정 요청을 보낸 사용자가 본인인지 확인 (empId는 컨트롤러에서 Principal로 가져온 값이므로 신뢰 가능)
        command.setEmpId(empId);

        // EmployeeUpdateRequest를 EmployeeResponse(DTO)로 변환하여 업데이트
        EmployeeResponse dto = new EmployeeResponse();
        dto.setEmpNum(command.getEmpNum());
        dto.setEmpAddr(command.getEmpAddr());
        dto.setEmpAddrDetail(command.getEmpAddrDetail());
        dto.setEmpEmail(command.getEmpEmail());
        dto.setEmpPhone(command.getEmpPhone());
        dto.setEmpPost(command.getEmpPost());

        employeeMapper.employeeUpdate(dto);
    }
}