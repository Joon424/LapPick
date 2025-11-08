package lappick.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("banner")
public class BannerController {

    @GetMapping("faq")
    public String faq() {
        return "user/common/faq";
    }

    @GetMapping("notice")
    public String notice() {
        return "user/common/notice";
    }

    @GetMapping("brand")
    public String brand() {
        return "user/common/brand";
    }
}