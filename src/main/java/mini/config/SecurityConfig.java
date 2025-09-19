package mini.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 스프링 부트 3.x 버전에 맞는 메서드 보안 활성화
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 (개발 완료 후 활성화 권장)

            .authorizeHttpRequests(auth -> auth
                    // 💥 [수정] 누구나 접근 가능한 경로에 '/corner/**'를 추가합니다.
                    .requestMatchers("/", "/login/**", "/goods/**", "/banner/**", "/register/**", "/corner/**").permitAll()
                    .requestMatchers("/resources/**", "/static/**", "/upload/**").permitAll() // 정적 리소스 경로

                    // 그 외 모든 요청은 반드시 인증(로그인)을 요구하도록 변경
                    .anyRequest().authenticated()
                )
            .formLogin(login -> login
                .loginPage("/login/item.login")
                .loginProcessingUrl("/login")
                .usernameParameter("userId")
                .passwordParameter("userPw")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login/item.login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    // 💥 [수정] 로그아웃 성공 시, 다시 메인 페이지로 이동하도록 원상 복구합니다.
                    .logoutSuccessUrl("/") 
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    // 💥 프로젝트의 유일한 PasswordEncoder. 여기서 모든 암호화를 관리합니다.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}