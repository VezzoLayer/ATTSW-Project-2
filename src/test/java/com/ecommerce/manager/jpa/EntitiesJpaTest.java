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
public class EntitiesJpaTest {

	@Autowired
	private TestEntityManager entityManager;

	@Test
	public void testUserJpaMapping() {
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
	public void testOrderJpaMapping() {
		Order savedOrder = entityManager.persistFlushFind(new Order(null, Item.BOX1, 700));

		assertThat(savedOrder.getItem()).isEqualTo(Item.BOX1);
		assertThat(savedOrder.getPrice()).isEqualTo(700);
		assertThat(savedOrder.getUser()).isNull();

		assertThat(savedOrder.getId()).isNotNull();
		assertThat(savedOrder.getId()).isPositive();

		// Per vedere identifier generato
		LoggerFactory.getLogger(EntitiesJpaTest.class).info("Saved: {}", savedOrder);
	}
}
