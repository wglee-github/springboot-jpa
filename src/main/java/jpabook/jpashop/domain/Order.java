package jpabook.jpashop.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

	@Id @GeneratedValue
	@Column(name = "order_id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)		// fetch LAZY : 지연로딩 (즉시로딩은 JPQL 사용 시 발생하는 N+1 문제가 자주 발생한다.)
	@JoinColumn(name = "member_id")		// FK 지정
	private Member member;
	
	/**
	 * 1:1 관계에서 FK를 어디에 둬야 하는가?
	 * 자주 사용하는 테이블에 두는것을 선호한다.
	 * cascade(영속성 전이 옵션) : ALL :  Order 객체가 persist 될 때 같이 persist 된다. Order 객체가 remove 되면 같이 remove 된다.
	 */
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "delivery_id")
	private Delivery delivery;

	/**
	 * cascade(영속성 전이 옵션) : ALL :  Order 객체가 persist 될 때 같이 persist 된다. Order 객체가 remove 되면 같이 remove 된다.
	 */
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
	private List<OrderItem> orderItems = new ArrayList<>();
	
	private LocalDateTime orderDate;	// 주문 시간
	
	@Enumerated(EnumType.STRING)
	private OrderStatus status;	// 주문상태 [ORDER, CANCEL]
	
	
	//=== 연관관계 편의 메소드 ===//
	public void setMember(Member member) {
		this.member = member;
		member.getOrders().add(this);
	}
	
	public void setDelivery(Delivery delivery) {
		this.delivery = delivery;
		delivery.setOrder(this);
	}
		
	public void addOrderItem(OrderItem orderItem) {
		orderItem.setOrder(this);
		orderItems.add(orderItem);
		
	}
	
	//=== 생성 메소드 ===//
	/**
	 * order의 연관관계 객체를 생성하는 메소드를 만들자.
	 * 주문 생성은 해당 메소드를 통해서만 하도록 한다.
	 * 변경이 발생한 경우 해당 메소드만 수정해 주면 된다.
	 */
	public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
		Order order = new Order();
		order.setMember(member);
		order.setDelivery(delivery);
		for (OrderItem orderItem : orderItems) {
			order.getOrderItems().add(orderItem);
		}
		order.setStatus(OrderStatus.ORDER);
		order.setOrderDate(LocalDateTime.now());
		
		return order;
	}
	
	
	//=== 비지니스 로직 ===//
	/**
	 * 주문 취소
	 */
	public void cancel() {
		// 배송 상태인 경우 주문 취소 안됨.
		if(DeliveryStatus.COMP.equals(delivery.getStatus())) {
			throw new IllegalStateException("이미 배송 완료된 상품은 취소가 불가능 합니다.");
		}

		// 주문 상태를 cancel로 변경
		this.setStatus(OrderStatus.CANCEL);
		
		// orderitem cancel 처리, (재고 수량 원복)
		for (OrderItem orderItem : orderItems) {
			orderItem.cancel();
		}
		
	}
	
	
	
	// === 조회 로직 ===// 
	/**
	 * 전체 주문 가격 조회 
	 */
	public int getTotalPrice() {
		
//		int totalPrice = 0;
//		for (OrderItem orderItem : orderItems) {
//			totalPrice += orderItem.getTotalPrice();
//		}
//		
//		return totalPrice;
		
		return orderItems.stream()
					.mapToInt(OrderItem::getTotalPrice)
					.sum();
	}
	
}
