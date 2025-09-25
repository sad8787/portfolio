package es.uvigo.esei.xcs.service;

import java.security.Principal;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import es.uvigo.esei.xcs.domain.entities.User;

@Stateless
@RolesAllowed({"OWNER", "ADMIN"})
public class UserService {
  @PersistenceContext
  private EntityManager em;
  
  @Inject
  private Principal principal;

  /**
   * Returns the current user entity.
   * 
   * @return the entity with the information of the current user.
   */
  public User getCurrentUser() {
    return this.em.find(User.class, this.principal.getName());
  }
}
