package es.uvigo.esei.xcs.domain.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import es.uvigo.esei.xcs.domain.entities.User;

public abstract class UserService<T extends User> {

    @PersistenceContext
    protected EntityManager em;
    @Context
    private SecurityContext securityContext;

    private final Class<T> type;

    public UserService(Class<T> type) {
        this.type = type;
    }

    /**
     * Crea un nuevo usuario, validando que el login no esté repetido.
     * 
     * @param user usuario a persistir
     * @throws IllegalArgumentException si ya existe un usuario con el mismo login
     */
    public void create(T user) {
        // Validar login duplicado
        if (existsByLogin(user.getLogin())) {
            throw new IllegalArgumentException("Ya existe un usuario con login: " + user.getLogin());
        }
        String roleToCreate = user.getRole();
        boolean isAdmin = securityContext.isUserInRole("ADMIN");

        switch (roleToCreate) {
            case "OWNER":
                // Cualquier usuario puede crear un OWNER
                em.persist(user);
                break;
            case "VET":
            case "ADMIN":
                // Solo ADMIN puede crear VET o ADMIN
                if (isAdmin) {
                    em.persist(user);
                } else {
                    throw new WebApplicationException(
                        "Solo un ADMIN puede crear usuarios de tipo " + roleToCreate,
                        Response.Status.FORBIDDEN
                    );
                }
                break;
            default:
                throw new IllegalArgumentException("Rol desconocido: " + roleToCreate);
        }
    }


    /**
     * Comprueba si existe un usuario con un login dado.
     */
    public boolean existsByLogin(String login) {
        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(u) FROM " + type.getSimpleName() + " u WHERE u.login = :login",
            Long.class
        );
        query.setParameter("login", login);
        return query.getSingleResult() > 0;
    }

   
    /**
     * Busca un usuario por su login aplicando las reglas de seguridad:
     * - OWNER: solo puede ver VETs
     * - VET: puede ver OWNERs y VETs
     * - ADMIN: puede ver todos
     *
     * @param login el login del usuario a buscar
     * @return un Optional con el usuario encontrado o vacío si no tiene permisos o no existe
     */
    public Optional<T> findByLogin(String login) {
        // Buscar usuario por login
        TypedQuery<T> query = em.createQuery(
            "SELECT u FROM " + type.getSimpleName() + " u WHERE u.login = :login",
            type
        );
        query.setParameter("login", login);
        List<T> results = query.getResultList();

        if (results.isEmpty()) {
            // Usuario no encontrado
            return Optional.empty();
        }

        T user = results.get(0);

        // Determinar rol del usuario que hace la petición
        boolean isAdmin = securityContext.isUserInRole("ADMIN");
        boolean isVet   = securityContext.isUserInRole("VET");
        boolean isOwner = securityContext.isUserInRole("OWNER");

        // Reglas de acceso
        if (isAdmin) {
            // ADMIN puede ver todos
            return Optional.of(user);
        } else if (isVet) {
            // VET puede ver OWNERs y VETs
            if ("OWNER".equals(user.getRole()) || "VET".equals(user.getRole())) {
                return Optional.of(user);
            }
        } else if (isOwner) {
            // OWNER solo puede ver VETs
            if ("VET".equals(user.getRole())) {
                return Optional.of(user);
            }
        }

        // Si no cumple las reglas, no tiene permiso
        return Optional.empty();
    }


     /**
     * Listar usuarios según rol:
     * - OWNER: solo ve VETs
     * - VET: ve OWNERs y VETs
     * - ADMIN: ve todos
     */
    public List<T> findAll() {
        List<T> allUsers = em.createQuery("SELECT u FROM " + type.getSimpleName() + " u", type)
                             .getResultList();

        if (securityContext.isUserInRole("ADMIN")) {
            return allUsers;
        } else if (securityContext.isUserInRole("VET")) {
            return allUsers.stream()
                           .filter(u -> u.getRole().equals("VET") || u.getRole().equals("OWNER"))
                           .collect(Collectors.toList());
        } else if (securityContext.isUserInRole("OWNER")) {
            return allUsers.stream()
                           .filter(u -> u.getRole().equals("VET"))
                           .collect(Collectors.toList());
        } else {
            throw new WebApplicationException("Rol no autorizado", Response.Status.FORBIDDEN);
        }
    }

    public void update(T user) {
        T existingUser = em.find(type, user.getLogin());
        if (existingUser == null) {
            throw new WebApplicationException(
                "Usuario no encontrado",
                Response.Status.NOT_FOUND
            );
        }
        boolean isAdmin = securityContext.isUserInRole("ADMIN");
        String currentLogin = securityContext.getUserPrincipal().getName();

        if (!isAdmin && !currentLogin.equals(user.getLogin())) {
            throw new WebApplicationException(
                "No tienes permisos para modificar a este usuario",
                Response.Status.FORBIDDEN
            );
        }
        em.merge(user);
    }

    public void delete(T user) {
        T existingUser = em.find(type, user.getLogin());
        if (existingUser == null) {
            throw new WebApplicationException(
                "Usuario no encontrado",
                Response.Status.NOT_FOUND
            );
        }
        if (!securityContext.isUserInRole("ADMIN")) {
            throw new WebApplicationException(
                "Solo un ADMIN puede eliminar usuarios",
                Response.Status.FORBIDDEN
            );
        }
        if (!em.contains(user)) {
            user = em.merge(user);
        }
        em.remove(user);
    }
}
    
