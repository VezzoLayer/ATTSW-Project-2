package com.ecommerce.manager.services;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
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

	@Test
	public void testGetUserByIdCorrectlyFound() {
		User user = new User(1L, "test", "test", "test", 5000);

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		assertThat(userService.getUserById(1)).isSameAs(user);
	}

	@Test
	public void testGetUserByIdNotFound() {
		when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

		assertThat(userService.getUserById(1)).isNull();
	}

	@Test
	public void testInsertNewUserShouldSetIdToNullAndReturnsSavedUser() {
		User userToSave = spy(new User(70L, "", "", "", 0));
		User savedUser = new User(1L, "saved", "saved", "saved", 3000);

		when(userRepository.save(any(User.class))).thenReturn(savedUser);

		User result = userService.insertNewUser(userToSave);

		assertThat(result).isSameAs(savedUser);

		InOrder inOrder = inOrder(userToSave, userRepository);
		inOrder.verify(userToSave).setId(null);
		inOrder.verify(userRepository).save(userToSave);
	}

	@Test
	public void testUpdateUserByIdSetsIdToArgumentAndReturnsSavedUser() {
		User replacement = spy(new User(null, "replacement", "replacement", "replacement", 2000));
		User replaced = new User(1L, "username", "name", "email", 3000);

		when(userRepository.save(any(User.class))).thenReturn(replaced);

		User result = userService.updateUserById(1L, replacement);

		assertThat(result).isSameAs(replaced);

		InOrder inOrder = inOrder(replacement, userRepository);
		inOrder.verify(replacement).setId(1L);
		inOrder.verify(userRepository).save(replacement);
	}
}
