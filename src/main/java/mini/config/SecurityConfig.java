package mini.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	    http
	        // (개발 중) CSRF 끄려면 ↓ 유지, 운영에서는 주석 처리하고 A방법만 쓰세요.
	        .csrf(csrf -> csrf.disable())

	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers(
	                "/login/**",
	                "/resources/**", "/static/**", "/css/**", "/js/**", "/images/**", "/webjars/**"
	            ).permitAll()
	            .requestMatchers("/emp/**", "/admin/**").hasAnyRole("EMP","ADMIN")
	            .anyRequest().permitAll()
	        )
	        .formLogin(login -> login
	            .loginPage("/login/item.login")        // 로그인 화면 GET	         
	            .usernameParameter("userId")           // input name과 동일
	            .passwordParameter("userPw")           // input name과 동일
	            .defaultSuccessUrl("/", false)         // 성공 후 이동 (필요시 직원용 URL로 변경)
	            .failureUrl("/login/item.login?error")
	            .permitAll()
	        )
	        .logout(logout -> logout
	            .logoutUrl("/logout")
	            .logoutSuccessUrl("/")
	            .invalidateHttpSession(true)
	            .deleteCookies("JSESSIONID")
	        );

	    return http.build();
	}

}

