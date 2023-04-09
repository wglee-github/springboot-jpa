package jpabook.jpashop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.api.UpdateMemberRequest;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

@Service 
/**
 * 
 * @Transactional
 * - class에 선언하면 public으로 작성된 메소드에 모두 적용해 준다.
 * - method에 선언하는게 우선순위가 된다.
 * 
 * readOnly = true
 * -  DB 조회를 최적화 해준다. 단, 조회만 가능하다.  readOnly 옵션 default는 false 이다
 * 
 * 
 * - 기본 적으로 조회를 많이 한다면 class 에 @Transactional(readOnly = true)	를 걸고 
 *   저장 메소드에 @Transactional 를 따로 걸어주면 된다. 	
 * 
 */
@Transactional(readOnly = true)	
@RequiredArgsConstructor	// README 파일에 적어놓음.
public class MemberService {

	 
	/**
	 * 이렇게 사용할 수 있는 이뉴는 README 파일을 읽을 것.
	 */
	private final MemberRepository memberRepository;
	
	
	/**
	 * 회원 등록 
	 */
	@Transactional
	public Long join(Member member) {
		validateDuplicateMember(member);
		memberRepository.save(member);
		return member.getId();
	}

	/**
	 * 회원 중복 체크
	 * @param member
	 */
	private void validateDuplicateMember(Member member) {
		/**
		 * 멀티쓰레드 환경에서 같은 이름이 동시에 DB insert를 하게 되면 문제가 생길 수 있다. 
		 * 따라서 DB에 name을 유니크 제약조건으로 걸어주면 좋다.
		 */
		List<Member> findMembers = memberRepository.findByName(member.getName());
		
		if(!findMembers.isEmpty()) {
			throw new IllegalStateException("이미 존재하는 회원입니다.");
		}
	}
	
	/**
	 * 회원 수정 API 
	 * CQS(Comman Query Separation)
	 * - comman와 query를 분리하자.
	 * - update 메소드는 반환값이 없는 void를 지향하자.
	 */
	@Transactional
	public void updateMember(Long id, UpdateMemberRequest memberReq) {
		Member member = memberRepository.findOne(id);
		member.setName(memberReq.getName());
		member.setAddress(new Address(memberReq.getCity(), memberReq.getStreet(), memberReq.getZipcode()));
	}
	
	/**
	 * 회원 전체 조회
	 */
	public List<Member> findMembers(){
		return memberRepository.findAll();
	}
	
	/**
	 * 회원 단건 조회
	 */
	public Member findOne(Long memberId) {
		return memberRepository.findOne(memberId);
	}
	
}
