package mini.controller;

import lombok.RequiredArgsConstructor;
import mini.command.PageData;
import mini.domain.QnaDTO;
import mini.service.qna.QnaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employee/qna")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_EMP')")
public class EmployeeQnaController {

    private final QnaService qnaService;

 // [교체] qnaList 메소드
    @GetMapping
    public String qnaList(@RequestParam(value = "searchWord", required = false) String searchWord,
                          @RequestParam(value = "status", required = false) String status, // [추가] status 파라미터
                          @RequestParam(value = "page", defaultValue = "1") int page,
                          Model model) {
        int size = 5;
        PageData<QnaDTO> pageData = qnaService.getAllQnaList(searchWord, status, page, size);

        model.addAttribute("pageData", pageData);
        model.addAttribute("qnaList", pageData.getItems());
        model.addAttribute("status", status); // [추가] 뷰에서 현재 필터 상태를 알 수 있도록 전달
        
        return "thymeleaf/employee/empQnaList";
    }

 // [교체] addAnswer 메소드
    @PostMapping("/answer")
    public String addAnswer(@RequestParam("qnaNum") int qnaNum,
                            @RequestParam("answerContent") String answerContent,
                            // [추가] 돌아갈 URL을 받기 위한 파라미터. 필수는 아님.
                            @RequestParam(value = "returnUrl", required = false) String returnUrl,
                            RedirectAttributes ra) {
        qnaService.addAnswer(qnaNum, answerContent);
        ra.addFlashAttribute("message", qnaNum + "번 문의에 답변이 등록되었습니다.");
        
        // [수정] returnUrl이 있으면 해당 경로로, 없으면 기존 경로로 리다이렉트
        if (returnUrl != null && !returnUrl.isEmpty()) {
            return "redirect:" + returnUrl;
        }
        return "redirect:/employee/qna";
    }
}