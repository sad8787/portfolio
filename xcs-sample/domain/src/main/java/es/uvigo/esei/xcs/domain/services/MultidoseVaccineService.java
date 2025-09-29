package es.uvigo.esei.xcs.domain.services;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;

import es.uvigo.esei.xcs.domain.entities.MultidoseVaccine;

@Stateless
public class MultidoseVaccineService extends VaccineService {

    // === CRUD específico ===

    public void create(MultidoseVaccine vaccine) {
        super.create(vaccine);
    }

    public MultidoseVaccine findByIdMultidose(int id) {
        return (MultidoseVaccine) super.findById(id).orElse(null);
    }

    public List<MultidoseVaccine> findAllMultidose() {
        TypedQuery<MultidoseVaccine> query = em.createQuery(
            "SELECT m FROM Vaccine m WHERE TYPE(m) = :type", MultidoseVaccine.class
        );
        query.setParameter("type", MultidoseVaccine.class);
        return query.getResultList();
    }

    public List<MultidoseVaccine> findByDoses(int doses) {
        TypedQuery<MultidoseVaccine> query = em.createQuery(
            "SELECT m FROM Vaccine m WHERE TYPE(m) = :type AND m.doses = :doses",
            MultidoseVaccine.class
        );
        query.setParameter("type", MultidoseVaccine.class);
        query.setParameter("doses", doses);
        return query.getResultList();
    }
}



