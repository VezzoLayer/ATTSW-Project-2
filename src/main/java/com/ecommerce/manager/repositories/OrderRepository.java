package com.ecommerce.manager.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.manager.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
