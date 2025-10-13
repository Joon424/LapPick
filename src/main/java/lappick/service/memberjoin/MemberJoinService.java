package lappick.service.memberjoin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lappick.command.UserCommand;
import lappick.domain.MemberDTO;
import lappick.mapper.UserMapper;

@Service
public class MemberJoinService {
    @Autowired
    UserMapper userMapper;
 
    
    @Autowired
    PasswordEncoder passwordEncoder;
    
    public void execute(UserCommand userCommand) {
        // UserCommand에서 받아온 데이터를 MemberDTO로 변환
        MemberDTO dto = new MemberDTO();
        dto.setGender(userCommand.getGender());
        dto.setMemberAddr(userCommand.getMemberAddr());
        dto.setMemberAddrDetail(userCommand.getMemberAddrDetail());
        dto.setMemberBirth(userCommand.getMemberBirth());
        dto.setMemberEmail(userCommand.getMemberEmail());
        dto.setMemberId(userCommand.getMemberId());
        dto.setMemberName(userCommand.getMemberName());
        dto.setMemberPhone1(userCommand.getMemberPhone1());
        dto.setMemberPhone2(userCommand.getMemberPhone2());
        dto.setMemberPost(userCommand.getMemberPost());

        // 비밀번호 암호화
        dto.setMemberPw(passwordEncoder.encode(userCommand.getMemberPw()));
        
        // 회원 정보 삽입
        Integer insertResult = userMapper.userInsert(dto);
        if (insertResult != null && insertResult > 0) {
            // 성공적으로 저장된 후 DB에 저장된 값을 리턴
            System.out.println("회원가입 성공!");
        } else {
            // 실패한 경우 오류 처리
            System.out.println("회원가입 실패");
        }
        
    }
}
