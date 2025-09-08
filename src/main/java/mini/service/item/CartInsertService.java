package mini.service.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import mini.command.CartCommand;
import mini.domain.AuthInfoDTO;
import mini.domain.CartDTO;
import mini.mapper.CartMapper;
import mini.mapper.MemberMapper;

@Service
public class CartInsertService {
	@Autowired
	MemberMapper memberMapper;
	@Autowired
	CartMapper cartMapper;
	public String execute(CartCommand cartCommand, HttpSession session) {
		AuthInfoDTO auth = (AuthInfoDTO)session.getAttribute("auth");
		String memberNum = memberMapper.memberNumSelect(auth.getUserId());
		try {
			memberNum = memberMapper.memberNumSelect(auth.getUserId());
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("Auth is null");

			return "000";  // session이 없다
		}
		if(memberNum == null) {
			System.out.println("MemberNum is null");
			return "900";
		}else {
			CartDTO dto = new CartDTO();
			dto.setCartQty(cartCommand.getQty());
			dto.setGoodsNum(cartCommand.getGoodsNum());
			dto.setMemberNum(memberNum);
			System.out.println("Cart Insert Data: " + dto);
			cartMapper.cartMerge(dto);
			return "200";
		}
		
	}
}