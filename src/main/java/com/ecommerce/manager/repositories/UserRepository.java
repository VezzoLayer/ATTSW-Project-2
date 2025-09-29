package com.ecommerce.manager.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.manager.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

	User findByUsername(String username);

	User findByName(String name);

	User findByEmail(String email);

	List<User> findByUsernameOrName(String username, String name);

	List<User> findByUsernameOrEmail(String username, String email);

	List<User> findByNameOrEmail(String name, String email);

	List<User> findByUsernameOrNameOrEmail(String username, String name, String email);

	@Query("Select u from User u where u.balance < :threshold")
	List<User> findAllUsersWithLowBalance(@Param("threshold") long threshold);
}