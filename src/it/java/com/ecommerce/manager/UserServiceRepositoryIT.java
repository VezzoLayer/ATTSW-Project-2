package com.ecommerce.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecommerce.manager.model.User;
import com.ecommerce.manager.repositories.UserRepository;
import com.ecommerce.manager.services.UserService;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(UserService.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserServiceRepositoryIT {

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Test
	public void testServiceCanInsertIntoRepository() {
		User savedUser = userService.insertNewUser(new User(null, "username", "name", "email", 2000));

		assertThat(userRepository.findById(savedUser.getId())).isPresent();
	}

	@Test
	public void testServiceCanUpdateRepository() {
		User savedUser = userService.insertNewUser(new User(null, "username", "name", "email", 1000));

		User modifiedUser = userService.updateUserById(savedUser.getId(),
				new User(savedUser.getId(), "mod username", "mod name", "mod email", 2000));

		assertThat(userRepository.findById(savedUser.getId())).contains(modifiedUser);
	}

	@Test
	public void testRepositoryCorrectlyFindsUserWithLowBalance() {
		User userShouldBeFound1 = userService.insertNewUser(new User(null, "u1", "n1", "e1", 800));
		User userShouldBeFound2 = userService.insertNewUser(new User(null, "u2", "n2", "e2", 500));

		userService.insertNewUser(new User(null, "u3", "n3", "e3", 1500));

		List<User> users = userRepository.findAllUsersWithLowBalance(1000);

		assertThat(users).containsExactly(userShouldBeFound1, userShouldBeFound2);
	}

	@Test
	public void testServiceCanCorrectlyDepositIntoRepository() {
		User savedUser = userService.insertNewUser(new User(null, "username", "name", "email", 1000));

		userService.deposit(savedUser.getId(), 1000);

		assertThat(userRepository.findById(savedUser.getId()).orElseThrow().getBalance()).isEqualTo(2000);
	}

	@Test
	public void testServiceCanCorrectlyWithdrawIntoRepository() {
		User savedUser = userService.insertNewUser(new User(null, "username", "name", "email", 2000));

		userService.withdraw(savedUser.getId(), 1000);

		assertThat(userRepository.findById(savedUser.getId()).orElseThrow().getBalance()).isEqualTo(1000);
	}
}
