package es.uvigo.esei.xcs.domain.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An administrator of the application.
 */
@Entity
@DiscriminatorValue("ADMIN")
@XmlRootElement(name = "admin", namespace = "http://entities.domain.xcs.esei.uvigo.es")
public class Administrator extends User {
    private static final long serialVersionUID = 1L;

    // Required by JPA
    protected Administrator() {
        super("dummy@example.com", "dummy123"); // email válido para pasar validación
    }

    /**
     * Creates a new instance of {@code Administrator}.
     *
     * @param login the login (must be a valid email)
     * @param password the raw password (min length 6)
     */
    public Administrator(String login, String password) {
        super(login, password);
        // role handled automatically by @DiscriminatorValue("ADMIN")
    }
}


