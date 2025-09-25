package es.uvigo.esei.xcs.jsf.pages;

import static org.jboss.arquillian.graphene.Graphene.guardHttp;

import java.util.Collection;
import java.util.List;

import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import es.uvigo.esei.xcs.domain.entities.Owner;
import es.uvigo.esei.xcs.domain.entities.Pet;

public class OwnersTable {
	@FindByJQuery("tbody tr")
	private List<OwnerRow> trOwner;
	
	public boolean hasOwner(Owner owner) {
		for (OwnerRow row : trOwner) {
			if (row.hasOwner(owner))
				return true;
		}
		
		return false;
	}
	
	public OwnerRow getOwnerRow(Owner owner) {
		for (OwnerRow row : trOwner) {
			if (row.hasOwner(owner))
				return row;
		}
		
		throw new IllegalArgumentException("No row for owner: " + owner.getLogin());
	}
	
	public void remove(Owner owner) {
		guardHttp(this.getOwnerRow(owner).getButtonRemove()).click();
	}
	
	public void edit(Owner owner) {
		guardHttp(this.getOwnerRow(owner).getButtonEdit()).click();
	}
	
	public static class OwnerRow {
		@FindBy(className = "owners-table-login")
		private WebElement tdLogin;
		
		@FindBy(className = "owners-table-password")
		private WebElement tdPassword;
		
		@FindBy(className = "owners-table-pets")
		private WebElement tdPets;
		
		@FindBy(className = "owners-table-remove")
		private WebElement buttonRemove;
		
		@FindBy(className = "owners-table-edit")
		private WebElement buttonEdit;
		
		public boolean hasOwner(Owner owner) {
			return this.getLoginText().equals(owner.getLogin())
				&& this.getPasswordText().equals(owner.getPassword())
				&& arePetNamesInTest(owner.getPets(), this.getPetsText());
		}
		
		private String getLoginText() {
			return this.tdLogin.getText().trim();
		}
		
		private String getPasswordText() {
			return this.tdPassword.getText().trim();
		}
		
		private String getPetsText() {
			return this.tdPets.getText().trim();
		}
		
		public WebElement getButtonRemove() {
			return this.buttonRemove;
		}
		
		public WebElement getButtonEdit() {
			return this.buttonEdit;
		}
		
		private static boolean arePetNamesInTest(Collection<Pet> pets, String text) {
			return pets.stream()
				.map(Pet::getName)
			.allMatch(text::contains);
		}
	}
}
