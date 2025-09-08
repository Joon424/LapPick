package mini.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import mini.command.CartCommand;
import mini.service.item.CartInsertService;
import mini.service.item.CartQtyDownService;
import mini.service.item.GoodsCartDelsService;
import mini.service.item.GoodsWishService;

@RestController  /// Rest API
@RequestMapping("item")
public class ItemRestController {
	@Autowired
	GoodsWishService goodsWishService;
	@Autowired
	CartInsertService cartInsertService;
	@Autowired
	CartQtyDownService cartQtyDownService;
	@Autowired
	GoodsCartDelsService goodsCartDelsService;
	
	@RequestMapping("wishItem")
	public void wishAdd(@RequestBody Map<String, Object> map,HttpSession session) {
		goodsWishService.execute(map.get("goodsNum").toString(), session);
	}
	@RequestMapping("cartAdd")
	public String cartAdd(@RequestBody CartCommand cartCommand
			, HttpSession session) {
		System.out.println(cartCommand.getGoodsNum());
		System.out.println("Received goodsNum: " + cartCommand.getGoodsNum());
	    System.out.println("Received qty: " + cartCommand.getQty());
		return cartInsertService.execute(cartCommand, session);
	}
	@GetMapping("cartQtyDown")
	public void cartQtyDown(String goodsNum, HttpSession session )  {
		cartQtyDownService.execute(goodsNum, session);
	}
	@PostMapping("cartDels")
	public String cartDels(@RequestBody String goodsNums[],  HttpSession session ) {
		return goodsCartDelsService.execute(goodsNums, session);
	}
	 @GetMapping("isLoggedIn")
	    public boolean isLoggedIn(HttpSession session) {
	        return session.getAttribute("auth") != null; // 세션에 로그인 정보(auth)가 있으면 true 반환
	    }
}