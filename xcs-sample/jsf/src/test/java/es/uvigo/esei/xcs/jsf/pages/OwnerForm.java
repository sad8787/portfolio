package es.uvigo.esei.xcs.jsf.pages;

import static org.jboss.arquillian.graphene.Graphene.guardHttp;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class OwnerForm {
	@FindBy(id = "owner-form:name-field")
	private WebElement fieldName;
	@FindBy(id = "owner-form:password-field")
	private WebElement fieldPassword;
	@FindBy(id = "owner-form:submit-button")
	private WebElement buttonSubmit;
	@FindBy(id = "owner-form:cancel-button")
	private WebElement buttonCancel;
	
	
	public void setName(String name) {
		this.fieldName.sendKeys(name);
	}
	
	public void setPassword(String password) {
		this.fieldPassword.sendKeys(password);
	}
	
	public String getName() {
		return this.fieldName.getText();
	}
	
	public String getPassword() {
		return this.fieldPassword.getText();
	}
	
	public void submit() {
		guardHttp(this.buttonSubmit).click();
	}
	
	public void cancel() {
		guardHttp(this.buttonCancel).click();
	}

	public boolean isEditing() {
		return this.fieldName.getAttribute("readonly") != null;
	}
}
