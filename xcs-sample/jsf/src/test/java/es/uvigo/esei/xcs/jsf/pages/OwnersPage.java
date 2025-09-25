package es.uvigo.esei.xcs.jsf.pages;

import static java.util.Arrays.stream;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.GrapheneElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import es.uvigo.esei.xcs.domain.entities.Owner;

public class OwnersPage {
	@Drone
	private WebDriver browser;

	@FindBy(id = "owner-form")
	private OwnerForm formOwner;
	
	@FindBy(id = "owners-table")
	private OwnersTable tableOwners;
	
	// GrapheneElement adds "isPresent()" to WebElement.
	@FindBy(id = "store-error")
	private GrapheneElement storeError;
	
	public void assertOnOwnersPage() {
		assertThat(browser.getCurrentUrl(), containsString("/owners.xhtml"));
	}
	
	public boolean areOwnersInTable(Owner ... owners) {
		return stream(owners).allMatch(this::isOwnerInTable);
	}
	
	public boolean isOwnerInTable(Owner owner) {
		return this.tableOwners.hasOwner(owner);
	}
	
	public void createOwner(String login, String password) {
		this.formOwner.setName(login);
		this.formOwner.setPassword(password);
		this.formOwner.submit();
	}
	
	public void removeOwner(Owner owner) {
		this.tableOwners.remove(owner);
	}
	
	public void editOwner(Owner owner) {
		this.tableOwners.edit(owner);
	}

	public void changePassword(String password) {
		this.formOwner.setPassword(password);
		this.formOwner.submit();
	}
	
	public boolean isErrorMessageVisible() {
		return storeError.isPresent();
	}
	
	public boolean isEditing() {
		return this.formOwner.isEditing();
	}

	public void cancelEdit() {
		this.formOwner.cancel();
	}
}
