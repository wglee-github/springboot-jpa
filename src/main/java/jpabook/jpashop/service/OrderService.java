package jpabook.jpashop.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final MemberRepository memberRepository;
	private final ItemRepository itemRepository;
	
	
	/**
	 * 주문
	 * - 주문하는 회원 식별자, 상품 식별자, 주문 수량 정보를 받아서 실제 주문 엔티티를 생성한 후 저장한다.
	 * 
	 * 
	 * cascade 설정을 범위
	 * 1. private owner 인 경우에만 사용
	 * - Delivery, OrderItem은 Order 객체만 참조해서 사용한다. 만약 Delivery, OrderItem가 다른 곳에서도 참조 하고 있다면 사용해서는 안된다. 
	 * - Delivery, OrderItem 처럼 private owner(한곳에서만 참조해서 사용하는 경우. 여기서는 Order만 참조)인 경우에만 cascade 설정을 사용해야 한다. 
	 * 2. persiste 라이프 사이클이 같은 경우에만 사용 
	 */   
	@Transactional
	public Long order(Long memberId, Long ItemId, int orderCount) {
		
		// 엔티티조회
		Member member = memberRepository.findOne(memberId);
		Item iItem = itemRepository.findOne(ItemId);
		
		// 배송정보 생성
		Delivery delivery = new Delivery();
		delivery.setAddress(member.getAddress());
		
		// 주문 상품 생성
		/**
		 * 
		 	• 주문 생성 시 '주문생성 스태틱 메소드'를 사용하여 추가하였다.
		 	• 만약 주문 생성을 하는 경우 아래와 같이 new 인스턴스를 생성해서 사용 하는 경우 주문 생성에 대한 로직이 분산된다.
		 			OrderItem orderItem = new OrderItem(); 
					orderItem.setOrderPrice(0);
			• 변경이슈가 생겼을 때 해당 로직을 다 찾아 다니면서 변경해줘야 한다. 이로인해 유지보수하기 어려워 진다. 
			• 게다가 주문생성에 대한 명확한 기준 없이 뒤죽박죽으로 개발할 여지가 있다.
			• 따라서 위와 같은 방식은 막아야 한다. 아래처럼 생성 메소드를 만들어서 관리해야 한다. 	 
			
			막는 방법
			1. OrderItem 객체의 생성자를 protected OrderItem() {} 로 만든다.
			2. @NoArgsConstructor(access = AccessLevel.PROTECTED) 를 class 위에 선언하다.
			- 1번과 2번은 같은 의미
		 * 
		 */
		OrderItem orderItem = OrderItem.creatOrderITem(iItem, iItem.getPrice(), orderCount);
		 
		// 주문 정보 생성
		Order order = Order.createOrder(member, delivery, orderItem);
		
		/**
		 * 주문 저장
		 * Order 객체 저장 시 cascade 걸려 있는 참조 객체도 같이 저장된다.
		 */ 
		orderRepository.save(order);
		
		return order.getId();
	}
	
	
	/**
	 * 취소
	 * - 주문 식별자를 받아서 주문 엔티티를 조회한 후 주문 엔티티에 주문 취소를 요청한다
	 * 
	 	• 주문 서비스의 주문과 주문 취소 메서드를 보면 비즈니스 로직 대부분이 엔티티에 있다. 
	 		서비스 계층은 단순히 엔티티에 필요한 요청을 위임하는 역할을 한다. 이처럼 엔티티가 비즈니스 로직을 가지고
			객체 지향의 특성을 적극 활용하는 것을 도메인 모델 패턴(http://martinfowler.com/eaaCatalog/domainModel.html)이라 한다. 
		• 반대로 엔티티에는 비즈니스 로직이 거의 없고 서비스 계층에서
			대부분의 비즈니스 로직을 처리하는 것을 트랜잭션 스크립트 패턴(http://martinfowler.com/eaaCatalog/transactionScript.html)이라 한다.
	 * 
	 */
	@Transactional
	public void cancelOrder(Long orderId) {
		// 주문 엔티티 조회
		Order order =  orderRepository.findOne(orderId);
		
		/**
		 *  주문 취소
		 *  - 더티체킹(변경내역감지)을 통해 JPA가 알아서 Order 와 Item 의 업데이트 쿼리가 DB에 날라간다.
		 */
		order.cancel();
		
	}
	
	
	/**
	 *  검색
	 */
	public List<Order> findOrders(OrderSearch orderSearch){
		return orderRepository.findAllByString(orderSearch);
	}
	
}
