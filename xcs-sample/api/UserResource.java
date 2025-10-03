package es.uvigo.esei.xcs.api;

import es.uvigo.esei.xcs.domain.entities.User;
import es.uvigo.esei.xcs.domain.services.UserService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource<T extends User> {

    @Inject
    private UserService<T> userService;

    // Crear un nuevo usuario
    @POST
    public Response createUser(T user) {
        userService.create(user);
        return Response.status(Response.Status.CREATED).entity(user).build();
    }

    // Obtener todos los usuarios
    @GET
    public List<T> getAllUsers() {
        return userService.findAll();
    }

    // Buscar usuario por login
    @GET
    @Path("/{login}")
    public Response getUserByLogin(@PathParam("login") String login) {
        return userService.findByLogin(login)
                .map(user -> Response.ok(user).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    // Actualizar usuario
    @PUT
    @Path("/{login}")
    public Response updateUser(@PathParam("login") String login, T user) {
        return userService.findByLogin(login)
                .map(existing -> {
                    userService.update(user);
                    return Response.ok(user).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    // Eliminar usuario
    @DELETE
    @Path("/{login}")
    public Response deleteUser(@PathParam("login") String login) {
        return userService.findByLogin(login)
                .map(existing -> {
                    userService.delete(existing);
                    return Response.noContent().build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}

