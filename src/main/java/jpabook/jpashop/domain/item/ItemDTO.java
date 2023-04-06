package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ItemDTO {

	private Long id;
	private String name;
	private int price;
	private int stockQuantity;
	
	// book 정보
	private String author;
	private String isbn;
	
	// album 정보
	
	// movie 정보
}
