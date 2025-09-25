package es.uvigo.esei.xcs.rest.entity;

import java.io.Serializable;

import es.uvigo.esei.xcs.domain.entities.Owner;

public class OwnerEditionData implements Serializable {
  private static final long serialVersionUID = 1L;
  private String password;

  OwnerEditionData() {}

  public OwnerEditionData(String password) {
    this.password = password;
  }

  public String getPassword() {
    return password;
  }

  public void assignData(Owner owner) {
    owner.changePassword(this.password);
  }

}
