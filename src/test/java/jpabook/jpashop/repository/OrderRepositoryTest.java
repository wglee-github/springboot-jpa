package jpabook.jpashop.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;

@SpringBootTest
@Rollback(false)
@Transactional
class OrderRepositoryTest {

	@Autowired OrderRepository orderRepository;
	
	@Test
	void test() {
		
		OrderSearch orderSearch = new OrderSearch();
		
		List<Order> orders = orderRepository.findAllByString(orderSearch);
		
		System.out.println("**************** " + orders.size());
		
		for (Order order : orders) {
			order.getOrderItems().stream().forEach(o -> System.out.println("========= orderItem orderPrice =========="+ o.getOrderPrice()));
		}
		
	}

}
