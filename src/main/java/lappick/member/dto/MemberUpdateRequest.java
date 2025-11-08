package lappick.member.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MemberUpdateRequest {
    String memberNum;
    String memberId;
    String memberPw;
    String memberPwCon;

    @NotEmpty(message = "이름을 입력해주세요")
    String memberName;

    @NotBlank(message = "주소를 입력하여 주세요.")
    String memberAddr;

    String memberAddrDetail;
    String memberPost;

    @NotBlank(message = "휴대폰 번호를 입력하여 주세요.")
    @Pattern(regexp = "^010[0-9]{8}$", message = "휴대폰 번호 11자리(010...)를 정확히 입력해주세요.")
    String memberPhone1;

    String memberPhone2;
    String gender;

    @NotNull(message="생년월일을 입력해주세요.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate memberBirth;

    @NotBlank(message = "이메일을 입력하여 주세요.")
    String memberEmail;

    public boolean isMemberPwEqualMemberPwCon() {
        if (memberPw == null || memberPwCon == null) {
            return false;
        }
        return memberPw.equals(memberPwCon);
    }
}