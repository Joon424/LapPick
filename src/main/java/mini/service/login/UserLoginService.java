package mini.service.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpSession;
import mini.command.LoginCommand;
import mini.domain.AuthInfoDTO;
import mini.mapper.LoginMapper;

@Service
public class UserLoginService {
	@Autowired
	LoginMapper loginMapper;
	@Autowired 
	PasswordEncoder passwordEncoder;
	public void execute(LoginCommand loginCommand
			,HttpSession session, BindingResult result) {
		System.out.println("로그인 시도: " + loginCommand.getUserId());
		AuthInfoDTO auth = loginMapper.loginSelectOne(loginCommand.getUserId());
		if(auth != null) {
			System.out.println("아이디가 존재합니다.");
			if(passwordEncoder.matches(loginCommand.getUserPw()
					, auth.getUserPw())) {
				System.out.println("비밀번호가 일치합니다.");
				session.setAttribute("auth", auth);
			}else {
				result.rejectValue("userPw", "loginCommand.userPw"
						, "비밀번호가 틀렸습니다.");
				System.out.println("비밀번호가 일치하지 않는다.");
			}
		}else {
			result.rejectValue("userId", "loginCommand.userId"
					, "아이디가 존재하지 않습니다.");
			System.out.println("아이디가 존재하지 않습니다.");
		}

	}
}
