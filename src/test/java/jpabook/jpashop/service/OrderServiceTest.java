package jpabook.jpashop.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;

@SpringBootTest
@Transactional
class OrderServiceTest {

	@Autowired EntityManager em;

	@Autowired OrderRepository orderRepository;
	@Autowired OrderService orderService;
	
	@Test
	public void 상품주문() throws Exception{
		// given
		Member member = createMember();
		em.persist(member);
		
		Book book = (Book) createItem("밥솥", 10000, 10);
		em.persist(book);
		
		int orderCount = 2;
		// when
		Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
		
		
		
		// then
		Order orderOther =  orderRepository.findOne(orderId);
		assertEquals(OrderStatus.ORDER, orderOther.getStatus(), "상품 주문시 상태는 ORDER");
		assertEquals(1, orderOther.getOrderItems().size(),"주문한 상품 종류 수가 정확해야 한다.");
		assertEquals(10000*orderCount, orderOther.getTotalPrice(),"주문 가격은 가격 * 수량이다.");
		assertEquals(10-orderCount, book.getStockQuantity(),"주문 수량 만큼 재고가 줄어야 한다.");
	}

	@Test
	public void 상품주문_재고수량초과() throws Exception{
		
		// given
		Member member = createMember();
		em.persist(member);
		
		Book book = (Book) createItem("밥솥", 10000, 10);
		em.persist(book);
		
		int orderCount = 11;

		// when
//		Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
		
		// then
		assertThatThrownBy(() -> orderService.order(member.getId(), book.getId(), orderCount)).isInstanceOf(NotEnoughStockException.class);
	}
	
	@Test
	public void 주문취소() throws Exception{
		
		// given
		// given
		Member member = createMember();
		em.persist(member);
		
		Book book = (Book) createItem("밥솥", 10000, 10);
		em.persist(book);
		
		int orderCount = 2;
		// when
		Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
		
		// when
		orderService.cancelOrder(orderId);
		
		
		// then
		Order cancelOrder =  orderRepository.findOne(orderId);
		assertEquals(OrderStatus.CANCEL, cancelOrder.getStatus(),"주문 취소 상태는 CANCEL");
		assertEquals(10, book.getStockQuantity(),"주문 취소 시 재고가 증가해야 한다.");
	}
	
	private Member createMember() {
		Member member = new Member();
		member.setName("회원1");
		member.setAddress(new Address("서울","도로","123"));
		return member;
	}
	
	private Item createItem(String name, int price, int stockQuantity) {
		Book book = new Book();
		book.setName(name);
		book.setPrice(price);
		book.setStockQuantity(stockQuantity);
		return book;
	}
}
