package lappick.admin.employee.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import lappick.admin.employee.dto.EmployeeResponse;
import lappick.domain.StartEndPageDTO;

@Mapper
@Repository
public interface EmployeeMapper {
    // 직원 번호 자동 생성
    public String autoNum();
    
    // CUD (Create, Update, Delete)
    public Integer employeeInsert(EmployeeResponse dto);
    public Integer employeeUpdate(EmployeeResponse dto);
    public Integer employeesDelete(@Param("empNums") String[] empNums);
    public Integer employeePwUpdate(@Param("empPw") String empPw, @Param("empId") String empId);

    // Read (Select)
    public List<EmployeeResponse> employeeAllSelect(StartEndPageDTO sepDTO);
    public int employeeCount(String searchWord);
    public EmployeeResponse selectByEmpNum(String empNum); // employeeOneSelect -> selectByEmpNum
    public EmployeeResponse selectByEmpId(String empId); // employeeSelectOne -> selectByEmpId
    public String getEmpNum(String empId);
}