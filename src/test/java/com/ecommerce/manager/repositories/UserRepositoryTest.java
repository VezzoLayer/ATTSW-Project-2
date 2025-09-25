package com.ecommerce.manager.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

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
}
