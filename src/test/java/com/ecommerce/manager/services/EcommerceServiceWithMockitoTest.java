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
public class EcommerceServiceWithMockitoTest {

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Mock
	private UserRepository userRepository;

	@Mock
	private OrderRepository orderRepository;

	@InjectMocks
	private EcommerceService ecommerceService;

	@Test
	public void testGetAllUsers() {
		User user1 = new User(1L, "test", "test", "test", 5000);
		User user2 = new User(2L, "test", "test", "test", 4000);

		when(userRepository.findAll()).thenReturn(asList(user1, user2));

		assertThat(ecommerceService.getAllUsers()).containsExactly(user1, user2);
	}

	@Test
	public void testGetAllOrders() {
		User user = new User(1L, "test", "test", "test", 5000);

		Order order1 = new Order(1L, Item.BOX1, 700, user);
		Order order2 = new Order(2L, Item.BOX2, 900, user);

		when(orderRepository.findAll()).thenReturn(asList(order1, order2));

		assertThat(ecommerceService.getAllOrders()).containsExactly(order1, order2);
	}

	@Test
	public void testGetUserByIdCorrectlyFound() {
		User user = new User(1L, "test", "test", "test", 5000);

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		assertThat(ecommerceService.getUserById(1)).isSameAs(user);
	}

	@Test
	public void testGetUserByIdNotFound() {
		when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

		assertThat(ecommerceService.getUserById(1)).isNull();
	}

	@Test
	public void testGetOrderByIdCorrectlyFound() {
		Order order = new Order(1L, Item.BOX1, 700, new User(1L, "test", "test", "test", 5000));

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		assertThat(ecommerceService.getOrderById(1)).isSameAs(order);
	}

	@Test
	public void testGetOrderByIdNotFound() {
		when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

		assertThat(ecommerceService.getOrderById(1)).isNull();
	}

	@Test
	public void testInsertNewUserShouldSetIdToNullAndReturnsSavedUser() {
		User userToSave = spy(new User(70L, "", "", "", 0));
		User savedUser = new User(1L, "saved", "saved", "saved", 3000);

		when(userRepository.save(any(User.class))).thenReturn(savedUser);

		User result = ecommerceService.insertNewUser(userToSave);

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

		EcommerceService ecommerceServiceSpy = spy(ecommerceService);
		doNothing().when(ecommerceServiceSpy).withdraw(anyLong(), anyLong());

		when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

		Order result = ecommerceServiceSpy.insertNewOrder(orderToSave);

		assertThat(result).isSameAs(savedOrder);

		InOrder inOrder = inOrder(orderToSave, ecommerceServiceSpy, orderRepository);
		inOrder.verify(orderToSave).setId(null);
		inOrder.verify(ecommerceServiceSpy).withdraw(1L, 700L);
		inOrder.verify(orderRepository).save(orderToSave);
	}

	@Test
	public void testInsertNewOrderWhenFailsShouldThrowIllegalStateException() {
		Order orderToSave = spy(new Order(null, Item.BOX1, 700, new User(1L, "test", "test", "test", 500)));

		EcommerceService ecommerceServiceSpy = spy(ecommerceService);
		doThrow(new IllegalStateException("Unable to insert new order")).when(ecommerceServiceSpy).withdraw(anyLong(),
				anyLong());

		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> ecommerceServiceSpy.insertNewOrder(orderToSave));

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

		User result = ecommerceService.updateUserById(1L, replacement);

		assertThat(result).isSameAs(replaced);

		InOrder inOrder = inOrder(replacement, userRepository);
		inOrder.verify(replacement).setId(1L);
		inOrder.verify(userRepository).save(replacement);
	}

	@Test
	public void testUpdateOrderByIdWhenSuccessSetsIdToArgumentAndReturnsSavedOrder() {
		User user1 = new User(1L, "test", "test", "test", 5000);
		User user2 = new User(2L, "test", "test", "test", 5000);

		Order replacement = spy(new Order(null, Item.BOX1, 500, user1));
		Order replaced = new Order(1L, Item.BOX2, 700, user2);

		EcommerceService ecommerceServiceSpy = spy(ecommerceService);
		doNothing().when(ecommerceServiceSpy).withdraw(anyLong(), anyLong());
		doNothing().when(ecommerceServiceSpy).deposit(anyLong(), anyLong());

		when(orderRepository.findById(1L)).thenReturn(Optional.of(replaced));
		when(orderRepository.save(any(Order.class))).thenReturn(replacement);

		Order result = ecommerceServiceSpy.updateOrderById(1L, replacement);

		assertThat(result).isSameAs(replacement);

		InOrder inOrder = inOrder(replacement, ecommerceServiceSpy, orderRepository);
		inOrder.verify(replacement).setId(1L);
		inOrder.verify(ecommerceServiceSpy).deposit(2L, 700);
		inOrder.verify(ecommerceServiceSpy).withdraw(1L, 500);
		inOrder.verify(orderRepository).save(replacement);
	}

	@Test
	public void testUpdateOrderWhenDepositFailsShouldThrowIllegalStateException() {
		User user = new User(1L, "test", "test", "test", 5000);

		Order replacement = spy(new Order(null, Item.BOX1, 700, user));
		Order replaced = new Order(1L, Item.BOX2, 900, user);

		EcommerceService ecommerceServiceSpy = spy(ecommerceService);
		doThrow(new IllegalStateException("Unable to update the order")).when(ecommerceServiceSpy).deposit(anyLong(),
				anyLong());

		when(orderRepository.findById(1L)).thenReturn(Optional.of(replaced));

		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> ecommerceServiceSpy.updateOrderById(1L, replacement));

		assertThat(ex.getMessage()).isEqualTo("Unable to update the order");

		InOrder inOrder = inOrder(replacement, ecommerceServiceSpy);
		inOrder.verify(replacement).setId(1L);
		inOrder.verify(ecommerceServiceSpy).deposit(1L, 900);

		verify(ecommerceServiceSpy, never()).withdraw(anyLong(), anyLong());
		verify(orderRepository, never()).save(any());
	}

	@Test
	public void testUpdateOrderWhenWithdrawFailsShouldThrowIllegalStateException() {
		User user = new User(1L, "test", "test", "test", 5000);

		Order replacement = spy(new Order(null, Item.BOX1, 700, user));
		Order replaced = new Order(1L, Item.BOX2, 1000, user);

		EcommerceService ecommerceServiceSpy = spy(ecommerceService);
		doNothing().when(ecommerceServiceSpy).deposit(anyLong(), anyLong());
		doThrow(new IllegalStateException("Unable to update the order")).when(ecommerceServiceSpy).withdraw(anyLong(),
				anyLong());

		when(orderRepository.findById(1L)).thenReturn(Optional.of(replaced));

		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> ecommerceServiceSpy.updateOrderById(1L, replacement));

		assertThat(ex.getMessage()).isEqualTo("Unable to update the order");

		InOrder inOrder = inOrder(replacement, ecommerceServiceSpy);
		inOrder.verify(replacement).setId(1L);
		inOrder.verify(ecommerceServiceSpy).deposit(1L, 1000);
		inOrder.verify(ecommerceServiceSpy).withdraw(1L, 700);

		verify(orderRepository, never()).save(any());
	}

	@Test
	@Parameters({ "500, 2500", "0, 2000" })
	public void testDepositwWhenParameterizedAmountIsCorrectShouldCorrectlyUpdateBalance(long amount,
			long expectedBalance) {
		User user = spy(new User(1L, "test", "test", "test", 2000));

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenReturn(user);

		ecommerceService.deposit(1L, amount);

		InOrder inOrder = inOrder(user, userRepository);
		inOrder.verify(user).setBalance(expectedBalance);
		inOrder.verify(userRepository).save(user);
	}

	@Test
	public void testDepositWhenAmountIsNegativeShouldThrowException() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> ecommerceService.deposit(1L, -500L));

		assertThat(ex.getMessage()).isEqualTo("Deposit amount cannot be negative");
		verifyNoInteractions(userRepository);
	}

	@Test
	public void testDepositWhenUserNotFoundShouldThrowException() {
		when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> ecommerceService.deposit(1L, 500L));

		assertThat(ex.getMessage()).isEqualTo("User not found");
		verify(userRepository, never()).save(any());
	}

	@Test
	@Parameters({ "2000, 0", "500, 1500", "0, 2000" })
	public void testWithdrawWhenParameterizedAmountIsCorrectShouldCorrectlyUpdateBalance(long amount,
			long expectedBalance) {
		User user = spy(new User(1L, "test", "test", "test", 2000));

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenReturn(user);

		ecommerceService.withdraw(1L, amount);

		InOrder inOrder = inOrder(user, userRepository);
		inOrder.verify(user).setBalance(expectedBalance);
		inOrder.verify(userRepository).save(user);
	}

	@Test
	public void testWithdrawWhenAmountIsNegativeShouldThrowException() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> ecommerceService.withdraw(1L, -500L));

		assertThat(ex.getMessage()).isEqualTo("Withdraw amount cannot be negative");
		verifyNoInteractions(userRepository);
	}

	@Test
	public void testWithdrawWhenUserNotFoundShouldThrowException() {
		when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> ecommerceService.withdraw(1L, 500L));

		assertThat(ex.getMessage()).isEqualTo("User not found");
		verify(userRepository, never()).save(any());
	}

	@Test
	public void testWithdrawWhenBalanceIsNotEnoughShouldThrowException() {
		User user = new User(1L, "test", "test", "test", 300);

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> ecommerceService.withdraw(1L, 500L));
		assertThat(ex.getMessage()).isEqualTo("Not enough balance to perform withdraw");
		verify(userRepository, never()).save(any());
	}
}
