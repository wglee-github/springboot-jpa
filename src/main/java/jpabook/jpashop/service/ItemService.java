package jpabook.jpashop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.item.ItemDTO;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
	
	private final ItemRepository itemRepository;

	
	/**
	 * 아이템 저장
	 */
	@Transactional
	public void saveItem(Item item) {
		itemRepository.save(item);
	}
	
	/**\
	 * 아이템 수정
	 * 변경감지(Dirty Checking)를 이용하여 수정.
	 */
	@Transactional
	public void updateItem1(Long itemId, String name, int price, int stockQuantity) {
		Item item = itemRepository.findOne(itemId);
//		item.setName(name);
//		item.setPrice(price);
//		item.setStockQuantity(stockQuantity);
		
		item.change(name, price, stockQuantity);
	}
	
	/**\
	 * 아이템 수정 -
	 * 변경감지(Dirty Checking)를 이용하여 수정.
	 */
	@Transactional
	public void updateItemBook(ItemDTO itemDTO) {
		Book book = (Book)itemRepository.findOne(itemDTO.getId());
		book.change(itemDTO);
		
	}
	
	/**
	 * 아이템 전체 조회
	 */
	public List<Item> findItems(){
		return itemRepository.findAll();		
	}
	
	/**
	 * 아이템 단건 조회
	 */
	public Item findItem(Long itemId) {
		return itemRepository.findOne(itemId);
	}

}
