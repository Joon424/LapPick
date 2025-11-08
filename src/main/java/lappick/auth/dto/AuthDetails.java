package lappick.auth.dto;

import java.util.Collection;
import org.apache.ibatis.type.Alias;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.Data;

@Data
@Alias("AuthDetails")
public class AuthDetails implements UserDetails {
    
    String userId;
    String userPw;
    String userName;
    String userEmail;
    String grade;
    String memberPhone;
    
    // 이 필드는 DB에서 매핑되지 않고, Service에서 수동으로 채워줄 것입니다.
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.userPw;
    }

    @Override
    public String getUsername() {
        return this.userId;
    }

    // 아래 4개는 계정 상태 관련 메서드입니다.
    // 특별한 계정 잠금/만료 로직이 없다면 모두 true를 반환합니다.
    
    @Override
    public boolean isAccountNonExpired() {
        return true; 
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}