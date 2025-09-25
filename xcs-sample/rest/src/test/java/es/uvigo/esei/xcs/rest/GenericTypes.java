package es.uvigo.esei.xcs.rest;

import java.util.List;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import es.uvigo.esei.xcs.domain.entities.Owner;

public final class GenericTypes {
	private GenericTypes() {}
	
	public static class ListOwnerType extends GenericType<List<Owner>> {
		public static ListOwnerType INSTANCE = new ListOwnerType();
		
		public static List<Owner> readEntity(Response response) {
			return response.readEntity(INSTANCE);
		}
	}
}
