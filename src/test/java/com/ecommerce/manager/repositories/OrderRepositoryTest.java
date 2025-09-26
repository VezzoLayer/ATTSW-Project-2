package com.ecommerce.manager.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecommerce.manager.model.Item;
import com.ecommerce.manager.model.Order;

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
}
