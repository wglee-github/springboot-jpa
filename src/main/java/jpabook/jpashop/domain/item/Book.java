package jpabook.jpashop.domain.item;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("B")
@Getter @Setter
public class Book extends Item{

	private String author;
	private String isbn;
	
	/**
	 *	아이템 Book 정보 수정
	 */
	public void change(ItemDTO itemDTO) {
		super.setName(itemDTO.getName());
		super.setPrice(itemDTO.getPrice());
		super.setStockQuantity(itemDTO.getStockQuantity());
		this.author = itemDTO.getAuthor();
		this.isbn = itemDTO.getIsbn();
	}
}
