package jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

	private final OrderRepository orderRepository;
	
	/**
	 * V1. 엔티티 직접 노출
	 * - Hibernate5Module 모듈 등록하면, LAZY=null 처리하여 LAZY 로딩 참조 객체의 proxy Error 해결
	 * - 양방향 관계 문제 발생 -> @JsonIgnore
	 */
	@GetMapping("/api/v1/orders")
	public List<Order> ordersV1(){
		List<Order> all = orderRepository.findAllByString(new OrderSearch());
		for (Order order : all) {
			order.getMember().getName();		// LAZY 강제 초기화
			order.getDelivery().getAddress();	// LAZY 강제 초기화
			List<OrderItem> orderItems = order.getOrderItems();	// LAZY 강제 초기화
			orderItems.stream().forEach(o -> o.getItem().getName());	// LAZY 강제 초기화
		}
		return all;
	}
	
	@GetMapping("/api/v2/orders")
	public List<OrderDTO> ordersV2(){
		List<Order> orders = orderRepository.findAllByString(new OrderSearch());
		List<OrderDTO> result = orders.stream()
				.map(o -> new OrderDTO(o))
				.collect(Collectors.toList());
		
		return result;
	}
	
	@Data
	static class OrderDTO{
		private Long id;
		private String name;
		private LocalDateTime orderDate;
		private OrderStatus status;
		private Address address;
		private List<OrderItemDTO> orderItems;
		
		public OrderDTO(Order order) {
			this.id = order.getId();
			this.name = order.getMember().getName();
			this.orderDate = order.getOrderDate();
			this.status = order.getStatus();
			this.address = order.getDelivery().getAddress();
			/**
			 * 이렇게 하면 안된다. orderItems도 DTO로 변환해줘야 한다.
			 */
//			this.orderItems = order.getOrderItems();	// <- 이렇게만 해줘도 orderItems는 LAZY 초기화와 되었었음(강의랑 다른 부분) 단, item은 나오지 않음.
//			order.getOrderItems().stream().map(o -> o.getItem().getName()).collect(Collectors.toList());	// item 나오게 하는 방법
			
			this.orderItems = order.getOrderItems().stream()
					.map(orderItem -> new OrderItemDTO(orderItem))
					.collect(Collectors.toList());
		}
	}
	
	@Data
	static class OrderItemDTO{
		private String itemName;	// 상품명
		private int orderPrice;		// 주문 가격
		private int count; 			// 주문 수량

		public OrderItemDTO(OrderItem orderItem) {
			this.itemName = orderItem.getItem().getName();
			this.orderPrice = orderItem.getOrderPrice();
			this.count = orderItem.getCount();
		}
		
	}
}
