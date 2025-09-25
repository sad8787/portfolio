package es.uvigo.esei.xcs.jsf;

import static java.util.stream.Collectors.joining;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import es.uvigo.esei.xcs.domain.entities.Owner;
import es.uvigo.esei.xcs.domain.entities.Pet;
import es.uvigo.esei.xcs.service.OwnerService;

@Named("owner")
@RequestScoped
public class OwnerManagedBean {
	@Inject
	private OwnerService service;
	
	private String login;
	private String password;
	
	private boolean editing;
	
	private String errorMessage;
	
	public String getLogin() {
		return login;
	}
	
	public void setLogin(String name) {
		this.login = name;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public boolean isError() {
		return this.errorMessage != null;
	}
	
	public boolean isEditing() {
		return this.editing;
	}
	
	public void setEditing(boolean editing) {
		this.editing = editing;
	}
	
	public List<Owner> getOwners() {
		return this.service.list();
	}
	
	public String getPetNames(String login) {
		return this.service.getPets(login).stream()
			.map(Pet::getName)
		.collect(joining(", "));
	}
	
	public String edit(String login) {
		this.editing = true;
		this.login = login;
		
		return this.getViewId();
	}
	
	public String cancelEditing() {
		this.clear();
		
		return this.getViewId();
	}
	
	public String remove(String login) {
		this.service.remove(login);
		
		return redirectTo(this.getViewId());
	}
	
	public String store() {
		try {
			if (this.isEditing()) {
				final Owner owner = this.service.get(this.login);
				owner.changePassword(this.password);
				
				this.service.update(owner);
			} else {
				this.service.create(new Owner(login, password));
			}

			this.clear();
			
			return redirectTo(this.getViewId());
		} catch (Throwable t) {
			this.errorMessage = t.getMessage();
			
			return this.getViewId();
		}
	}
	
	private void clear() {
		this.login = null;
		this.password = null;
		this.errorMessage = null;
		this.editing = false;
	}

	private String redirectTo(String url) {
		return url  + "?faces-redirect=true";
	}
	
	private String getViewId() {
		return FacesContext.getCurrentInstance().getViewRoot().getViewId();
	}
}
