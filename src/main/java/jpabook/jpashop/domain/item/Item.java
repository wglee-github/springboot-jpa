package jpabook.jpashop.domain.item;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToMany;
import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter @Setter
public abstract class Item {

	@Id @GeneratedValue
	@Column(name = "item_id")
	private Long id;
	
	private String name;				// 상품이름
	private int price;					// 상품가격
	private int  stockQuantity;		// 재고수량
	
	@ManyToMany(mappedBy = "items")
	private List<Category> categories = new ArrayList<>();
	
	
	/**
	 *  우리가 보통 인티티 객체의 특정 값을 변경하고자 할 때 service 단에서 로직을 구현하고 최종적으로 엔티티 객체의 특정 필드에 set 해주는 방식으로 구현을 하게 되느데
	 *  이는 객체 지향적이지 못하다. 그래서 내가 변경하고자 하는 값을 들고 있는 엔티티 객체에 액션 메소드를 구현하는것이 응집도면에서도 좋고 더 객체지향 스럽다.
	 */

	/**
	 * stock 증가
	 */
	public void addStock(int stockQuantity) {
		this.stockQuantity += stockQuantity;
	}
	
	
	/**
	 * stock 감소
	 */
	public void removeStock(int stockQuantity) {
		int restStock =  this.stockQuantity - stockQuantity;
		
		if(restStock < 0) {
			throw new NotEnoughStockException("need more stock");
		}
		
		this.stockQuantity = restStock;
	}

	/**
	 *	아이템 정보 수정 1
	 */
	public void change(String name, int price, int stockQuantity) {
		this.name = name;
		this.price = price;
		this.stockQuantity = stockQuantity;
	}
	
}
