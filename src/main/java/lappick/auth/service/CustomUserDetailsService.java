package lappick.auth.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lappick.auth.dto.AuthDetails;
import lappick.auth.mapper.AuthMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // @Autowired 대신 생성자 주입 방식 사용
public class CustomUserDetailsService implements UserDetailsService {
    
    private final AuthMapper authMapper; // LoginMapper -> AuthMapper

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthDetails auth = authMapper.loginSelectOne(username); // AuthInfoDTO -> AuthDetails
        
        if (auth == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }
        
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + auth.getGrade().toUpperCase()));
        
        return new User(auth.getUserId(), auth.getUserPw(), authorities);
    }
}