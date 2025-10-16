package com.ecommerce.manager;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecommerce.manager.model.Item;
import com.ecommerce.manager.model.Order;
import com.ecommerce.manager.model.User;
import com.ecommerce.manager.repositories.OrderRepository;
import com.ecommerce.manager.repositories.UserRepository;
import com.ecommerce.manager.services.EcommerceService;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(EcommerceService.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class EcommerceServiceRepositoryIT {

	@Autowired
	private EcommerceService ecommerceService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Test
	public void testServiceCanInsertIntoUserRepository() {
		User savedUser = ecommerceService.insertNewUser(new User(null, "username", "name", "email", 2000));

		assertThat(userRepository.findById(savedUser.getId())).isPresent();
	}

	@Test
	public void testServiceCanInsertIntoOrderRepositoryAndCorrectlyUpdateUserBalance() {
		User user = ecommerceService.insertNewUser(new User(null, "u1", "n1", "e1", 2000));
		Order order = new Order(null, Item.BOX1, 500, user);

		Order savedOrder = ecommerceService.insertNewOrder(order);

		assertThat(orderRepository.findById(savedOrder.getId())).isPresent();
		assertThat(ecommerceService.getUserById(user.getId()).getBalance()).isEqualTo(1500);
	}

	@Test
	public void testServiceCanUpdateUserRepository() {
		User savedUser = ecommerceService.insertNewUser(new User(null, "username", "name", "email", 1000));

		User modifiedUser = ecommerceService.updateUserById(savedUser.getId(),
				new User(savedUser.getId(), "mod username", "mod name", "mod email", 2000));

		assertThat(userRepository.findById(savedUser.getId())).contains(modifiedUser);
	}
}
