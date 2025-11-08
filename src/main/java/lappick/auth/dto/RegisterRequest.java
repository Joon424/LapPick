package lappick.auth.dto;

import java.time.LocalDate; 
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    
    @NotEmpty(message = "아이디를 입력해주세요.")
    @Size(min = 5, max = 12, message = "아이디는 5자 이상 12자 이하로 입력해주세요.") 
    String memberId;

    @NotEmpty(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, message = "비밀번호는 8자 이상 입력해주세요.")
    String memberPw;
    
    @NotEmpty(message = "비밀번호 확인을 입력해주세요.")
    String memberPwCon;

    @NotBlank(message = "이름을 입력하여 주세요.")
    String memberName;

    @NotBlank(message = "주소를 입력하여 주세요.")
    String memberAddr;
    
    String memberPost;
    String memberAddrDetail;

    @NotBlank(message = "휴대폰 번호를 입력하여 주세요.")
    @Pattern(regexp = "^010[0-9]{8}$", message = "휴대폰 번호 11자리(010...)를 정확히 입력해주세요.")
    String memberPhone1;
    
    String memberPhone2;
    String gender;

    @NotNull(message = "생년월일을 입력해주세요.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate memberBirth = LocalDate.of(1990, 1, 1);

    @NotBlank(message = "이메일을 입력하여 주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String memberEmail;
}