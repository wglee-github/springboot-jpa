package jpabook.jpashop;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.repository.OrderRepository;

@SpringBootTest
class JpashopApplicationTests {

	@Autowired
	private OrderRepository orderRepository;
	
	@Test
	void contextLoads() {
		Long id = 0L;
		Order order = orderRepository.findOne(id);
		OrderItem[] orderItems = order.getOrderItems().toArray(new OrderItem[order.getOrderItems().size()]);
		testMethod2(orderItems);
	}

	public void testMethod(String... strings) {
		
		for (String string : strings) {
			System.out.println(string);
		}
	}
	
	public void testMethod2(OrderItem... orderItems) {
		
		for (OrderItem string : orderItems) {
			System.out.println(string.getCount());
		}
	}
}
