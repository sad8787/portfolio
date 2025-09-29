package es.uvigo.esei.xcs.domain.entities;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
@DiscriminatorValue("PERIODIC")
public class PeriodicVaccine extends Vaccine {

    private static final long serialVersionUID = 1L;

    public enum PeriodicType {
        DAYS, MONTHS, YEARS
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PeriodicType periodicType;

    @Column(nullable = false)
    private int periode;

    // Constructor requerido por JPA
    protected PeriodicVaccine() {
        super(); // llama al constructor por defecto de Vaccine
    }

    // Constructor público
    public PeriodicVaccine(String name, PeriodicType periodicType, int periode) {
        super(name); // inicializa el nombre en la superclase
        if (periodicType == null) {
            throw new IllegalArgumentException("periodicType cannot be null");
        }
        if (periode <= 0) {
            throw new IllegalArgumentException("periode must be positive");
        }
        this.periodicType = periodicType;
        this.periode = periode;
    }

    public PeriodicType getPeriodicType() {
        return periodicType;
    }

    public void setPeriodicType(PeriodicType periodicType) {
        if (periodicType == null) {
            throw new IllegalArgumentException("periodicType cannot be null");
        }
        this.periodicType = periodicType;
    }

    public int getPeriode() {
        return periode;
    }

    public void setPeriode(int periode) {
        if (periode <= 0) {
            throw new IllegalArgumentException("periode must be positive");
        }
        this.periode = periode;
    }
}


