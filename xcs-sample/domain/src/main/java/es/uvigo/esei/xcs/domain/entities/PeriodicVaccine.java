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

    protected PeriodicVaccine() {}

    public PeriodicVaccine(String name, PeriodicType periodicType, int periode) {
        super(name);
        this.periodicType = periodicType;
        this.periode = periode;
    }

    public PeriodicType getPeriodicType() {
        return periodicType;
    }

    public void setPeriodicType(PeriodicType periodicType) {
        this.periodicType = periodicType;
    }

    public int getPeriode() {
        return periode;
    }

    public void setPeriode(int periode) {
        this.periode = periode;
    }
}

