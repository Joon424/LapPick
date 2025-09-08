// src/main/java/mini/config/StaticResourceConfig.java
package mini.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 과거 템플릿의 /resources/** 를 실제 static 등에서 찾게 함
        registry.addResourceHandler("/resources/**")
                .addResourceLocations(
                        "classpath:/static/",           // 네가 실제 쓰는 위치
                        "classpath:/resources/",        // 혹시 남아있는 파일 대비
                        "classpath:/public/",
                        "classpath:/META-INF/resources/"
                )
                .setCachePeriod(0);

        // /static/** 도 그대로 서빙
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0);

        // 업로드 파일(동적)은 보통 파일시스템 경로여야 함
        // 예) 프로젝트 실행 디렉토리 아래 ./upload 폴더
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:./upload/")   // 실제 저장 위치에 맞게 조정
                .setCachePeriod(0);
    }
}
