package com.ecommerce.manager.controllers;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlTable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecommerce.manager.model.User;
import com.ecommerce.manager.services.UserService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = UserWebController.class)
public class UserWebControllerHtmlUnitTest {

	@Autowired
	private WebClient webClient;

	@MockitoBean
	private UserService userService;

	@Test
	public void testHomePageTitle() throws Exception {
		HtmlPage page = webClient.getPage("/");

		assertThat(page.getTitleText()).isEqualTo("Users");
	}

	@Test
	public void testHomePageWithNoUsers() throws Exception {
		when(userService.getAllUsers()).thenReturn(emptyList());

		HtmlPage page = this.webClient.getPage("/");

		assertThat(page.getBody().getTextContent()).contains("No Users");
	}

	@Test
	public void testHomePageWithUsersShouldShowThemInATable() throws Exception {
		when(userService.getAllUsers())
				.thenReturn(asList(new User(1L, "u1", "n1", "e1", 1000), new User(2L, "u2", "n2", "e2", 2000)));

		HtmlPage page = this.webClient.getPage("/");

		assertThat(page.getBody().getTextContent()).doesNotContain("No Users");

		HtmlTable table = page.getHtmlElementById("users_table");

		String expectedTableContent = """
				Users
				ID Username Name Email Balance
				1 u1 n1 e1 1000
				2 u2 n2 e2 2000""";

		// replace /t con spazi bianchi e rimuove /r
		assertThat(table.asNormalizedText().replace("\t", " ").replace("\r", "")).isEqualTo(expectedTableContent);
	}

	@Test
	public void testEditUserPageTitle() throws Exception {
		HtmlPage page = webClient.getPage("/editUser/1");

		assertThat(page.getTitleText()).isEqualTo("Edit User");
	}

	@Test
	public void testEditNonExistingUser() throws Exception {
		when(userService.getUserById(1L)).thenReturn(null);

		HtmlPage page = this.webClient.getPage("/editUser/1");

		assertThat(page.getBody().getTextContent()).contains("No user found with id: 1");
	}

	@Test
	public void testEditExistingUser() throws Exception {
		when(userService.getUserById(1)).thenReturn(new User(1L, "original u", "original n", "original e", 1000));

		HtmlPage page = this.webClient.getPage("/editUser/1");

		final HtmlForm form = page.getFormByName("user_form");

		form.getInputByValue("original u").setValueAttribute("modified u");
		form.getInputByValue("original n").setValueAttribute("modified n");
		form.getInputByValue("original e").setValueAttribute("modified e");
		form.getInputByValue("1000").setValueAttribute("2000");

		form.getButtonByName("btn_submit").click();

		verify(userService).updateUserById(1L, new User(1L, "modified u", "modified n", "modified e", 2000));
	}

	@Test
	public void testEditNewUser() throws Exception {
		HtmlPage page = this.webClient.getPage("/newUser");

		final HtmlForm form = page.getFormByName("user_form");

		form.getInputByName("username").setValueAttribute("new u");
		form.getInputByName("name").setValueAttribute("new n");
		form.getInputByName("email").setValueAttribute("new e");
		form.getInputByName("balance").setValueAttribute("2000");

		form.getButtonByName("btn_submit").click();

		verify(userService).insertNewUser(new User(null, "new u", "new n", "new e", 2000));
	}

	@Test
	public void testHomePageShouldHaveALinkForCreatingANewUser() throws Exception {
		HtmlPage page = this.webClient.getPage("/");

		assertThat(page.getAnchorByText("New User").getHrefAttribute()).isEqualTo("/newUser");
	}
}
