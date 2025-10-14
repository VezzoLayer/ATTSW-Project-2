package com.ecommerce.manager;

import static org.assertj.core.api.Assertions.assertThat;

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

}
