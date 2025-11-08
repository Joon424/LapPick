package lappick.member.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.apache.ibatis.type.Alias;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.Data;

@Alias("MemberResponse")
@Data
public class MemberResponse { 
    String memberNum;
    String memberId;
    String memberPw;
    String memberName;
    String memberPhone1;
    String memberPhone2;
    String memberAddr;
    String memberAddrDetail;
    String memberPost;
    String gender;
    String memberEmail;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate memberBirth; 
    
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime memberRegist; 
    
    String memberEmailConf;
}