package jpabook.jpashop.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

	/**
	 * 1단계
	 */
//	@PersistenceContext
//	private EntityManager em;
	
	/**
	 * 최종단계
	 * spring data jpa 가  @PersistenceContext 대신 @Autowired 를 사용할 수 있다. 
	 * 즉, 여기서도 @RequiredArgsConstructor 를 사용할 수 있다는 말이다. 
	 * 
	 * 자세한 설명은 README 파일 읽자.
	 */
	private final EntityManager em;
	
	
	public void save(Member member) {
		em.persist(member);
	}
	
	public Member findOne(Long id) {
		return em.find(Member.class, id);
	}
	
	public List<Member> findAll(){
		return em.createQuery("select m from Member m",Member.class).getResultList();
	}
	
	public List<Member> findByName(String name){
		return em.createQuery("select m from Member m where m.name = :name",Member.class)
			.setParameter("name", name)
			.getResultList();
	}
}
