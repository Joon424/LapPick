package mini.service.member;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mini.mapper.MemberMapper;

@Service
@RequiredArgsConstructor
public class MemberDeleteService {
	@Autowired
	MemberMapper memberMapper;

    /** 기존 컨트롤러 호환용 */
    @Transactional
    public int execute(String memberNum) {
        return deleteOne(memberNum);
    }

    /** 직원 단건 삭제 */
    @Transactional
    public int deleteOne(String memberNum) {
        if (memberNum == null || memberNum.isBlank()) return 0;
        return memberMapper.memberDeleteOne(memberNum);
    }

    /** 직원 다건(선택) 삭제 */
    @Transactional
    public int deleteMany(List<String> memberNums) {
        if (memberNums == null || memberNums.isEmpty()) return 0;
        return memberMapper.memberDeleteMany(memberNums);
    }



}