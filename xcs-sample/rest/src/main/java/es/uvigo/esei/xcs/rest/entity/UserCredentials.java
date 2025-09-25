package es.uvigo.esei.xcs.rest.entity;

import java.io.Serializable;

import es.uvigo.esei.xcs.domain.entities.User;

public class UserCredentials implements Serializable {
  private static final long serialVersionUID = 1L;
  
  private String login;
  private String role;

  public UserCredentials(User user) {
    this.login = user.getLogin();
    this.role = user.getRole();
  }

  public String getLogin() {
    return login;
  }

  public String getRole() {
    return role;
  }

}
