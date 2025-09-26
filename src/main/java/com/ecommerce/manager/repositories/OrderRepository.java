package com.ecommerce.manager.repositories;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.manager.model.Item;
import com.ecommerce.manager.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

	Collection<Order> findByItem(Item item);

}
