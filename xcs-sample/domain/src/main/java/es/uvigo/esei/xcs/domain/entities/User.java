package es.uvigo.esei.xcs.domain.entities;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import static java.util.Objects.requireNonNull;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import static org.apache.commons.lang3.Validate.inclusiveBetween;
import static org.apache.commons.lang3.Validate.matchesPattern;

@Entity
@Inheritance
@DiscriminatorColumn(
    name = "role",
    discriminatorType = DiscriminatorType.STRING,
    length = 5
)
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 100, nullable = false)
    protected String login;

    @Column(length = 32, nullable = false)
    protected String password;

    @Column(name = "role", insertable = false, updatable = false)
    protected String role;

    // ===== Constructor por defecto =====
    protected User() {}

    /**
     * Crea un nuevo usuario con validación de email y contraseña.
     */
    public User(String login, String password) {
        this.setLogin(login);
        this.changePassword(password);
    }

    // ===== LOGIN =====

    /**
     * Devuelve el login del usuario (email).
     */
    public String getLogin() {
        return login;
    }

    /**
     * Establece el login, validando que sea un email válido.
     */
    public void setLogin(String login) {
        requireNonNull(login, "login can't be null");
        inclusiveBetween(1, 100, login.length(), "login must have a length between 1 and 100");

        if (!isValidEmail(login)) {
            throw new IllegalArgumentException("login must be a valid email address");
        }

        this.login = login.toLowerCase(); // normaliza a minúsculas
    }

    // ===== ROLE =====

    public String getRole() {
        return role;
    }

    // ===== PASSWORD =====

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        requireNonNull(password, "password can't be null");
        matchesPattern(password, "[A-Fa-f0-9]{32}", "password must be a valid MD5 string");
        this.password = password.toUpperCase();
    }

    public void changePassword(String password) {
        requireNonNull(password, "password can't be null");
        if (password.length() < 6)
            throw new IllegalArgumentException("password can't be shorter than 6");

        try {
            final MessageDigest digester = MessageDigest.getInstance("MD5");
            final HexBinaryAdapter adapter = new HexBinaryAdapter();
            this.password = adapter.marshal(digester.digest(password.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    // ===== EMAIL VALIDATION =====

    /**
     * Verifica si el texto pasado es un email válido.
     */
    private boolean isValidEmail(String email) {
        // Expresión regular compatible con RFC 5322 simplificada y robusta
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
}
