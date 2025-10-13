package com.ecommerce.manager.controllers;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
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

		// Scorro tutto il documento e cerco gli elementi 'a' che soddisfano il criterio
		assertThat(page.getByXPath("//a[text()='Show Orders']")).isEmpty();
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
				1 u1 n1 e1 1000 Edit Handle Balance
				2 u2 n2 e2 2000 Edit Handle Balance""";

		// replace /t con spazi bianchi e rimuove /r
		assertThat(table.asNormalizedText().replace("\t", " ").replace("\r", "")).isEqualTo(expectedTableContent);

		page.getAnchorByHref("/editUser/1");
		page.getAnchorByHref("/editUser/2");

		page.getAnchorByHref("/1/handle_balance");
		page.getAnchorByHref("/2/handle_balance");
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

	@Test
	public void testHandleBalancePageTitle() throws Exception {
		HtmlPage page = webClient.getPage("/1/handle_balance");

		assertThat(page.getTitleText()).isEqualTo("Handle Balance");
	}

	@Test
	public void testHandleBalanceOfANonExistingUser() throws Exception {
		when(userService.getUserById(1L)).thenReturn(null);

		HtmlPage page = this.webClient.getPage("/1/handle_balance");

		assertThat(page.getBody().getTextContent()).contains("No user found with id: 1");
	}

	@Test
	public void testHandleBalanceOfExistingUserShouldDisplayIdAndBalance() throws Exception {
		when(userService.getUserById(1L)).thenReturn(new User(1L, "u1", "n1", "e1", 1500));

		HtmlPage page = this.webClient.getPage("/1/handle_balance");

		assertThat(page.getBody().getTextContent()).doesNotContain("No user found with id: 1");

		String bodyText = page.getBody().getTextContent();

		assertThat(bodyText).contains("User ID: 1");
		assertThat(bodyText).contains("Balance: 1500");
	}

	@Test
	public void testDepositWhenAmountIsAllowedShouldCallService() throws Exception {
		when(userService.getUserById(1L)).thenReturn(new User(1L, "u1", "n1", "e1", 1500));

		HtmlPage page = this.webClient.getPage("/1/handle_balance");

		HtmlForm form = page.getFormByName("deposit_form");

		form.getInputByName("amount").setValueAttribute("500");
		form.getButtonByName("btn_deposit").click();

		verify(userService).deposit(1L, 500L);
	}

	@Test
	public void testDepositWhenIllegalArgumentExceptionIsRaisedShouldRedirectToHomeWithErrorMessage() throws Exception {
		when(userService.getUserById(1L)).thenReturn(new User(1L, "u1", "n1", "e1", 1500));
		doThrow(new IllegalArgumentException("Amount must be positive")).when(userService).deposit(1L, -500L);

		HtmlPage page = this.webClient.getPage("/1/handle_balance");

		HtmlForm form = page.getFormByName("deposit_form");

		form.getInputByName("amount").setValueAttribute("-500");
		HtmlPage resultPage = form.getButtonByName("btn_deposit").click();

		verify(userService).deposit(1L, -500L);
		assertThat(resultPage.getBody().getTextContent()).contains("Amount must be positive");
	}

	@Test
	public void testWithdrawWhenAmountIsAllowedShouldCallService() throws Exception {
		when(userService.getUserById(1L)).thenReturn(new User(1L, "u1", "n1", "e1", 1500));

		HtmlPage page = this.webClient.getPage("/1/handle_balance");

		HtmlForm form = page.getFormByName("withdraw_form");

		form.getInputByName("amount").setValueAttribute("500");
		form.getButtonByName("btn_withdraw").click();

		verify(userService).withdraw(1L, 500L);
	}

	@Test
	public void testWithdrawWhenIllegalArgumentExceptionIsRaisedShouldRedirectToHomeWithErrorMessage()
			throws Exception {
		when(userService.getUserById(1L)).thenReturn(new User(1L, "u1", "n1", "e1", 1500));
		doThrow(new IllegalArgumentException("Amount must be positive")).when(userService).withdraw(1L, -500L);

		HtmlPage page = this.webClient.getPage("/1/handle_balance");

		HtmlForm form = page.getFormByName("withdraw_form");

		form.getInputByName("amount").setValueAttribute("-500");
		HtmlPage resultPage = form.getButtonByName("btn_withdraw").click();

		verify(userService).withdraw(1L, -500L);
		assertThat(resultPage.getBody().getTextContent()).contains("Amount must be positive");
	}

	@Test
	public void testWithdrawWhenIllegalStateExceptionIsRaisedShouldRedirectToHomeWithErrorMessage() throws Exception {
		when(userService.getUserById(1L)).thenReturn(new User(1L, "u1", "n1", "e1", 300));
		doThrow(new IllegalStateException("Balance is not enough")).when(userService).withdraw(1L, 500L);

		HtmlPage page = this.webClient.getPage("/1/handle_balance");

		HtmlForm form = page.getFormByName("withdraw_form");

		form.getInputByName("amount").setValueAttribute("500");
		HtmlPage resultPage = form.getButtonByName("btn_withdraw").click();

		verify(userService).withdraw(1L, 500L);
		assertThat(resultPage.getBody().getTextContent()).contains("Balance is not enough");
	}

	@Test
	public void testHomePageShouldHaveALinkForShowingOrdersWhenThereIsAtLeastOneUser() throws Exception {
		when(userService.getAllUsers()).thenReturn(asList(new User(1L, "u1", "n1", "e1", 1000)));

		HtmlPage page = this.webClient.getPage("/");

		assertThat(page.getAnchorByText("Show Orders").getHrefAttribute()).isEqualTo("/orders");
	}
}
