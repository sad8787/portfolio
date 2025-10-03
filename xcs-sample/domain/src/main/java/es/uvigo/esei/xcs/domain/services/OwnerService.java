package es.uvigo.esei.xcs.domain.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import es.uvigo.esei.xcs.domain.entities.Owner;
import es.uvigo.esei.xcs.domain.entities.Pet;


@Stateless
public class OwnerService {

    @PersistenceContext
    private EntityManager em;
    @Context
    private SecurityContext securityContext;

    // ============ CRUD básico ============

    public Owner createOwner(Owner owner) {
        // Validar login duplicado
        if (em.find(Owner.class, owner.getLogin()) != null) {
            throw new WebApplicationException("Ya existe un OWNER con login: " + owner.getLogin(),
                                              Response.Status.CONFLICT);
        }
        // Todos pueden crear OWNER
        em.persist(owner);
        return owner;
    }
    public Owner findOwner(int id) {
        Owner owner = em.find(Owner.class, id);
        if (owner == null) {
            throw new WebApplicationException("Owner no encontrado", Response.Status.NOT_FOUND);
        }

        String currentUser = securityContext.getUserPrincipal().getName();
        boolean isAdmin = securityContext.isUserInRole("ADMIN");
        boolean isVet = securityContext.isUserInRole("VET");
        boolean isOwner = securityContext.isUserInRole("OWNER");

        // ADMIN y VET pueden ver cualquier owner
        if (isAdmin || isVet) {
            return owner;
        }

        // OWNER solo puede ver a sí mismo
        if (isOwner && currentUser.equals(owner.getLogin())) {
            return owner;
        }

        throw new WebApplicationException("No tienes permisos para ver este OWNER",
                                        Response.Status.FORBIDDEN);
    }

    public List<Owner> listOwners() {
        List<Owner> allOwners = em.createQuery("SELECT o FROM Owner o", Owner.class)
                                  .getResultList();

        String currentUser = securityContext.getUserPrincipal().getName();
        boolean isAdmin = securityContext.isUserInRole("ADMIN");
        boolean isVet = securityContext.isUserInRole("VET");
        boolean isOwner = securityContext.isUserInRole("OWNER");

        if (isAdmin) {
            return allOwners;
        } else if (isVet) {
            // VET ve owners que tienen pets atendidos por este vet
            return allOwners.stream()
                            .filter(o -> o.getPets().stream()
                                          .anyMatch(p -> p.getVets().stream()
                                                         .anyMatch(v -> v.getLogin().equals(currentUser))))
                            .collect(Collectors.toList());
        } else if (isOwner) {
            // OWNER solo ve a sí mismo
            return allOwners.stream()
                            .filter(o -> o.getLogin().equals(currentUser))
                            .collect(Collectors.toList());
        }

        throw new WebApplicationException("Rol no autorizado", Response.Status.FORBIDDEN);
    }

    public Owner updateOwner(Owner owner) {
        Owner existing = em.find(Owner.class, owner.getLogin());
        if (existing == null) {
            throw new WebApplicationException("Owner no encontrado", Response.Status.NOT_FOUND);
        }

        boolean isAdmin = securityContext.isUserInRole("ADMIN");
        String currentLogin = securityContext.getUserPrincipal().getName();

        if (!isAdmin && !currentLogin.equals(owner.getLogin())) {
            throw new WebApplicationException("No tienes permisos para modificar a este OWNER",
                                              Response.Status.FORBIDDEN);
        }

        return em.merge(owner);
    }

    public void deleteOwner(int id) {
        Owner owner = em.find(Owner.class, id);
        if (owner == null) {
            throw new WebApplicationException("Owner no encontrado", Response.Status.NOT_FOUND);
        }

        if (!securityContext.isUserInRole("ADMIN")) {
            throw new WebApplicationException("Solo un ADMIN puede eliminar owners",
                                              Response.Status.FORBIDDEN);
        }

        em.remove(owner);
    }
    //internal
    protected  Optional<Owner> findOwnerInternal(int id) {
        return Optional.ofNullable(em.find(Owner.class, id));
    }

    protected List<Owner> listOwnersInternal() {
        return em.createQuery("SELECT o FROM Owner o", Owner.class)
                 .getResultList();
    }

    protected Owner updateOwnerInternal(Owner owner) {
        return em.merge(owner);
    }

    protected void deleteOwnerInternal(int id) {
        Owner owner = em.find(Owner.class, id);
        if (owner != null) {
            em.remove(owner);
        }
    }

    // ============ Gestión de mascotas ============
    public List<Pet> listPets(int ownerId) {
        Owner owner = findOwner(ownerId); // Aplica seguridad automáticamente
        return Collections.unmodifiableList(new ArrayList<>(owner.getPets()));
    }

    public void addPetToOwner(int ownerId, Pet pet) {
        Owner owner = findOwner(ownerId); // Aplica seguridad

        // Si el pet ya tenía un owner distinto, lo eliminamos de ese owner
        Owner currentOwner = pet.getOwner();
        if (currentOwner != null && !currentOwner.equals(owner)) {
            currentOwner.removePet(pet);
        }

        owner.addPet(pet);

        if (pet.getId() == 0) {
            em.persist(pet);
        } else {
            em.merge(pet);
        }
    }

    public void removePetFromOwner(int ownerId, int petId) {
        Owner owner = findOwner(ownerId); // Aplica seguridad
        Pet pet = em.find(Pet.class, petId);
        if (pet == null) {
            throw new WebApplicationException("Pet no encontrado", Response.Status.NOT_FOUND);
        }
        if (!owner.ownsPet(pet)) {
            throw new WebApplicationException("Este pet no pertenece al OWNER", Response.Status.FORBIDDEN);
        }

        owner.removePet(pet);
        em.merge(owner);
        em.merge(pet);
    }
   

}

