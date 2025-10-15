package com.ecommerce.manager.services;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.ecommerce.manager.model.Item;
import com.ecommerce.manager.model.Order;
import com.ecommerce.manager.model.User;
import com.ecommerce.manager.repositories.OrderRepository;
import com.ecommerce.manager.repositories.UserRepository;

@RunWith(JUnitParamsRunner.class)
public class UserServiceWithMockitoTest {

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Mock
	private UserRepository userRepository;

	@Mock
	private OrderRepository orderRepository;

	@InjectMocks
	private UserService userService;

	@Test
	public void testGetAllUsers() {
		User user1 = new User(1L, "test", "test", "test", 5000);
		User user2 = new User(2L, "test", "test", "test", 4000);

		when(userRepository.findAll()).thenReturn(asList(user1, user2));

		assertThat(userService.getAllUsers()).containsExactly(user1, user2);
	}

	@Test
	public void testGetAllOrders() {
		User user = new User(1L, "test", "test", "test", 5000);

		Order order1 = new Order(1L, Item.BOX1, 700, user);
		Order order2 = new Order(2L, Item.BOX2, 900, user);

		when(orderRepository.findAll()).thenReturn(asList(order1, order2));

		assertThat(userService.getAllOrders()).containsExactly(order1, order2);
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
	public void testGetOrderByIdCorrectlyFound() {
		Order order = new Order(1L, Item.BOX1, 700, new User(1L, "test", "test", "test", 5000));

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		assertThat(userService.getOrderById(1)).isSameAs(order);
	}

	@Test
	public void testGetOrderByIdNotFound() {
		when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

		assertThat(userService.getOrderById(1)).isNull();
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
	public void testInsertNewOrderWhenSuccessShouldSetIdToNullAndReturnsSavedOrder() {
		User user = new User(1L, "test", "test", "test", 5000);

		Order orderToSave = spy(new Order(70L, Item.BOX1, 700, user));
		Order savedOrder = new Order(1L, Item.BOX2, 900, user);

		UserService userServiceSpy = spy(userService);
		doNothing().when(userServiceSpy).withdraw(anyLong(), anyLong());

		when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

		Order result = userServiceSpy.insertNewOrder(orderToSave);

		assertThat(result).isSameAs(savedOrder);

		InOrder inOrder = inOrder(orderToSave, userServiceSpy, orderRepository);
		inOrder.verify(orderToSave).setId(null);
		inOrder.verify(userServiceSpy).withdraw(1L, 700L);
		inOrder.verify(orderRepository).save(orderToSave);
	}

	@Test
	public void testInsertNewOrderWhenFailsShouldThrowIllegalStateException() {
		Order orderToSave = spy(new Order(null, Item.BOX1, 700, new User(1L, "test", "test", "test", 500)));

		UserService userServiceSpy = spy(userService);
		doThrow(new IllegalStateException("Unable to insert new order")).when(userServiceSpy).withdraw(anyLong(),
				anyLong());

		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> userServiceSpy.insertNewOrder(orderToSave));

		assertThat(ex.getMessage()).isEqualTo("Unable to insert new order");

		InOrder inOrder = inOrder(orderToSave);
		inOrder.verify(orderToSave).setId(null);
		verifyNoInteractions(orderRepository);
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
	@Parameters({ "500, 2500", "0, 2000" })
	public void testDepositwWhenParameterizedAmountIsCorrectShouldCorrectlyUpdateBalance(long amount,
			long expectedBalance) {
		User user = spy(new User(1L, "test", "test", "test", 2000));

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenReturn(user);

		userService.deposit(1L, amount);

		InOrder inOrder = inOrder(user, userRepository);
		inOrder.verify(user).setBalance(expectedBalance);
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
	public void testDepositWhenUserNotFoundShouldThrowException() {
		when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> userService.deposit(1L, 500L));

		assertThat(ex.getMessage()).isEqualTo("User not found");
	}

	@Test
	@Parameters({ "2000, 0", "500, 1500", "0, 2000" })
	public void testWithdrawWhenParameterizedAmountIsCorrectShouldCorrectlyUpdateBalance(long amount,
			long expectedBalance) {
		User user = spy(new User(1L, "test", "test", "test", 2000));

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenReturn(user);

		userService.withdraw(1L, amount);

		InOrder inOrder = inOrder(user, userRepository);
		inOrder.verify(user).setBalance(expectedBalance);
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
	public void testWithdrawWhenUserNotFoundShouldThrowException() {
		when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> userService.withdraw(1L, 500L));
		assertThat(ex.getMessage()).isEqualTo("User not found");
	}

	@Test
	public void testWithdrawWhenBalanceIsNotEnoughShouldThrowException() {
		User user = new User(1L, "test", "test", "test", 300);

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> userService.withdraw(1L, 500L));
		assertThat(ex.getMessage()).isEqualTo("Not enough balance to perform withdraw");
		verify(userRepository, never()).save(any());
	}
}
