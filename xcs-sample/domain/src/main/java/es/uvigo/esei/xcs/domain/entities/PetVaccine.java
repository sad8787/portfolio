package es.uvigo.esei.xcs.domain.entities;

import java.io.Serializable;
import java.util.Date;
import static java.util.Objects.requireNonNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@Entity(name = "PetVaccine")
@Table(name = "PET_VACCINE")
@XmlRootElement(name = "petVaccine", namespace = "http://entities.domain.xcs.esei.uvigo.es")
@XmlAccessorType(XmlAccessType.FIELD)
public class PetVaccine implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    @XmlTransient
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vaccine_id", nullable = false)
    private Vaccine vaccine;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date date;

    protected PetVaccine() {}

    public PetVaccine(Pet pet, Vaccine vaccine, Date date) {
        requireNonNull(pet, "pet can't be null");
        requireNonNull(vaccine, "vaccine can't be null");
        requireNonNull(date, "date can't be null");

        this.pet = pet;
        this.vaccine = vaccine;
        this.date = date;
        
        pet.internalAddPetVaccine(this);
        vaccine.internalAddPetVaccine(this);
    }



    public int getId() { return id; }

    public Pet getPet() { return pet; }
    public void setPet(Pet pet) {
        requireNonNull(pet, "pet can't be null");
        this.pet = pet;
    }

    public Vaccine getVaccine() { return vaccine; }
    public void setVaccine(Vaccine vaccine) {
        requireNonNull(vaccine, "vaccine can't be null");
        this.vaccine = vaccine;
    }

    public Date getDate() { return date; }
    public void setDate(Date date) {
        requireNonNull(date, "date can't be null");
        this.date = date;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PetVaccine)) return false;
        PetVaccine that = (PetVaccine) o;
        return id != 0 && id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

}

