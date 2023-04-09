package jpabook.jpashop.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;


@SpringBootTest
@Transactional 		// 테스트 환경에서 @Transactional 은 마지막에 DB를 rollback 한다.
//@Rollback(false)	// rollback 안하는 어노테이션 default true, false를 주면 rollback 안함. 
class MemberServiceTest {

	
	@Autowired MemberRepository memberRepository;
	@Autowired MemberService memberService;
	@Autowired EntityManager em;
	
//	@Test
	public void 회원가입() throws Exception{
		
		// given
		Member member = new Member();
		member.setName("회원1");
		
		// when
		Long id = memberService.join(member);
		
//		em.flush();	// DB에 SQL을 보내는 명령어 
		
		// then
		assertEquals(member, memberService.findOne(id));
	}

	/**
	 * 
	 * 예외 테스트
	 * 
	 * @throws Exception
	 */
//	@Test
	public void 중복_회원_예외() throws Exception{
		
		// given
		Member member1 = new Member();
		member1.setName("회원1");
		
		Member member2 = new Member();
		member2.setName("회원1");
		
		// when
		memberService.join(member1);
//		memberService.join(member2);
		
		
		// then
		// 1번 방법
//		Assertions.assertThrows(IllegalStateException.class, () -> {
//			memberService.join(member2);
//		}); 
		// 2번 방법
		assertThatThrownBy(() -> memberService.join(member2)).isInstanceOf(IllegalStateException.class);
	}


	@Test
	public void 회워조회() {
		List<Member> members = memberRepository.findAll();
		
		members.stream().forEach(m -> System.out.println("======= Member 조회 : " + m));
	}
}
