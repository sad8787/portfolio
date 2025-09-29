package es.uvigo.esei.xcs.domain.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("MONODOSE")
public class MonodoseVaccine extends Vaccine {

    private static final long serialVersionUID = 1L;

    // Constructor requerido por JPA
    protected MonodoseVaccine() {
        super(); // Llama al constructor por defecto de Vaccine
    }

    // Constructor público para crear la vacuna con nombre
    public MonodoseVaccine(String name) {
        super(name); // Llama al constructor de Vaccine que inicializa name directamente
    }
}


