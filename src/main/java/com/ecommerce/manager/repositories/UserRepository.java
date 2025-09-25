package com.ecommerce.manager.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.manager.model.User;

public interface UserRepository extends JpaRepository<User, Long>{

	User findByUsername(String username);

}
