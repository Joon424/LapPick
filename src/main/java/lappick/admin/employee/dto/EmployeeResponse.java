package lappick.admin.employee.dto;

import java.util.Date;
import org.apache.ibatis.type.Alias;
import lombok.Data;

@Data
@Alias("EmployeeResponse")
public class EmployeeResponse { // EmployeeDTO -> EmployeeResponse
    String empNum;
    String empId;
    String empPw;
    String empName;
    String empAddr;
    String empAddrDetail;
    Integer empPost;
    String empPhone;
    String empEmail;
    Date empHireDate;
    String empJumin;
    String empImage;
}