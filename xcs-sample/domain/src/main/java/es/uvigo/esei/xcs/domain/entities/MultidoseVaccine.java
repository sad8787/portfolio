@Entity
@DiscriminatorValue("MULTIDOSE")
public class MultidoseVaccine extends Vaccine {
    private static final long serialVersionUID = 1L;

    @Column(nullable = false)
    private int doses;

    protected MultidoseVaccine() {}

    public MultidoseVaccine(String name, int doses) {
        super(name);
        this.doses = doses;
    }

    public int getDoses() {
        return doses;
    }

    public void setDoses(int doses) {
        this.doses = doses;
    }
}

