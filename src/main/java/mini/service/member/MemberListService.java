package mini.service.member;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import lombok.RequiredArgsConstructor;
import mini.domain.MemberDTO;
import mini.domain.MemberListPage;
import mini.domain.StartEndPageDTO;
import mini.mapper.MemberMapper;
import mini.service.StartEndPageService;

@Service
@RequiredArgsConstructor
public class MemberListService {
	@Autowired
	MemberMapper memberMapper;
	@Autowired
	StartEndPageService startEndPageService;
	

    // 컨트롤러가 호출하는 시그니처 그대로 제공
    public String execute(Integer page, Integer size, String searchWord, Model model) {
        MemberListPage pd = getPage(page, size, searchWord);
        model.addAttribute("members", pd.getItems());
        model.addAttribute("page", pd.getPage());
        model.addAttribute("size", pd.getSize());
        model.addAttribute("totalPage", pd.getTotalPages());
        model.addAttribute("searchWord", pd.getSearchWord());
        // 실제 파일 위치에 맞춘 뷰 이름
        return "thymeleaf/employee/memberList";
    }
    

    public MemberListPage getPage(Integer page, Integer size, String searchWord) {
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? 10 : size;

        long startRow = (p - 1L) * s + 1;
        long endRow   = p * 1L * s;

        // ★ 기존 코드에 endRow,endRow 로 들어가던 버그도 함께 교정
        StartEndPageDTO sep = new StartEndPageDTO(startRow, endRow, searchWord);
        sep.setStartRow(startRow);
        sep.setEndRow(endRow);
        sep.setSearchWord(searchWord);

        List<MemberDTO> list = memberMapper.memberSelectList(sep);
        int total = memberMapper.memberCountBySearch(searchWord);
        int totalPages = (int)Math.ceil(total / (double)s);

        return MemberListPage.builder()
                .items(list)
                .page(p).size(s)
                .total(total).totalPages(totalPages)
                .searchWord(searchWord)
                .startRow(startRow).endRow(endRow)
                .build();
    }
}