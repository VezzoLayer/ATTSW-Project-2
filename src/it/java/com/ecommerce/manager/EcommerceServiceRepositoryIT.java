package com.ecommerce.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.Before;
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

	@Before
	public void setup() {
		// Si puliscono le repository perché si fa uso di tests non transactional
		orderRepository.deleteAll();
		orderRepository.flush();

		userRepository.deleteAll();
		userRepository.flush();
	}

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
		assertThat(userRepository.findById(user.getId()).orElseThrow().getBalance()).isEqualTo(1500);
	}

	@Test
	public void testServiceCanUpdateUserRepository() {
		User savedUser = ecommerceService.insertNewUser(new User(null, "username", "name", "email", 1000));

		User modifiedUser = ecommerceService.updateUserById(savedUser.getId(),
				new User(savedUser.getId(), "mod username", "mod name", "mod email", 2000));

		assertThat(userRepository.findById(savedUser.getId())).contains(modifiedUser);
	}

	@Test
	public void testUserRepositoryCorrectlyFindsUserWithLowBalance() {
		User userShouldBeFound1 = ecommerceService.insertNewUser(new User(null, "u1", "n1", "e1", 800));
		User userShouldBeFound2 = ecommerceService.insertNewUser(new User(null, "u2", "n2", "e2", 500));

		ecommerceService.insertNewUser(new User(null, "not", "not", "not", 1500));

		List<User> users = userRepository.findAllUsersWithLowBalance(1000);

		assertThat(users).containsExactly(userShouldBeFound1, userShouldBeFound2);
	}

	@Test
	public void testServiceCanCorrectlyDepositIntoUserRepository() {
		User savedUser = ecommerceService.insertNewUser(new User(null, "username", "name", "email", 1000));

		ecommerceService.deposit(savedUser.getId(), 1000);

		assertThat(userRepository.findById(savedUser.getId()).orElseThrow().getBalance()).isEqualTo(2000);
	}

	@Test
	public void testServiceCanCorrectlyWithdrawIntoUserRepository() {
		User savedUser = ecommerceService.insertNewUser(new User(null, "username", "name", "email", 1000));

		ecommerceService.withdraw(savedUser.getId(), 500);

		assertThat(userRepository.findById(savedUser.getId()).orElseThrow().getBalance()).isEqualTo(500);
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void testServiceInsertNewOrderWhenOrderRepositorySaveFailsShouldRollBack() {
		User user = ecommerceService.insertNewUser(new User(null, "u1", "n1", "e1", 2000));
		Order notSavedOrder = new Order(null, null, 500, user);

		assertThatThrownBy(() -> ecommerceService.insertNewOrder(notSavedOrder))
				.isInstanceOf(DataIntegrityViolationException.class);

		assertThat(orderRepository.findById(notSavedOrder.getId())).isNotPresent();
		assertThat(userRepository.findById(user.getId()).orElseThrow().getBalance()).isEqualTo(2000);
	}

	@Test
	public void testServiceUpdateIntoOrderRepositoryAndCorrectlyUpdateBalances() {
		User user1 = ecommerceService.insertNewUser(new User(null, "u1", "n1", "e1", 1000));
		User user2 = ecommerceService.insertNewUser(new User(null, "u2", "n2", "e2", 2000));

		Order savedOrder = ecommerceService.insertNewOrder(new Order(null, Item.BOX1, 500, user1));

		assertThat(userRepository.findById(user1.getId()).orElseThrow().getBalance()).isEqualTo(500);

		Order replacementOrder = ecommerceService.updateOrderById(savedOrder.getId(),
				new Order(null, Item.BOX2, 1000, user2));

		// Balance di user1 aggiornato dopo aver rimosso l'ordine riferito a lui
		assertThat(userRepository.findById(user1.getId()).orElseThrow().getBalance()).isEqualTo(1000);
		// Balance di user2 aggiornato dopo aver aggiornato l'ordine riferito a lui
		assertThat(userRepository.findById(user2.getId()).orElseThrow().getBalance()).isEqualTo(1000);
		// Controllo che l'ordine sia effettivamente aggiornato
		assertThat(orderRepository.findById(savedOrder.getId())).contains(replacementOrder);
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void testServiceUpdateAnOrderWhenOrderRepositorySaveFailsShouldRollBack() {
		User user1 = ecommerceService.insertNewUser(new User(null, "u1", "n1", "e1", 1000));
		User user2 = ecommerceService.insertNewUser(new User(null, "u2", "n2", "e2", 2000));

		Order savedOrder = ecommerceService.insertNewOrder(new Order(null, Item.BOX1, 500, user1));

		assertThatThrownBy(
				() -> ecommerceService.updateOrderById(savedOrder.getId(), new Order(null, null, 1000, user2)))
				.isInstanceOf(DataIntegrityViolationException.class);

		// Si controlla che tutto sia allo stato iniziale prima dell'update
		assertThat(userRepository.findById(user1.getId()).orElseThrow().getBalance()).isEqualTo(500);
		assertThat(userRepository.findById(user2.getId()).orElseThrow().getBalance()).isEqualTo(2000);
		assertThat(orderRepository.findById(savedOrder.getId())).contains(savedOrder);
	}
}
