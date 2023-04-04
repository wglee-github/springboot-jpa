package jpabook.jpashop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jpabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

	@Id @GeneratedValue
	@Column(name = "order_item_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id")
	private Item item;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id")
	private Order order;
	
	private int orderPrice;	// 주문 가격
	private int count;			// 주문 수량
	
	
	
	
	// === 생성메소드 ===//
	/**
	 * 주문상품 저장 및 재고 수량 차감
	 *   
	 */
	public static OrderItem creatOrderITem(Item item, int orderPrice, int count) {
		OrderItem orderItem = new OrderItem();
		orderItem.setItem(item);
		orderItem.setOrderPrice(orderPrice);
		orderItem.setCount(count);
		
		item.removeStock(count);
		
		return orderItem;
	}
	
	
	//=== 비지니스 로직 ===//
	/**
	 * 재고 수량 원복 
	 */ 
	public void cancel() {
		getItem().addStock(count);
	}

	//=== 조회 로직 ===//
	/**
	 *  주문 상품 전체 가격 조회
	 */
	public int getTotalPrice() {
		return getOrderPrice() * getCount();
	}


	public OrderItem(int count) {
		super();
		this.count = count;
	}
	
}
