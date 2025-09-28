package com.ecommerce.manager.jpa;

import static org.assertj.core.api.Assertions.assertThat;

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
public class OrderJpaTest {

	@Autowired
	private TestEntityManager entityManager;

	@Test
	public void testOrderJpaMappingWithNoUser() {
		Order savedOrder = entityManager.persistFlushFind(new Order(null, Item.BOX1, 700));

		assertThat(savedOrder.getItem()).isEqualTo(Item.BOX1);
		assertThat(savedOrder.getPrice()).isEqualTo(700);
		assertThat(savedOrder.getUser()).isNull();

		assertThat(savedOrder.getId()).isNotNull();
		assertThat(savedOrder.getId()).isPositive();

		// Per vedere identifier generato
		LoggerFactory.getLogger(OrderJpaTest.class).info("Saved: {}", savedOrder);
	}

	@Test
	public void testOrderJpaMappingWithUser() {
		User user = entityManager.persistFlushFind(new User(null, "test", "test", "test", 1500));

		Order savedOrder = entityManager.persistFlushFind(new Order(null, Item.BOX1, 700, user));

		assertThat(savedOrder.getItem()).isEqualTo(Item.BOX1);
		assertThat(savedOrder.getPrice()).isEqualTo(700);
		assertThat(savedOrder.getUser()).isEqualTo(user);

		assertThat(savedOrder.getId()).isNotNull();
		assertThat(savedOrder.getId()).isPositive();

		// Per vedere identifier generato
		LoggerFactory.getLogger(OrderJpaTest.class).info("Saved: {}", savedOrder);
	}
}
