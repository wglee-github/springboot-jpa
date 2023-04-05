package jpabook.jpashop.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;

@SpringBootTest
@Transactional
class ItemRepositoryTest {

	@Autowired ItemService itemService;
	@Autowired EntityManager em;
	
	@Test
	void test() {
		
		Book book = new Book();
		book.setName("JPA");
		
//		itemService.saveItem(book);
		em.persist(book);
		
		Item item = em.find(Book.class, book.getId());
		
		assertEquals(book.getId(), item.getId());
	}

}
