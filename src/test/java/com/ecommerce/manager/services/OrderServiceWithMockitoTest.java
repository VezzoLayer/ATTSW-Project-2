package com.ecommerce.manager.services;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ecommerce.manager.model.Item;
import com.ecommerce.manager.model.Order;
import com.ecommerce.manager.model.User;
import com.ecommerce.manager.repositories.OrderRepository;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceWithMockitoTest {

	@Mock
	private OrderRepository orderRepository;

	@InjectMocks
	private OrderService orderService;

	@Test
	public void GetAllOrders() {
		User user = new User(1L, "test", "test", "test", 5000);

		Order order1 = new Order(1L, Item.BOX1, 700, user);
		Order order2 = new Order(2L, Item.BOX2, 900, user);

		when(orderRepository.findAll()).thenReturn(asList(order1, order2));

		assertThat(orderService.getAllOrders()).containsExactly(order1, order2);
	}

	@Test
	public void testGetOrderByIdCorrectlyFound() {
		Order order = new Order(1L, Item.BOX1, 700, new User(1L, "test", "test", "test", 5000));

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		assertThat(orderService.getOrderById(1)).isSameAs(order);
	}
}
