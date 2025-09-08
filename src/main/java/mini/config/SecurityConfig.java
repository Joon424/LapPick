// src/main/java/mini/config/SecurityConfig.java
package mini.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF는 폼 POST 보호용인데, 지금은 정적 리소스 확인/개발 편의상 끕니다.
            .csrf(csrf -> csrf.disable())
            // 정적 리소스(그리고 홈, 에러 등)는 모두 허용
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/error",
                    "/resources/**", "/static/**",
                    "/css/**", "/js/**", "/images/**", "/icons/**",
                    "/upload/**"
                ).permitAll()
                // 나머지도 당장은 전부 허용 (추후 로그인/권한 붙일 때 여기만 바꾸면 됨)
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
