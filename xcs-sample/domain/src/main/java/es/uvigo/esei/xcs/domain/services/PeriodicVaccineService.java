package es.uvigo.esei.xcs.domain.services;

import es.uvigo.esei.xcs.domain.entities.PeriodicVaccine;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import java.util.List;

@Stateless
public class PeriodicVaccineService extends VaccineService {

    public PeriodicVaccineService() {
        super(PeriodicVaccine.class);
    }

    /** Crear una nueva vacuna periódica */
    public void create(PeriodicVaccine vaccine) {
        super.create(vaccine);
    }

    /** Buscar todas las vacunas periódicas */
    @SuppressWarnings("unchecked")
    public List<PeriodicVaccine> findAllPeriodic() {
        TypedQuery<PeriodicVaccine> query = em.createQuery(
            "SELECT p FROM Vaccine p WHERE TYPE(p) = :type", PeriodicVaccine.class
        );
        query.setParameter("type", PeriodicVaccine.class);
        return query.getResultList();
    }

    /** Buscar vacunas periódicas por tipo (DAYS, MONTHS, YEARS) */
    public List<PeriodicVaccine> findByPeriodicType(PeriodicVaccine.PeriodicType type) {
        TypedQuery<PeriodicVaccine> query = em.createQuery(
            "SELECT p FROM Vaccine p WHERE TYPE(p) = :clazz AND p.periodicType = :type",
            PeriodicVaccine.class
        );
        query.setParameter("clazz", PeriodicVaccine.class);
        query.setParameter("type", type);
        return query.getResultList();
    }

    /** Buscar vacunas periódicas por periodo (ej: todas las de 12 meses) */
    public List<PeriodicVaccine> findByPeriode(int periode) {
        TypedQuery<PeriodicVaccine> query = em.createQuery(
            "SELECT p FROM Vaccine p WHERE TYPE(p) = :clazz AND p.periode = :periode",
            PeriodicVaccine.class
        );
        query.setParameter("clazz", PeriodicVaccine.class);
        query.setParameter("periode", periode);
        return query.getResultList();
    }
}

