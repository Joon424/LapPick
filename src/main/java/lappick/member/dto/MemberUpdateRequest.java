package lappick.member.dto;

import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MemberUpdateRequest { // MemberCommand -> MemberUpdateRequest
    String memberNum;
    String memberId;
    String memberPw; // 비밀번호 확인용
    String memberPwCon;

    @NotEmpty(message = "이름을 입력해주세요")
    String memberName;

    @NotBlank(message = "주소를 입력하여 주세요.")
    String memberAddr;

    String memberAddrDetail;
    String memberPost;

    @NotBlank(message = "연락처을 입력하여 주세요.")
    @Size(min = 11, max = 23)
    String memberPhone1;

    String memberPhone2;
    String gender;

    @NotNull(message="생년월일을 입력해주세요.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    Date memberBirth;

    @NotBlank(message = "이메일을 입력하여 주세요.")
    String memberEmail;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    Date memberRegist;

    public boolean isMemberPwEqualMemberPwCon() {
        if (memberPw == null || memberPwCon == null) {
            return false;
        }
        return memberPw.equals(memberPwCon);
    }
}