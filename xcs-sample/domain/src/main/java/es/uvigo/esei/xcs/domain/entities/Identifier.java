package es.uvigo.esei.xcs.domain.entities;

import java.io.Serializable;
import static java.util.Objects.requireNonNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import static org.apache.commons.lang3.Validate.inclusiveBetween;

/**
 * An identifier for a pet.
 * Each identifier has a type (e.g., microchip, tattoo, passport) 
 * and a value.
 * 
 * @author Sadiel Godales
 */


@Entity(name = "Identifier")
@XmlRootElement(name = "identifier", namespace = "http://entities.domain.xcs.esei.uvigo.es")
@XmlAccessorType(XmlAccessType.FIELD)

public class Identifier implements Serializable {
    private static final long serialVersionUID = 1L; 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 50, nullable = false)
    private String type;

    @Column(length = 100, nullable = false, unique = true)
    private String value;

    @ManyToOne
    @JoinColumn(name = "pet_id", referencedColumnName = "id", nullable = false)
    @XmlTransient
    private Pet pet;

    // Required for JPA
    protected Identifier() {}

    /**
     * Creates a new instance of {@code Identifier} without a pet.
     * 
     * @param type  the type of identifier (e.g., microchip). 
     *              Must be non-null, length 1–50.
     * @param value the value of the identifier. 
     *              Must be non-null, length 1–100.
     */    

    public Identifier(String type, String value) {
        requireNonNull(type, "type can't be null");
        inclusiveBetween(1, 50, type.length(), "type must have length between 1 and 50");

        requireNonNull(value, "value can't be null");
        inclusiveBetween(1, 100, value.length(), "value must have length between 1 and 100");

        this.type = type;
        this.value = value;
    }

    public static Identifier create(String type, String value, Pet pet) {
        Identifier identifier = new Identifier(type, value);
        identifier.setPet(pet);
        return identifier;
    }



    // getters y setters
    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        requireNonNull(type, "type can't be null");
        inclusiveBetween(1, 50, type.length(), "type must have length between 1 and 50");
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        requireNonNull(value, "value can't be null");
        inclusiveBetween(1, 100, value.length(), "value must have length between 1 and 100");
        this.value = value;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        if (this.pet != null) {
            this.pet.internalRemoveIdentifier(this);
        }

        this.pet = pet;

        if (this.pet != null) {
            this.pet.internalAddIdentifier(this);
        }
    }

}

