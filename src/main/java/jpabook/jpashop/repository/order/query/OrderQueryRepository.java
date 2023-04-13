package jpabook.jpashop.repository.order.query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

	private final EntityManager em;

	/**
	 * 
	 */
	public List<OrderQueryDTO> findOrderQueryDTO() {
		List<OrderQueryDTO> result = findOrders();
		result.forEach(o -> {
			List<OrderItemQueryDto> orderItemQueryDtos = findOrderItems(o.getOrderId());
			o.setOrderItems(orderItemQueryDtos);
		});
		return result;
	}

	private List<OrderItemQueryDto> findOrderItems(Long orderId) {
		return em.createQuery(""
				+ "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"
				+ " from OrderItem oi"
				+ " join oi.item i"
				+ " where oi.order.id = :orderId", OrderItemQueryDto.class)
				.setParameter("orderId", orderId)
				.getResultList();
	}

	/**
	 * N+1 문제를 해결해보자. 
	 */
	public List<OrderQueryDTO> findAllByDTO_optimization() {
		// 1. OrderQueryDTO 조회
		List<OrderQueryDTO> result =  findOrders();
		
		// 2. orderid를 리스트로 추출 + 3. OrderItemQueryDTO in 조회 및 OrderItemQueryDTO -> Map 변환
		Map<Long, List<OrderItemQueryDto>> map = findOrderItemMap(toOrderIds(result));
		
		// 4. OrderQueryDTO에 OrderItemQueryDTO 셋팅
		result.forEach(o -> o.setOrderItems(map.get(o.getOrderId())));
		return result;
	}
	
	// orderid를 리스트로 추출
	private List<Long> toOrderIds(List<OrderQueryDTO> queryDTOs){
		return queryDTOs.stream()
				.map(o -> o.getOrderId())
				.collect(Collectors.toList());
	}
	
	// OrderItemQueryDTO in 조회 및 OrderItemQueryDTO -> Map 변환
	private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
		// 3. OrderItemQueryDTO in 조회
		List<OrderItemQueryDto> queryDtos = em.createQuery(""
				+ "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"
				+ " from OrderItem oi"
				+ " join oi.item i"
				+ " where oi.order.id in :orderIds", OrderItemQueryDto.class)
				.setParameter("orderIds", orderIds)
				.getResultList();
		
		
		// 3-1. OrderItemQueryDTO -> Map 변환
		return queryDtos.stream().collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));
	}
	
	// OrderQueryDTO 조회
	public List<OrderQueryDTO> findOrders() {
		return em.createQuery(""
				+ "select new jpabook.jpashop.repository.order.query.OrderQueryDTO(o.id, m.name, o.orderDate, o.status, d.address)"
				+ " from Order o"
				+ " join o.member m"
				+ " join o.delivery d", OrderQueryDTO.class)
				.getResultList();
	}

	public List<OrderFlatDTO> findAllByDTO_flat() {
		return em.createQuery(
				"select new jpabook.jpashop.repository.order.query.OrderFlatDTO(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)"
				+ " from Order o"
				+ "	join o.member m"
				+ " join o.delivery d"
				+ " join o.orderItems oi"
				+ " join oi.item i", OrderFlatDTO.class)
				.getResultList();
	}

	
}
