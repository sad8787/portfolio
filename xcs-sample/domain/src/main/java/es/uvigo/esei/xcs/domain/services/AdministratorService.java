package es.uvigo.esei.xcs.domain.services;

import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;

import es.uvigo.esei.xcs.domain.entities.Administrator;

@Stateless
public class AdministratorService extends UserService<Administrator> {

    public AdministratorService() {
        super(Administrator.class);
    }

    // Métodos específicos para Administrador (si los necesitas)
    public Optional<Administrator> findAdministratorByLogin(String login) {
        return super.findByLogin(login);
    }

    public List<Administrator> findAllAdministrators() {
        return super.findAll();
    }
}



