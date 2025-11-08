package lappick.auth.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lappick.auth.dto.AuthDetails;
import lappick.auth.mapper.AuthMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final AuthMapper authMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthDetails auth = authMapper.loginSelectOne(username);
        
        if (auth == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }
        
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // DB에서 가져온 grade 값에 따라 정확한 Role 부여
        String role = "";
        if ("mem".equalsIgnoreCase(auth.getGrade())) {
            role = "ROLE_MEMBER";
        } 
        else if ("emp".equalsIgnoreCase(auth.getGrade())) {
            role = "ROLE_EMPLOYEE";
        }
        
        if (!role.isEmpty()) {
             authorities.add(new SimpleGrantedAuthority(role));
        }

        auth.setAuthorities(authorities); 
        
        return auth;
    }
}