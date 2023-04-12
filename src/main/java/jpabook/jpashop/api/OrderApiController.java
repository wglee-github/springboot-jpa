package jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
	
	/**
	 * 
	 *
	 	* 지연 로딩으로 너무 많은 SQL 실행
			· SQL 실행 수
				· order 1번
				· member , address N번(order 조회 수 만큼)
				· orderItem N번(order 조회 수 만큼)
				· item N번(orderItem 조회 수 만큼)
		
		* 참고: 
			· 지연 로딩은 영속성 컨텍스트에 있으면 영속성 컨텍스트에 있는 엔티티를 사용하고 없으면 SQL을 실행한다. 
			· 따라서 같은 영속성 컨텍스트에서 이미 로딩한 회원 엔티티를 추가로 조회하면 SQL을 실행하지 않는다
	 */
	@GetMapping("/api/v2/orders")
	public List<OrderDTO> ordersV2(){
		List<Order> orders = orderRepository.findAllByString(new OrderSearch());
		List<OrderDTO> result = orders.stream()
				.map(o -> new OrderDTO(o))
				.collect(Collectors.toList());
		
		return result;
	}
	
	/**
	 * 
	 	· hibernate 6 이후 부터는 fetch 조인 시 distinct 하지 않아도 중복 제거 해준다.
	 	
	 	· 컬렉션 페치 조인을 페이징하는 경우 SQL에서 페이징 처리를 하지 않는다.
	 	· 하이버네이트는 경고 로그를 남기면서 모든 데이터를 DB에서 읽어오고, 메모리에서 페이징 해버린다(매우 위험하다) 
	 		· ERROR : firstResult/maxResults specified with collection fetch; applying in memory
	 	
	 	· 컬렉션 페치 조인은 1개만 사용할 수 있다. 컬렉션 둘 이상에 페치 조인을 사용하면 안된다.	
	 */
	@GetMapping("/api/v3/orders")
	public List<OrderDTO> ordersV3(){
		List<Order> orders = orderRepository.findAllWithItem();
		List<OrderDTO> result =  orders.stream()
				.map(o -> new OrderDTO(o))
				.collect(Collectors.toList());
		
		return result;
	}
	
	/**
	 *
	 * 
	 	* 컬렉션을 페치 조인하면 페이징이 불가능하다.
		· 컬렉션을 페치 조인하면 일대다 조인이 발생하므로 데이터가 예측할 수 없이 증가한다.
		일다대에서 일(1)을 기준으로 페이징을 하는 것이 목적이다. 그런데 데이터는 다(N)를 기준으로 row
		가 생성된다.
		Order를 기준으로 페이징 하고 싶은데, 다(N)인 OrderItem을 조인하면 OrderItem이 기준이
		되어버린다.
		(더 자세한 내용은 자바 ORM 표준 JPA 프로그래밍 - 페치 조인 한계 참조)
		이 경우 하이버네이트는 경고 로그를 남기고 모든 DB 데이터를 읽어서 메모리에서 페이징을 시도한다. 
		최악의 경우 장애로 이어질 수 있다.
	 * 
	 */
	@GetMapping("/api/v3.1/orders")
	public List<OrderDTO> ordersV3_page(
			@RequestParam(value = "offset", defaultValue = "0") int offset,
			@RequestParam(value = "limit", defaultValue = "100") int limit)
	{
		List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
		List<OrderDTO> result =  orders.stream()
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
			
			/**
			 * orderItemDTO 변환
			 */
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
