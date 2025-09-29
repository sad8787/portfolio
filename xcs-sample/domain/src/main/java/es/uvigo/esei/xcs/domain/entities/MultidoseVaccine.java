package es.uvigo.esei.xcs.domain.entities;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("MULTIDOSE")
public class MultidoseVaccine extends Vaccine {

    private static final long serialVersionUID = 1L;

    @Column(nullable = false)
    private int doses;

    // Constructor requerido por JPA
    protected MultidoseVaccine() {
        super(); // Llama al constructor por defecto de Vaccine
    }

    // Constructor público
    public MultidoseVaccine(String name, int doses) {
        super(name); // Inicializa el nombre en la superclase
        if (doses <= 0) {
            throw new IllegalArgumentException("doses must be positive");
        }
        this.doses = doses;
    }

    public int getDoses() {
        return doses;
    }

    public void setDoses(int doses) {
        if (doses <= 0) {
            throw new IllegalArgumentException("doses must be positive");
        }
        this.doses = doses;
    }
}


