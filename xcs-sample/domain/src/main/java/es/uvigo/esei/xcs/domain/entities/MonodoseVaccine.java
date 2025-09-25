@Entity
@DiscriminatorValue("MONODOSE")
public class MonodoseVaccine extends Vaccine {
    private static final long serialVersionUID = 1L;

    protected MonodoseVaccine() {}

    public MonodoseVaccine(String name) {
        super(name);
    }
}

