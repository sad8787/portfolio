package es.uvigo.esei.xcs.domain.services;

import es.uvigo.esei.xcs.domain.entities.MonodoseVaccine;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import java.util.List;

@Stateless
public class MonodoseVaccineService extends VaccineService {

    public void create(MonodoseVaccine vaccine) {
        super.create(vaccine);
    }

    /** Método específico para obtener MonodoseVaccine por id */
    public MonodoseVaccine findByIdMonodose(int id) {
        return (MonodoseVaccine) super.findById(id).orElse(null);
    }

    public List<MonodoseVaccine> findAllMonodose() {
        TypedQuery<MonodoseVaccine> query = em.createQuery(
            "SELECT m FROM Vaccine m WHERE TYPE(m) = :type", MonodoseVaccine.class
        );
        query.setParameter("type", MonodoseVaccine.class);
        return query.getResultList();
    }
}


