package mini.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * WebConfig
 * - 정적 리소스 매핑(과거 템플릿 호환 포함)
 * - 업로드 디렉터리(파일시스템) 매핑
 * - 공용 Bean(jsonView, passwordEncoder)
 *
 * ※ SecurityFilterChain은 별도 SecurityConfig에서 1개만 유지하세요.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 파일 업로드 실제 저장 경로
     * - 기본값 ./upload (프로젝트 루트 기준)
     * - application-*.yml 에서 app.upload-dir 로 변경 가능
     */
    @Value("${app.upload-dir:./upload}")
    private String uploadDir;

    /**
     * JSON 응답용 View
     * - 컨트롤러에서 ModelAndView("jsonView") 사용 시 필요
     */
    @Bean("jsonView")
    public MappingJackson2JsonView jsonView() {
        return new MappingJackson2JsonView();
    }

    /**
     * 비밀번호 인코더
     * - 회원/관리자 패스워드 해시용
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 정적/업로드 리소스 매핑
     * - /resources/** : 과거 템플릿의 경로를 현재 정적 리소스 위치로 매핑(호환성)
     * - /static/**    : 스프링 부트 기본 정적 경로 유지
     * - /upload/**    : 파일시스템(프로젝트 외부) 경로로 매핑 → 동적 업로드 파일 제공
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1) 과거 템플릿 호환: /resources/** → 실제 정적 위치들에서 탐색
        registry.addResourceHandler("/resources/**")
                .addResourceLocations(
                        "classpath:/static/",            // 현재 사용하는 기본 정적 폴더
                        "classpath:/resources/",         // 과거 구조 대비
                        "classpath:/public/",
                        "classpath:/META-INF/resources/" // 웹JAR 등 호환
                )
                .setCachePeriod(0); // 개발 중 캐시 비활성화

        // 2) /static/** 그대로 노출(명시적으로 유지)
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0);

        // 3) /upload/** → 파일시스템 매핑 (절대경로 변환 + 슬래시 보정)
        Path path = Paths.get(uploadDir).toAbsolutePath().normalize();
        String fileLocation = path.toUri().toString(); // 예) file:///C:/dev/LapPick/upload/
        if (!fileLocation.endsWith("/")) fileLocation = fileLocation + "/";

        registry.addResourceHandler("/upload/**")
                .addResourceLocations(fileLocation) // "file:" 접두사 포함된 URI 문자열
                .setCachePeriod(0);
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new mini.config.interceptor.AuthCheckInterceptor(Set.of("mem","emp")))
                .addPathPatterns(
                    "/myPage/**",
                    "/item/cartList",
                    "/memberUpdate",
                    "/memberPwModify",
                    "/memberDropOk"
                );

        registry.addInterceptor(new mini.config.interceptor.AuthCheckInterceptor(Set.of("emp")))
                .addPathPatterns(
                    "/employee/**",
                    "/goods/goodsForm", "/goods/goodsWrite",
                    "/goods/goodsModify/**", "/goods/goodsDelete",
                    "/goods/productsDelete", "/goods/goodsRedirect",
                    "/goodsIpgo/**"
                );
    }
}
    