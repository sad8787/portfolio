package es.uvigo.esei.xcs.domain.services;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import es.uvigo.esei.xcs.utils.EmailSender;

public abstract class UserService<T extends User> {

    @PersistenceContext
    protected EntityManager em;
    @Context
    protected SecurityContext securityContext;

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
                em.persist(user);
                break;

            case "VET":
            case "ADMIN":
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

        // --- Envío de correo en un hilo separado ---
        new Thread(() -> {
            try {
                String configPath = "src/main/resources/email-config.txt";
                EmailSender emailSender = new EmailSender(configPath);

                String subject = "Bienvenido a XCS";
                String body = String.format(
                    "Hola %s,%n%nTu cuenta ha sido creada exitosamente como %s.%n%nSaludos,%nEquipo XCS",
                    user.getLogin(), user.getRole()
                );

                emailSender.sendEmail(user.getLogin(), subject, body);
            } catch (Exception e) {
                System.err.println("⚠️ Error al enviar correo a " + user.getLogin() + ": " + e.getMessage());
            }
        }).start();
    }


    /**
     
     * - OWNER: solo ve VETs
     * - VET: ve OWNERs y VETs
     * - ADMIN: ve todos
   
     * Filtra usuarios según rol del usuario actual.
     */
    protected boolean canView(User target) {
        if (securityContext.isUserInRole("ADMIN")) return true;
        if (securityContext.isUserInRole("VET")) {
            return "OWNER".equals(target.getRole()) || "VET".equals(target.getRole());
        }
        if (securityContext.isUserInRole("OWNER")) {
            return "VET".equals(target.getRole());
        }
        return false;
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
        TypedQuery<T> query = em.createQuery(
            "SELECT u FROM " + type.getSimpleName() + " u WHERE u.login = :login",
            type
        );
        query.setParameter("login", login);
        List<T> results = query.getResultList();

        if (results.isEmpty()) return Optional.empty();

        T user = results.get(0);

        if (!canView(user)) {
            throw new WebApplicationException(
                "No tienes permisos para ver este usuario",
                Response.Status.FORBIDDEN
            );
        }

        return Optional.of(user);
    }



    /**
     * Lista todos los usuarios que el usuario actual puede ver.
     */
    public List<T> findAll() {
        List<T> allUsers = em.createQuery("SELECT u FROM " + type.getSimpleName() + " u", type)
                             .getResultList();

        return allUsers.stream()
                       .filter(this::canView)
                       .collect(Collectors.toList());
    }
    // ======================== Listados públicos ========================



    private List<T> filterByRole(String role) {
        List<T> allUsers = em.createQuery("SELECT u FROM " + type.getSimpleName() + " u", type)
                             .getResultList();
        return allUsers.stream()
                       .filter(this::canView)
                       .filter(u -> role.equals(u.getRole()))
                       .collect(Collectors.toList());
    }    

    /** Listar solo ADMINs que puedo ver */
    public List<T> listAdmins() {
        return filterByRole("ADMIN");
    }

    /** Listar solo VETs que puedo ver */
    public List<T> listVets() {
        return filterByRole("VET");
    }

    /** Listar solo OWNERs que puedo ver */
    public List<T> listOwners() {
        return filterByRole("OWNER");
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


    //=============================login==============
    /**
     * Autentica a un usuario usando su login y contraseña en texto plano.
     *
     * @param login    el login del usuario
     * @param password la contraseña en texto plano
     * @return el usuario autenticado
     * @throws WebApplicationException si las credenciales son incorrectas
     */
    public T login(String login, String password) {
        // 1️⃣ Buscar usuario por login
        TypedQuery<T> query = em.createQuery(
            "SELECT u FROM " + type.getSimpleName() + " u WHERE u.login = :login",
            type
        );
        query.setParameter("login", login);
        List<T> results = query.getResultList();

        if (results.isEmpty()) {
            throw new WebApplicationException(
                "Login o contraseña incorrectos",
                Response.Status.UNAUTHORIZED
            );
        }

        T user = results.get(0);

        // 2️⃣ Calcular el MD5 de la contraseña recibida
        String hashedInput = hashMD5(password);

        // 3️⃣ Comparar con la almacenada
        if (!hashedInput.equalsIgnoreCase(user.getPassword())) {
            throw new WebApplicationException(
                "Login o contraseña incorrectos",
                Response.Status.UNAUTHORIZED
            );
        }

        // 4️⃣ Autenticación exitosa
        return user;
    }

    /**
     * Convierte un texto en su hash MD5.
     */
    private String hashMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString().toUpperCase();
        } 
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al calcular hash MD5", e);
        }
    }

}
    
