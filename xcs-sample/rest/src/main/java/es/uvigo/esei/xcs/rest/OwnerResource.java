package es.uvigo.esei.xcs.rest;

import java.net.URI;

import javax.ejb.EJB;
import javax.persistence.EntityExistsException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import es.uvigo.esei.xcs.domain.entities.Owner;
import es.uvigo.esei.xcs.rest.entity.OwnerEditionData;
import es.uvigo.esei.xcs.rest.entity.OwnerCreationData;
import es.uvigo.esei.xcs.service.OwnerService;

/**
 * Resource that represents the owners in the application.
 * 
 * @author Miguel Reboiro Jato
 */
@Path("owner")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OwnerResource {
	@EJB
	private OwnerService service;
	
	@Context
	private UriInfo uriInfo;

	/**
	 * Returns the owner identified by the login.
	 * 
	 * @param login the login of an owner.
	 * @return an {@code OK} response containing the {@link Owner} with the
	 * provided login.
	 * @throws IllegalArgumentException if {@code login} is {@code null} or
	 * if it does not correspond with any owner.
	 */
	@Path("{login}")
	@GET
	public Response get(@PathParam("login") String login) {
		if (login == null)
			throw new IllegalArgumentException("login can't be null");
		
		final Owner owner = this.service.get(login);
		
		if (owner == null)
			throw new IllegalArgumentException("Owner not found: " + login);
		else
			return Response.ok(owner).build();
	}

	/**
	 * Returns the list of owners stored in the application.
	 * 
	 * @return an {@code OK} response containing the list of owners stored in
	 * the application.
	 */
	@GET
	public Response list() {
		return Response.ok(this.service.list()).build();
	}
	
	/**
	 * Creates a new owner. This owner may include a list of pets, that will be
	 * also created.
	 * 
	 * @param ownerData a new owner to be stored.
	 * @return a {@code CREATED} response with the URI of the new owner in the
	 * {@code Location} header.
	 * @throws IllegalArgumentException if owner is {@code null} or if an owner
	 * with the same login already exists.
	 */
	@POST
	public Response create(OwnerCreationData ownerData) {
	  if (ownerData == null) {
	    throw new IllegalArgumentException("ownerData can't be null");
	  }
	  
		try {
			final Owner newOwner = this.service.create(ownerData.toOwner());
			final URI ownerUri = uriInfo.getAbsolutePathBuilder()
				.path(newOwner.getLogin())
			.build();
			
			return Response.created(ownerUri).build();
		} catch (EntityExistsException eee) {
			throw new IllegalArgumentException("The owner already exists");
		}
	}
	
	/**
	 * Updates an owner. This owner may include a list of pets, that will be
	 * also created or updated. If the owner does not exists it will be created.
	 * 
	 * @param ownerData an owner to be updated.
	 * @return an empty {@code OK} response.
	 * @throws IllegalArgumentException if owner is {@code null}.
	 */
	@Path("{login}")
	@PUT
	public Response update(@PathParam("login") String login, OwnerEditionData ownerData) {
	  if (login == null) {
	    throw new IllegalArgumentException("login can't be null");
	  }
    if (ownerData == null) {
      throw new IllegalArgumentException("ownerData can't be null");
    }
	  
	  final Owner owner = this.service.get(login);
	  
	  ownerData.assignData(owner);
	  
		this.service.update(owner);
		
		return Response.ok().build();
	}
	
	/**
	 * Deletes an owner.
	 * 
	 * @param login the login of the owner to be deleted.
	 * @return an empty {@code OK} response.
	 * @throws IllegalArgumentException if {@code login} is {@code null} or if
	 * it does not identifies a valid owner.
	 */
	@Path("{login}")
	@DELETE
	public Response delete(@PathParam("login") String login) {
		if (login == null)
			throw new IllegalArgumentException("login can't be null");
		
		this.service.remove(login);
		
		return Response.ok().build();
	}
}
