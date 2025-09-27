package com.ecommerce.manager.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecommerce.manager.model.Item;
import com.ecommerce.manager.model.Order;
import com.ecommerce.manager.model.User;

@DataJpaTest
@RunWith(SpringRunner.class)
public class OrderRepositoryTest {

	@Autowired
	private OrderRepository repository;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	public void testRepositoryContainsExactlyOneOrder() {
		Order orderSaved = entityManager.persistFlushFind(new Order(null, Item.BOX1, 500));

		Collection<Order> orders = repository.findAll();
		assertThat(orders).containsExactly(orderSaved);
	}

	@Test
	public void testFindOrdersByItem() {
		Order order1 = entityManager.persistFlushFind(new Order(null, Item.BOX1, 500));
		Order order2 = entityManager.persistFlushFind(new Order(null, Item.BOX1, 1000));
		entityManager.persistFlushFind(new Order(null, Item.BOX2, 500)); // Should not be found

		List<Order> orders = repository.findByItem(Item.BOX1);
		assertThat(orders).containsExactly(order1, order2);
	}

	@Test
	public void testFindOrdersByPrice() {
		Order order1 = entityManager.persistFlushFind(new Order(null, Item.BOX1, 700));
		Order order2 = entityManager.persistFlushFind(new Order(null, Item.BOX2, 700));
		entityManager.persistFlushFind(new Order(null, Item.BOX3, 500)); // Should not be found

		List<Order> orders = repository.findByPrice(700L);
		assertThat(orders).containsExactly(order1, order2);
	}

	@Test
	public void testFindOrdersByUser() {
		User userMatch = entityManager.persistFlushFind(new User(null, "test", "test", "test", 4000));

		Order order1 = entityManager.persistFlushFind(new Order(null, Item.BOX1, 700, userMatch));
		Order order2 = entityManager.persistFlushFind(new Order(null, Item.BOX2, 1300, userMatch));

		User userNotMatch = entityManager.persistFlushFind(new User(null, "not", "not", "not", 500));
		entityManager.persistFlushFind(new Order(null, Item.BOX3, 500, userNotMatch)); // Should not be found

		List<Order> orders = repository.findByUser(userMatch);
		assertThat(orders).containsExactly(order1, order2);
	}

	@Test
	public void testFindOrdersByItemOrPrice() {
		Order order1 = entityManager.persistFlushFind(new Order(null, Item.BOX1, 700));
		Order order2 = entityManager.persistFlushFind(new Order(null, Item.BOX3, 1000));
		entityManager.persistFlushFind(new Order(null, Item.BOX2, 500)); // Should not be found

		List<Order> orders = repository.findByItemOrPrice(Item.BOX1, 1000L);
		assertThat(orders).containsExactly(order1, order2);
	}

	@Test
	public void testFindAllOrdersWithHighPrice() {
		Order order1 = entityManager.persistFlushFind(new Order(null, Item.BOX1, 1501));
		Order order2 = entityManager.persistFlushFind(new Order(null, Item.BOX2, 1700));
		entityManager.persistFlushFind(new Order(null, Item.BOX3, 1300)); // Should not be found

		List<Order> orders = repository.findAllOrdersWithHighPrice(1500L);
		assertThat(orders).containsExactly(order1, order2);
	}
}
