package jpabook.jpashop.repository.order.simplequery;

import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderSimpleRepository {

	private final EntityManager em;
	
	public List<OrderSimpleQueryDTO> findOrderDTOs() {
		return em.createQuery(
				"select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDTO(o.id, m.name, o.orderDate, o.status, d.address)  from Order o"
				+ " join o.member m"
				+ " join o.delivery d" , OrderSimpleQueryDTO.class)
		.getResultList();
	}
}
