package es.uvigo.esei.xcs.api;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import es.uvigo.esei.xcs.domain.entities.PetVaccine;
import es.uvigo.esei.xcs.domain.services.PetVaccineService;
/*POST /api/petvaccines → crear

GET /api/petvaccines/{id} → obtener por ID

GET /api/petvaccines → listar todas

GET /api/petvaccines/pet/{petId} → listar por mascota

GET /api/petvaccines/pet/{petId}/periodic → listar periódicas por mascota

DELETE /api/petvaccines/{id} → eliminar */
@Path("/petvaccines")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PetVaccineResource {

    @Inject
    private PetVaccineService petVaccineService;

    // Obtener todos los registros de PetVaccine
    @GET
    public List<PetVaccine> getAllPetVaccines() {
        return petVaccineService.findAll();
    }

    // Obtener un PetVaccine por ID
    @GET
    @Path("/{id}")
    public Response getPetVaccineById(@PathParam("id") int id) {
        PetVaccine pv = petVaccineService.findById(id).orElseThrow(() -> new WebApplicationException("PetVaccine no encontrado", Response.Status.NOT_FOUND));
        return Response.ok(pv).build();
    }

    // Crear un nuevo PetVaccine
    @POST
    public Response createPetVaccine(PetVaccine pv) {
        petVaccineService.create(pv);
        return Response.status(Response.Status.CREATED).entity(pv).build();
    }

    @PUT
    @Path("/{id}")
    public Response updatePetVaccine(@PathParam("id") int id, PetVaccine pv) {
        PetVaccine existing = petVaccineService.findById(id)
            .orElseThrow(() -> new WebApplicationException("PetVaccine no encontrado", Response.Status.NOT_FOUND));

        // Actualiza los campos necesarios
        existing.setPet(pv.getPet());
        existing.setVaccine(pv.getVaccine());
        existing.setDate(pv.getDate());

        petVaccineService.update(existing); // void, no asignar
        return Response.ok(existing).build(); // retornamos la entidad actualizada
    }



    // Eliminar un PetVaccine
    @DELETE
    @Path("/{id}")
    public Response deletePetVaccine(@PathParam("id") int id) {
        PetVaccine existing = petVaccineService.findById(id).orElseThrow(() -> new WebApplicationException("PetVaccine no encontrado", Response.Status.NOT_FOUND));
        
        petVaccineService.delete(existing);
        return Response.noContent().build();
    }
}


