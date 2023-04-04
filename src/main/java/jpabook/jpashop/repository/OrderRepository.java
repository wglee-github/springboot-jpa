package jpabook.jpashop.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

	
	private final EntityManager em;

	/**
	 * 주문 저장  
	 */
	public void save(Order order) {
		em.persist(order);
	}
	
	/**
	 *	주문 단건 조회 
	 */
	public Order findOne(Long orderId) {
		
		return em.find(Order.class, orderId);
	}
	
	/**
	 * 주문 전체 조회
	 */
	public List<Order> findAll(){
		return em.createQuery("select o from Order o", Order.class).getResultList();
	}
}
