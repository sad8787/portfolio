package es.uvigo.esei.xcs.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SecurityExceptionMapper implements ExceptionMapper<SecurityException> {
	@Override
	public Response toResponse(SecurityException e) {
		return Response.status(Response.Status.FORBIDDEN)
			.entity(e.getMessage())
			.type(MediaType.TEXT_PLAIN)
		.build();
	}
}
