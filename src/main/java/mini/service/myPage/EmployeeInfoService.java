package mini.service.myPage;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpSession;
import mini.domain.AuthInfoDTO;
import mini.domain.EmployeeDTO;
import mini.mapper.EmployeeInfoMapper;

@Service
public class EmployeeInfoService {
	@Autowired
	EmployeeInfoMapper employeeInfoMapper; 
	public Map<String, Object> execute(HttpSession session, Model model) {
		Map<String, Object> map = new HashMap<String, Object>();
		AuthInfoDTO auth = (AuthInfoDTO)session.getAttribute("auth");
		EmployeeDTO dto = employeeInfoMapper.employeeSelectOne(auth.getUserId());
		//model.addAttribute("employeeCommand", dto);
		map.put("employeeCommand", dto);
		return map;
	}
}