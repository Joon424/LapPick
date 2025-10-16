package lappick.auth.dto;

import org.apache.ibatis.type.Alias;
import lombok.Data;

@Data
@Alias("AuthDetails") // auth -> AuthDetails
public class AuthDetails { // AuthInfoDTO -> AuthDetails
    String userId;
    String userPw;
    String userName;
    String userEmail;
    String grade;
    String memberPhone;
}