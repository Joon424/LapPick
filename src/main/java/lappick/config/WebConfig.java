package lappick.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Bean("jsonView")
    public MappingJackson2JsonView jsonView() {
        return new MappingJackson2JsonView();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("classpath:/static/", "classpath:/resources/")
                .setCachePeriod(0);

        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0);

        // OS 독립적인 파일 경로를 위해 "file:///" 접두사 및 / 구분자 사용
        String resourcePath = "file:///" + uploadDir.replace("\\", "/");
        
        // (디버깅용) 서버 시작 시 실제 매핑된 파일 경로를 콘솔에 출력
        System.out.println(">>> [WebConfig] 파일 리소스 경로 확인: " + resourcePath); 
        
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(resourcePath)
                .setCachePeriod(0);
    }
}