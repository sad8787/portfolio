package es.uvigo.esei.xcs.jsf.pages;

import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Location;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

@Location("index.xhtml")
public class LoginPage {
	@Drone
	private WebDriver browser;
	
	@FindBy(id = "login-form")
	private LoginForm loginForm;
	
	public void login(String login, String password) {
		this.loginForm.login(login, password);
	}
	
	public void assertOnLoginPage() {
		assertThat(browser.getCurrentUrl(), containsString("/index.xhtml"));
	}
}
