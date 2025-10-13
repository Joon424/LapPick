package lappick.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("banner")
public class BannerController {

    @GetMapping("faq")
    public String faq() {
        // FAQ 페이지로 연결
        return "thymeleaf/banner/FAQ";
    }

    @GetMapping("notice")
    public String notice() {
        // 공지사항 페이지로 연결
        return "thymeleaf/banner/notice";
    }

    @GetMapping("brand")
    public String brand() {
        // 브랜드 페이지로 연결
        return "thymeleaf/banner/brand";
    }
}
