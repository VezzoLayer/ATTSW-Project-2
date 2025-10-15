package com.ecommerce.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.manager.model.Item;
import com.ecommerce.manager.model.Order;
import com.ecommerce.manager.model.User;
import com.ecommerce.manager.repositories.OrderRepository;
import com.ecommerce.manager.services.OrderService;
import com.ecommerce.manager.services.UserService;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import({ OrderService.class, UserService.class })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderServiceRepositoryIT {

	@Autowired
	private OrderService orderService;

	@Autowired
	private UserService userService;

	@Autowired
	private OrderRepository orderRepository;

	@Test
	public void testServiceCanInsertIntoRepositoryAndCorrectlyUpdateUserBalance() {
		User user = userService.insertNewUser(new User(null, "u1", "n1", "e1", 2000));
		Order order = new Order(null, Item.BOX1, 500, user);

		Order savedOrder = orderService.insertNewOrder(order);

		assertThat(orderRepository.findById(savedOrder.getId())).isPresent();
		assertThat(userService.getUserById(user.getId()).getBalance()).isEqualTo(1500);
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void testServiceInsertNewOrderWhenRepositorySaveFailsShouldRollbackUserBalance() {
		User user = userService.insertNewUser(new User(null, "u1", "n1", "e1", 2000));
		Order notSavedOrder = new Order(null, null, 500, user);

		assertThatThrownBy(() -> orderService.insertNewOrder(notSavedOrder))
				.isInstanceOf(DataIntegrityViolationException.class);

		assertThat(orderRepository.findById(notSavedOrder.getId()).orElse(null)).isNull();
		assertThat(userService.getUserById(user.getId()).getBalance()).isEqualTo(2000);
	}
}
