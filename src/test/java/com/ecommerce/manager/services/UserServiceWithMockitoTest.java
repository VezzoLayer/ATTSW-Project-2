package com.ecommerce.manager.services;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
	public void GetAllUsers() {
		User user1 = new User(1L, "test", "test", "test", 5000);
		User user2 = new User(2L, "test", "test", "test", 4000);

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

	@Test
	public void testDepositWhenAmountIsCorrectShouldIncreaseBalance() {
		User user = spy(new User(1L, "test", "test", "test", 2000));

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenReturn(user);

		userService.deposit(1L, 500L);

		InOrder inOrder = inOrder(user, userRepository);
		inOrder.verify(user).setBalance(2500L);
		inOrder.verify(userRepository).save(user);
	}

	@Test
	public void testDepositWhenAmountIsZeroShouldBeAllowed() {
		User user = spy(new User(1L, "test", "test", "test", 2000));

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenReturn(user);

		userService.deposit(1L, 0L);

		InOrder inOrder = inOrder(user, userRepository);
		inOrder.verify(user).setBalance(2000L);
		inOrder.verify(userRepository).save(user);
	}

	@Test
	public void testDepositWhenAmountIsNegativeShouldThrowException() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> userService.deposit(1L, -500L));

		assertThat(ex.getMessage()).isEqualTo("Deposit amount cannot be negative");
		verifyNoInteractions(userRepository);
	}

	@Test
	public void testDepositWhenUserNotFoundShouldReturnNull() {
		when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> userService.deposit(1L, 500L));

		assertThat(ex.getMessage()).isEqualTo("User not found");
	}

	@Test
	public void testWithdrawWhenAmountIsCorrectShouldDecrementBalance() {
		User user = spy(new User(1L, "test", "test", "test", 2000));

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenReturn(user);

		User result = userService.withdraw(1L, 500L);

		assertThat(result.getBalance()).isEqualTo(1500L);

		InOrder inOrder = inOrder(user, userRepository);
		inOrder.verify(user).setBalance(1500L);
		inOrder.verify(userRepository).save(user);
	}

	@Test
	public void testWithdrawWhenAmountIsZeroShouldBeAllowed() {
		User user = spy(new User(1L, "test", "test", "test", 2000));

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenReturn(user);

		User result = userService.withdraw(1L, 0L);

		assertThat(result.getBalance()).isEqualTo(2000L);

		InOrder inOrder = inOrder(user, userRepository);
		inOrder.verify(user).setBalance(2000L);
		inOrder.verify(userRepository).save(user);
	}

	@Test
	public void testWithdrawWhenAmountIsNegativeShouldThrowException() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> userService.withdraw(1L, -500L));

		assertThat(ex.getMessage()).isEqualTo("Withdraw amount cannot be negative");
		verifyNoInteractions(userRepository);
	}

	@Test
	public void testWithdrawWhenUserNotFoundShouldReturnNull() {
		when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

		assertThat(userService.withdraw(1L, 500L)).isNull();
	}

	@Test
	public void testWithdrawWhenBalanceIsNotEnoughShouldThrowException() {
		User user = new User(1L, "test", "test", "test", 300);

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> userService.withdraw(1L, 500L));
		assertThat(ex.getMessage()).isEqualTo("Not enough balance to perform withdraw");
		verify(userRepository, never()).save(any());
	}

	@Test
	public void testWithdrawWhenAmountIsExactlyTheBalanceShouldBeAllowed() {
		User user = spy(new User(1L, "test", "test", "test", 2000));

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenReturn(user);

		User result = userService.withdraw(1L, 2000L);

		assertThat(result.getBalance()).isEqualTo(0L);

		InOrder inOrder = inOrder(user, userRepository);
		inOrder.verify(user).setBalance(0L);
		inOrder.verify(userRepository).save(user);
	}
}
