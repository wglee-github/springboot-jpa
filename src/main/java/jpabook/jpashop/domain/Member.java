package jpabook.jpashop.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Member {

	@Id @GeneratedValue
	@Column(name = "member_id")
	private Long id;
	
	private String name;
	
	@Embedded
	private Address address;
	
	/**
	 * 양방향 연관관계 
	 * mappedBy : 연관관계 주인에 의해 맵핑 된다는 의미이다. 따라서 누구에 의해 맵핑되었는지 연관관계 주인을 적어주면 된다. 
	 * 연관관계 주인이 아닌 곳에 설정하며, 연관관계의 주인 필드명으로 표시(FK를 설정한 필드명)
	 * 
	 */
	@OneToMany(mappedBy = "member")
	private List<Order> orders = new ArrayList<>();
	
}
