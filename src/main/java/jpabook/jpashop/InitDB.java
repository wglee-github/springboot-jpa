package jpabook.jpashop;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InitDB {

	private final InitService initService;
	
	/**
	 * @PostConstruct Srping이 올라간 후 실행된다.
	 */
	@PostConstruct
	public void init() {
		initService.dbInit1();
		initService.dbInit2();
	}
	
	@Component
	@Transactional
	@RequiredArgsConstructor
	static class InitService {

		private final EntityManager em;
		
		public void dbInit1() {
			Member member = createMember("userA","서울", "광명사거리", "20315");
			em.persist(member);
			
			Book book1 = createBook("JPA1 BOOK", 10000, 100);
			em.persist(book1);
			
			Book book2 = createBook("JPA2 BOOK", 20000, 100);
			em.persist(book2);
			
			
			OrderItem orderItem1 = OrderItem.creatOrderITem(book1, 10000, 1);
			OrderItem orderItem2 = OrderItem.creatOrderITem(book2, 20000, 2);
			
			Delivery delivery = createDelivery(member.getAddress());
			
			Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
			em.persist(order);
			
		}
		
		public void dbInit2() {
			Member member = createMember("userB","인천", "센트럴로", "30595");
			em.persist(member);
			
			Book book1 = createBook("SPRING1 BOOK", 10000, 100);
			em.persist(book1);
			
			Book book2 = createBook("SPRING2 BOOK", 20000, 100);
			em.persist(book2);
			
			
			OrderItem orderItem1 = OrderItem.creatOrderITem(book1, 10000, 1);
			OrderItem orderItem2 = OrderItem.creatOrderITem(book2, 20000, 2);
			
			Delivery delivery = createDelivery(member.getAddress());
			
			Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
			em.persist(order);
			
		}
		
		private Member createMember(String name, String city, String street, String zipcode) {
			Member member = new Member();
			member.setName(name);
			member.setAddress(new Address(city, street, zipcode));
			return member;
		}
		
		private Book createBook(String name, int price, int stockQuantity) {
			Book book = new Book();
			book.setName(name);
			book.setPrice(price);
			book.setStockQuantity(stockQuantity);
			return book;
		}
		
		private Delivery createDelivery(Address address) {
			Delivery delivery = new Delivery();
			delivery.setAddress(address);
			return delivery;
		}
	}
}
