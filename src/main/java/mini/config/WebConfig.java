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
	
	private String resourcePath = "file:///C:/lappick/upload/";

	 @Value("${file.upload.dir}")
	    private String uploadDir;

    @Bean("jsonView")
    public MappingJackson2JsonView jsonView() {
        return new MappingJackson2JsonView();
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 기존 정적 리소스 설정
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("classpath:/static/", "classpath:/resources/")
                .setCachePeriod(0);

        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0);
        registry.addResourceHandler("/upload/**")
        .addResourceLocations(resourcePath);

        // 2. [수정] 업로드 폴더 매핑 방식을 더 명확하게 변경하고, 확인용 로그를 추가합니다.
        String resourcePath = "file:///" + uploadDir.replace("\\", "/");
        
        // 서버 시작 시 콘솔에 이 메시지가 찍히는지 확인해주세요.
        System.out.println(">>> 파일 리소스 경로 확인: " + resourcePath); 
        
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(resourcePath)
                .setCachePeriod(0);
    }
    
}
    