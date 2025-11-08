package lappick.web.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import lappick.common.service.FileDelService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CheckRestController {

    private final FileDelService fileDelService;

    @PostMapping("/file/fileDel")
    public int fileDel(String orgFile, String storeFile, HttpSession session) {
         return  fileDelService.execute(orgFile, storeFile, session);
    }
}