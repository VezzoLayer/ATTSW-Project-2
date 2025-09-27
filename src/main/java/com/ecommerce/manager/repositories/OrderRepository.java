package com.ecommerce.manager.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.manager.model.Item;
import com.ecommerce.manager.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

	List<Order> findByItem(Item item);

	List<Order> findByPrice(long l);

	@Query("Select o from Order o where o.price > :threshold")
	List<Order> findAllOrdersWithHighPrice(@Param("threshold") long threshold);

}
