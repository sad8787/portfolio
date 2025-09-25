package es.uvigo.esei.xcs.jsf;

import java.security.Principal;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

@Named("login")
@RequestScoped
public class LoginManagedBean {
	@Inject
	private Principal currentUserPrincipal;
	
	@Inject
	private HttpServletRequest request;
	
	private String login;
	private String password;
	
	private boolean error = false;
	
	public String getLogin() {
		return login;
	}
	
	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isError() {
		return error;
	}
	
	public String doLogin() {
		try {
			request.login(this.getLogin(), this.getPassword());
			this.error = false;
			
			if (this.isAdmin()) {
				return redirectTo(this.getAdminViewId());
			} else if (this.isOwner()) {
				return redirectTo(this.getOwnerViewId());
			} else {
				return redirectTo(this.getViewId());
			}
		} catch (ServletException e) {
			this.error = true;
			
			return this.getViewId();
		}
	}
	
	public String doLogout() throws ServletException {
		request.logout();
		
		return redirectTo("/index.xhtml");
	}
	
	public Principal getCurrentUser() {
		return this.currentUserPrincipal;
	}
	
	private String redirectTo(String url) {
		return url  + "?faces-redirect=true";
	}
	
	private String getViewId() {
		return FacesContext.getCurrentInstance().getViewRoot().getViewId();
	}
	
	private String getOwnerViewId() {
		return "/owner/pets.xhtml";
	}
	
	private String getAdminViewId() {
		return "/admin/owners.xhtml";
	}
	
	private boolean isAdmin() {
		return this.isUserInRole("ADMIN");
	}
	
	private boolean isOwner() {
		return this.isUserInRole("OWNER");
	}
	
	private boolean isUserInRole(String role) {
		return FacesContext.getCurrentInstance().getExternalContext()
			.isUserInRole(role);
	}
}