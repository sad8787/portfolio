package es.uvigo.esei.xcs.domain.services;

import javax.ejb.Stateless;

import es.uvigo.esei.xcs.domain.entities.Administrator;

@Stateless
public class AdministratorService extends UserService<Administrator> {
    public AdministratorService() { super(Administrator.class); }
}



