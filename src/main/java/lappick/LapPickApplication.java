package lappick;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lappick.service.member.MemberService;

@SpringBootApplication
@Controller
public class LapPickApplication {

    public static void main(String[] args) {
        SpringApplication.run(LapPickApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(MemberService memberService) {
        return args -> {
            // memberService.migratePasswords();
            // System.out.println("비밀번호 마이그레이션 완료.");
        };
    }
}
