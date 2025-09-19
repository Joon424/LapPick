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

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:./upload}")
    private String uploadDir;


    @Bean("jsonView")
    public MappingJackson2JsonView jsonView() {
        return new MappingJackson2JsonView();
    }

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
    
}
    