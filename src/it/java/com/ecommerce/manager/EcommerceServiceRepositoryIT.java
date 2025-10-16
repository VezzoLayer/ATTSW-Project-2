package com.ecommerce.manager;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecommerce.manager.model.User;
import com.ecommerce.manager.repositories.UserRepository;
import com.ecommerce.manager.services.EcommerceService;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(EcommerceService.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class EcommerceServiceRepositoryIT {

	@Autowired
	private EcommerceService ecommerceService;

	@Autowired
	private UserRepository userRepository;

	@Test
	public void testServiceCanInsertIntoUserRepository() {
		User savedUser = ecommerceService.insertNewUser(new User(null, "username", "name", "email", 2000));

		assertThat(userRepository.findById(savedUser.getId())).isPresent();
	}
}
