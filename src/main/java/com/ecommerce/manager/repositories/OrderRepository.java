package com.ecommerce.manager.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.manager.model.Item;
import com.ecommerce.manager.model.Order;
import com.ecommerce.manager.model.User;

public interface OrderRepository extends JpaRepository<Order, Long> {

	List<Order> findByItem(Item item);

	List<Order> findByPrice(long price);

	List<Order> findByUser(User user);

	List<Order> findByItemOrPrice(Item item, long price);

	List<Order> findByItemAndUser(Item item, User user);

	@Query("Select o from Order o where o.price > :threshold")
	List<Order> findAllOrdersWithHighPrice(@Param("threshold") long threshold);

}
