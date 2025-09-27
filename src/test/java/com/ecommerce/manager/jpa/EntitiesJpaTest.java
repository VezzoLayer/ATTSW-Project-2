package com.ecommerce.manager.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecommerce.manager.model.Item;
import com.ecommerce.manager.model.Order;
import com.ecommerce.manager.model.User;

@DataJpaTest
@RunWith(SpringRunner.class)
public class EntitiesJpaTest {

	@Autowired
	private TestEntityManager entityManager;

	@Test
	public void testUserJpaMappingWithNoOrders() {
		User savedUser = entityManager.persistFlushFind(new User(null, "username", "name", "email", 4000));

		assertThat(savedUser.getUsername()).isEqualTo("username");
		assertThat(savedUser.getName()).isEqualTo("name");
		assertThat(savedUser.getEmail()).isEqualTo("email");
		assertThat(savedUser.getBalance()).isEqualTo(4000);
		assertThat(savedUser.getOrders()).isEmpty();

		assertThat(savedUser.getId()).isNotNull();
		assertThat(savedUser.getId()).isPositive();

		// Per vedere identifier generato
		LoggerFactory.getLogger(EntitiesJpaTest.class).info("Saved: {}", savedUser);
	}

	@Test
	public void testUserJpaMappingWithOrders() {
		User user = new User(null, "withOrders", "test", "test@test.com", 2000);

		Order order1 = new Order(null, Item.BOX1, 500, user);
		Order order2 = new Order(null, Item.BOX3, 900, user);

		List<Order> orders = new ArrayList<>();
		orders.add(order1);
		orders.add(order2);

		user.setOrders(orders);

		User savedUser = entityManager.persistFlushFind(user);

		assertThat(savedUser.getUsername()).isEqualTo("withOrders");
		assertThat(savedUser.getName()).isEqualTo("test");
		assertThat(savedUser.getEmail()).isEqualTo("test@test.com");
		assertThat(savedUser.getBalance()).isEqualTo(2000);
		assertThat(savedUser.getOrders()).containsExactly(order1, order2);

		assertThat(savedUser.getId()).isNotNull();
		assertThat(savedUser.getId()).isPositive();

		// Per vedere identifier generato
		LoggerFactory.getLogger(EntitiesJpaTest.class).info("Saved: {}", savedUser);
	}

	@Test
	public void testOrderJpaMappingWithNoUser() {
		Order savedOrder = entityManager.persistFlushFind(new Order(null, Item.BOX1, 700));

		assertThat(savedOrder.getItem()).isEqualTo(Item.BOX1);
		assertThat(savedOrder.getPrice()).isEqualTo(700);
		assertThat(savedOrder.getUser()).isNull();

		assertThat(savedOrder.getId()).isNotNull();
		assertThat(savedOrder.getId()).isPositive();

		// Per vedere identifier generato
		LoggerFactory.getLogger(EntitiesJpaTest.class).info("Saved: {}", savedOrder);
	}

	@Test
	public void testOrderJpaMappingWithUser() {
		User savedUser = entityManager.persistFlushFind(new User(null, "test", "test", "test", 1500));

		Order order = new Order(null, Item.BOX1, 700);
		order.setUser(savedUser);

		Order savedOrder = entityManager.persistFlushFind(order);

		assertThat(savedOrder.getItem()).isEqualTo(Item.BOX1);
		assertThat(savedOrder.getPrice()).isEqualTo(700);
		assertThat(savedOrder.getUser()).isEqualTo(savedUser);

		assertThat(savedOrder.getId()).isNotNull();
		assertThat(savedOrder.getId()).isPositive();

		// Per vedere identifier generato
		LoggerFactory.getLogger(EntitiesJpaTest.class).info("Saved: {}", savedOrder);
	}
}
