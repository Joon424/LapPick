package lappick.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // application.properties에서 파일 업로드 경로를 주입받습니다.
    @Value("${file.upload.dir}")
    private String uploadDir;

    @Bean("jsonView")
    public MappingJackson2JsonView jsonView() {
        return new MappingJackson2JsonView();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 기존 정적 리소스 설정 (/resources/**, /static/**)
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("classpath:/static/", "classpath:/resources/")
                .setCachePeriod(0);

        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0);

        // 2. [수정] 업로드된 이미지 파일에 대한 URL 매핑 (중복 제거 및 최종본)
        // file:/// 접두사와 OS에 맞는 경로 구분자 처리를 통해 안정적으로 경로를 설정합니다.
        String resourcePath = "file:///" + uploadDir.replace("\\", "/");
        
        // 서버 시작 시 콘솔에 이 메시지가 찍히는지 확인하여 경로가 올바른지 검증할 수 있습니다.
        System.out.println(">>> [WebConfig] 파일 리소스 경로 확인: " + resourcePath); 
        
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(resourcePath)
                .setCachePeriod(0);
    }
}