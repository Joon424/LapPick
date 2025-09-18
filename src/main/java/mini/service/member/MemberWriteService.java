package mini.service.member;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mini.command.MemberCommand;
import mini.domain.MemberDTO;
import mini.mapper.MemberMapper;

@Service
@RequiredArgsConstructor
public class MemberWriteService {
	@Autowired
	MemberMapper memberMapper;
	@Autowired
	PasswordEncoder passwordEncoder;
	
    public void save(MemberDTO dto) {
        if (memberMapper.existsByMemberId(dto.getMemberId()) > 0) {
            throw new DuplicateKeyException("이미 사용중인 아이디입니다.");
        }
        memberMapper.memberInsert(dto);
    }
    // 호환용: 기존 컨트롤러들이 execute/write를 부르면 아래가 save로 위임
    public void execute(MemberDTO dto) { save(dto); }
    public void write(MemberDTO dto)   { save(dto); }
    
	}