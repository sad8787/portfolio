package es.uvigo.esei.xcs.domain.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An administrator of the application.
 * 
 * @author 
 */
@Entity
@DiscriminatorValue("ADMIN")
@XmlRootElement(name = "admin", namespace = "http://entities.domain.xcs.esei.uvigo.es")
public class Administrator extends User {
    private static final long serialVersionUID = 1L;

    // Required by JPA
    protected Administrator() {
        super("dummy", "dummy123"); // valores por defecto para JPA
    }

    /**
     * Creates a new instance of {@code Administrator}.
     * 
     * @param login the login that identifies the user. Must be non-null, non-empty, max length 100.
     * @param password the raw password of the user. Must be non-null, min length 6.
     * 
     * @throws NullPointerException if {@code null} is passed.
     * @throws IllegalArgumentException if constraints are not met.
     */
    public Administrator(String login, String password) {
        super(login, password);
        // No need to set role manually: handled by @DiscriminatorValue("ADMIN")
    }
}

