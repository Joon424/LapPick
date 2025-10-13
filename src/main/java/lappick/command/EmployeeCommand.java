package lappick.command;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmployeeCommand {
	String empNum;
	@NotEmpty(message = "아이디를 입력해주세요. ")
	@Size(min = 5, max = 12)
	String empId;
	String empPw;
	@NotEmpty(message = "비밀번호확인 입력하여 주세요.")
	String empPwCon;
	@NotBlank(message = "이름을 입력하여 주세요.")
	String empName;
	@NotBlank(message = "주소를 입력하여 주세요.")
	String empAddr;
	String empAddrDetail;
	Integer empPost;
	@NotBlank(message = "연락처을 입력하여 주세요.")
	String empPhone;
	@Email(message = "형식에 맞지 않습니다.")
	@NotEmpty(message = "이메일을 입력하여 주세요.")
	String empEmail;
	@NotEmpty(message = "주민번호를 입력하여 주세요.")
	String empJumin;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	Date empHireDate;
	//empPw와 empPwCon를 비교하기 위한 메서드
	public boolean isEmpPwEqualsEmpPwCon() {
		return empPw.equals(empPwCon);
	}
}