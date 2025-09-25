package es.uvigo.esei.xcs.rest;

import javax.ejb.EJB;
import javax.ejb.EJBAccessException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import es.uvigo.esei.xcs.domain.entities.User;
import es.uvigo.esei.xcs.rest.entity.UserCredentials;
import es.uvigo.esei.xcs.service.UserService;

@Path("user")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
  @EJB
  private UserService service;

  @GET
  public Response getCredentials() {
    try {
      final User currentUser = this.service.getCurrentUser();

      return Response.ok(new UserCredentials(currentUser)).build();
    } catch (EJBAccessException eae) {
      throw new SecurityException(eae);
    }
  }
}
