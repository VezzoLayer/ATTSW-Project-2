package com.ecommerce.manager.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecommerce.manager.model.User;

@DataJpaTest
@RunWith(SpringRunner.class)
public class UserRepositoryTest {

	@Autowired
	private UserRepository repository;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	public void testRepositoryContainsExactlyOneUser() {
		User user = new User(null, "test", "test", "test", 4000);
		User userSaved = entityManager.persistFlushFind(user);

		Collection<User> users = repository.findAll();
		assertThat(users).containsExactly(userSaved);
	}

	@Test
	public void testFindUserByUsername() {
		User userShouldBeFound = entityManager.persistFlushFind(new User(null, "AAA", "test", "test", 4000));

		User userFound = repository.findByUsername("AAA");
		assertThat(userFound).isEqualTo(userShouldBeFound);
	}

	@Test
	public void testFindUserByName() {
		User userShouldBeFound = entityManager.persistFlushFind(new User(null, "test", "ABC", "test", 4000));

		User userFound = repository.findByName("ABC");
		assertThat(userFound).isEqualTo(userShouldBeFound);
	}

	@Test
	public void testFindUserByEmail() {
		User userShouldBeFound = entityManager
				.persistFlushFind(new User(null, "test", "test", "xyz@edu.unifi.it", 4000));

		User userFound = repository.findByEmail("xyz@edu.unifi.it");
		assertThat(userFound).isEqualTo(userShouldBeFound);
	}

	@Test
	public void testFindUserByUsernameOrName() {
		User user1 = entityManager.persistFlushFind(new User(null, "username", "test", "test", 2000));
		User user2 = entityManager.persistFlushFind(new User(null, "test", "name", "test", 1000));
		entityManager.persistFlushFind(
				new User(null, "Should Not Be Found", "Should Not Be Found", "Should Not Be Found", 1000));

		List<User> found = repository.findByUsernameOrName("username", "name");
		assertThat(found).containsExactly(user1, user2);
	}

	@Test
	public void testFindUserByUsernameOrEmail() {
		User user1 = entityManager.persistFlushFind(new User(null, "username", "test", "test", 2000));
		User user2 = entityManager.persistFlushFind(new User(null, "test", "test", "email", 1000));
		entityManager.persistFlushFind(
				new User(null, "Should Not Be Found", "Should Not Be Found", "Should Not Be Found", 1000));

		List<User> found = repository.findByUsernameOrEmail("username", "email");
		assertThat(found).containsExactly(user1, user2);
	}

	@Test
	public void testFindUserByNameOrEmail() {
		User user1 = entityManager.persistFlushFind(new User(null, "test", "name", "test", 2000));
		User user2 = entityManager.persistFlushFind(new User(null, "test", "test", "email", 1000));
		entityManager.persistFlushFind(
				new User(null, "Should Not Be Found", "Should Not Be Found", "Should Not Be Found", 1000));

		List<User> found = repository.findByNameOrEmail("name", "email");
		assertThat(found).containsExactly(user1, user2);
	}

	@Test
	public void testFindUserByUsernameOrNameOrEmail() {
		User user1 = entityManager.persistFlushFind(new User(null, "username", "test", "test", 2000));
		User user2 = entityManager.persistFlushFind(new User(null, "test", "name", "test", 500));
		User user3 = entityManager.persistFlushFind(new User(null, "test", "test", "email", 1000));
		entityManager.persistFlushFind(
				new User(null, "Should Not Be Found", "Should Not Be Found", "Should Not Be Found", 800));

		List<User> found = repository.findByUsernameOrNameOrEmail("username", "name", "email");
		assertThat(found).containsExactly(user1, user2, user3);
	}

	@Test
	public void testFindAllUsersWithLowBalance() {
		User user1 = entityManager.persistFlushFind(new User(null, "test", "test", "test", 500));
		User user2 = entityManager.persistFlushFind(new User(null, "test", "test", "test", 800));
		entityManager.persistFlushFind(
				new User(null, "Should Not Be Found", "Should Not Be Found", "Should Not Be Found", 1300));
		entityManager.persistFlushFind(
				new User(null, "Should Not Be Found", "Should Not Be Found", "Should Not Be Found", 1000));

		List<User> found = repository.findAllUsersWithLowBalance(1000L);
		assertThat(found).containsExactly(user1, user2);
	}
}