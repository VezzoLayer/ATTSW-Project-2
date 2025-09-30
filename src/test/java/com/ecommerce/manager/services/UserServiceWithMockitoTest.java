package com.ecommerce.manager.services;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ecommerce.manager.model.User;
import com.ecommerce.manager.repositories.UserRepository;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceWithMockitoTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	@Test
	public void GetAllUser() {
		User user1 = new User(null, "test", "test", "test", 5000);
		User user2 = new User(null, "test", "test", "test", 4000);

		when(userRepository.findAll()).thenReturn(asList(user1, user2));

		assertThat(userService.getAllUsers()).containsExactly(user1, user2);
	}
}
