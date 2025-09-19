package mini.command;

import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MemberCommand {
    String memberNum;

    @NotEmpty(message = "아이디를 입력해주세요")
    String memberId;

    // 💥 [추가] 비밀번호를 받을 필드가 없어서 추가했습니다.
    String memberPw;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
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
        // null 체크 추가
        if (memberPw == null || memberPwCon == null) {
            return false;
        }
        return memberPw.equals(memberPwCon);
    }
}








