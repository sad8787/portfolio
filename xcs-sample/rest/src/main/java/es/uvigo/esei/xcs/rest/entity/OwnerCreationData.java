package es.uvigo.esei.xcs.rest.entity;

import java.io.Serializable;

import es.uvigo.esei.xcs.domain.entities.Owner;

public class OwnerCreationData implements Serializable {
  private static final long serialVersionUID = 1L;
  
  private String login;
  private String password;

  OwnerCreationData() {}

  public OwnerCreationData(String login, String password) {
    this.login = login;
    this.password = password;
  }
  
  public String getLogin() {
    return login;
  }

  public String getPassword() {
    return password;
  }

  public Owner toOwner() {
    return new Owner(this.login, this.password);
  }
}
