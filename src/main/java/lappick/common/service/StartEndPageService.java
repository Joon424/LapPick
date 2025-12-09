package lappick.common.service;

import java.util.List;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;

import lappick.common.dto.StartEndPageDTO;

@Service
public class StartEndPageService {

	private static final Logger log = LoggerFactory.getLogger(StartEndPageService.class);
	public StartEndPageDTO execute(int page, int limit, String searchWord) {
		int startRow = ((page - 1) * limit) + 1 ;
		int endRow = startRow + limit - 1;
		StartEndPageDTO sepDTO = new StartEndPageDTO(startRow, endRow, searchWord);
		return sepDTO;
	}
	public void execute(int page, int limit, int count,String searchWord
			, List list, Model model) {
		int limitPage = 10 ;
		int startPageNum = (int)((double) page / limitPage + 0.95 - 1) * limitPage + 1;
		int endPageNum = startPageNum + limitPage - 1;
		int maxPage = (int)((double)count / limit + 0.95);
		if(endPageNum > maxPage) endPageNum = maxPage;
		if(endPageNum == 0) endPageNum = 1;
		if(searchWord == null) searchWord="";
		model.addAttribute("list", list);
		model.addAttribute("searchWord", searchWord);
		model.addAttribute("page", page);
		model.addAttribute("startPageNum", startPageNum);
		model.addAttribute("endPageNum", endPageNum);
		model.addAttribute("count", count);
		model.addAttribute("maxPage", maxPage);
	}
}