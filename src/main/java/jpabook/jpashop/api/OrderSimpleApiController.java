package jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDTO;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * 
 * XToOne(ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 * 
 * 
	※ 쿼리 방식 선택 권장 순서
		1. 우선 엔티티를 DTO로 변환하는 방법을 선택한다. -> V2
		2. 필요하면 페치 조인으로 성능을 최적화 한다. 대부분의 성능 이슈가 해결된다. -> V3
		3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다. -> V4
		4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사용한다.
 * 
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

	private final OrderRepository orderRepository;
	private final OrderSimpleRepository orderSimpleRepository;
	
	
	/**
	 *
	 *  
	 * V1 -> 엔티티를 직접 노출 
	 	1. 엔티티를 직접 노출할 때는 양방향 연관관계가 걸린 곳은 꼭! 한곳을 @JsonIgnore 처리 해야 한다. 
	   	· jackson 라이브러리에서 json으로 파싱하는 과정에서 순환참조 에러가 발생한다. (무한루프 발생)
 			· ERROR : Cannot call sendError() after the response has been committed
 		
	 	2. 엔티티를 직접 노출하는 것은 좋지 않다.
		· order member 와 order address 는 지연 로딩이다. 따라서 실제 엔티티 대신에 프록시 존재
		· jackson 라이브러리는 기본적으로 이 프록시 객체를 json으로 어떻게 생성해야 하는지 모름 예외 발생
		· Hibernate5Module 을 스프링 빈으로 등록하면 해결(스프링 부트 사용중)
			· 스프링 부트 3.0 이상: Hibernate5JakartaModule 등록
				· build.gradle 에 다음 라이브러리를 추가하자
				· implementation 'com.fasterxml.jackson.datatype:jackson-datatype-hibernate5-jakarta'
				· 스프링 부트 3.0 이상이면 Hibernate5Module 대신에 Hibernate5JakartaModule 을 사용해야 한다.
		
		
		※ 참고 
		· 앞에서 계속 강조했듯이 정말 간단한 애플리케이션이 아니면 엔티티를 API 응답으로 외부로 노출하는 것은 좋지 않다. 
		· 따라서 Hibernate5Module 를 사용하기 보다는 DTO로 변환해서 반환하는 것이 더 좋은 방법이다.


		※ 주의 
		· 지연 로딩(LAZY)을 피하기 위해 즉시 로딩(EARGR)으로 설정하면 안된다! 
		· 즉시 로딩 때문에 연관관계가 필요 없는 경우에도 데이터를 항상 조회해서 성능 문제가 발생할 수 있다. 
		· 즉시 로딩으로 설정하면 성능 튜닝이 매우 어려워 진다.
		· 항상 지연 로딩을 기본으로 하고, 성능 최적화가 필요한 경우에는 페치 조인(fetch join)을 사용해라!(V3에서 설명)
		
	 */
	@GetMapping("/api/v1/simple-orders")
	public List<Order> ordersV1(){
		List<Order> all  = orderRepository.findAllByString(new OrderSearch());
		for (Order order : all) {
			order.getMember().getName();		// LAZY 강제 초기화
			order.getDelivery().getAddress();	// LAZY 강제 초기화
		}
		return all;
	}
	
	
	/**
	 *
	 * V2 -> 엔티티를 DTO로 변환
	 	· 엔티티를 DTO로 변환하는 일반적인 방법이다.
		· 쿼리가 총 1 + N + N번 실행된다. (v1과 쿼리수 결과는 같다)
			· order 조회 1번(order 조회 결과 수가 N이 된다.)
			· order -> member 지연 로딩 조회 N 번
			· order -> delivery 지연 로딩 조회 N 번
			· 예) order의 결과가 4개면 최악의 경우 1 + 4 + 4번 실행된다.(최악의 경우)
				· 지연로딩은 영속성 컨텍스트에서 조회하므로, 이미 조회된 경우 쿼리를 생략한다
	 */
	@GetMapping("/api/v2/simple-orders")
	public List<SimpleOrderDto> ordersV2(){
		List<Order> orders  = orderRepository.findAllByString(new OrderSearch());
		return orders.stream()
				.map(o -> new SimpleOrderDto(o))
//				.map(SimpleOrderDto::new)
				.collect(Collectors.toList());
	}
	
	/**
	 * 
	 * V3 -> 엔티티를 DTO로 변환 - 페치 조인 최적화
	 	· 엔티티를 페치 조인(fetch join)을 사용해서 쿼리 1번으로 조회가 가능하다.
		· 페치 조인으로 order -> member , order -> delivery 는 이미 조회 된 상태이므로 지연로딩이 실행되지 않는다. 
	 */
	@GetMapping("/api/v3/simple-orders")
	public List<SimpleOrderDto> ordersV3(){
		List<Order> orders = orderRepository.findAllWithMemberDelivery();
		return orders.stream()
//				.map(o -> new SimpleOrderDto(o))
				.map(SimpleOrderDto::new)
				.collect(Collectors.toList());
	}
	
	/**
	 * 
	 * V4 -> 리포지토리에서 DTO 직접 조회
	 	· 일반적인 SQL을 사용할 때 처럼 원하는 값을 선택해서 조회한다.
		· new 명령어를 사용해서 JPQL의 결과를 DTO로 즉시 변환한다.
		· SELECT 절에서 원하는 데이터를 직접 선택하므로 DB -> 애플리케이션 네트웍 용량의 최적화가 가능하다(생각보다 미비)
		· 가장 큰 트레이드오프는 리포지토리 재사용성 떨어진다. 
		· API 스펙에 맞춘 코드가 리포지토리에 들어가는 단점이 있다. 
		
		※ 참고
			· 리포지토리에서는 엔티티만 조회 하는걸 추천한다.
			· 성능 최적화를 위해 FETCH 조인 정도만 하는게 좋다.
		
		
		※ 정리
			· 엔티티를 DTO로 변환하거나, DTO로 바로 조회하는 두가지 방법은 각각 장단점이 있다. 둘중 상황에
			· 따라서 더 나은 방법을 선택하면 된다. 엔티티로 조회하면 리포지토리 재사용성도 좋고, 개발도 단순해진다. 
			· 따라서 권장하는 방법은 다음과 같다. 
		
	 */
	@GetMapping("/api/v4/simple-orders")
	public List<OrderSimpleQueryDTO> ordersV4(){
		return orderSimpleRepository.findOrderDTOs();
	}
	
	@Data
	static class SimpleOrderDto {
		private Long orderId;
		private String name;
		private LocalDateTime orderDate;
		private OrderStatus orderStatus;
		private Address address;
		
		public SimpleOrderDto(Order order) {
			this.orderId = order.getId();
			this.name = order.getMember().getName();
			this.orderDate = order.getOrderDate();
			this.orderStatus = order.getStatus();
			this.address = order.getDelivery().getAddress();
		}
	}
	
	

	
}
