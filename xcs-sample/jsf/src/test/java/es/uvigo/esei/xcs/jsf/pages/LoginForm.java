package es.uvigo.esei.xcs.jsf.pages;

import static org.jboss.arquillian.graphene.Graphene.guardHttp;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginForm {
	@FindBy(id = "login-form:login-field")
	private WebElement inputLogin;
	
	@FindBy(id = "login-form:password-field")
	private WebElement inputPassword;
	
	@FindBy(id = "login-form:submit")
	private WebElement buttonSubmit;
	
	public void login(String login, String password) {
		inputLogin.clear();
		inputPassword.clear();
		
		inputLogin.sendKeys(login);
		inputPassword.sendKeys(password);
		
		guardHttp(buttonSubmit).click();
	}
}
