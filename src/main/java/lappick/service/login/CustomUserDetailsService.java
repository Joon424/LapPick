package lappick.service.login;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lappick.domain.AuthInfoDTO;
import lappick.mapper.LoginMapper;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    LoginMapper loginMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 사용자 ID로 DB에서 회원 정보(암호화된 비밀번호, 등급 포함)를 조회합니다.
        AuthInfoDTO auth = loginMapper.loginSelectOne(username);
        
        // 2. 사용자가 없으면, 스프링 시큐리티에 오류를 알립니다.
        if (auth == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }
        
        // 3. 사용자가 있다면, 권한 목록을 만듭니다. (MEM 또는 EMP)
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + auth.getGrade().toUpperCase()));
        
        // 4. 스프링 시큐리티가 사용할 UserDetails 객체를 만들어서 반환합니다.
        return new User(auth.getUserId(), auth.getUserPw(), authorities);
    }
}