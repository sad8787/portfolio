package es.uvigo.esei.xcs.service;

import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.anyPet;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.existentPetId;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.newPet;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.newPetWithOwner;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.owners;

import javax.ejb.EJB;
import javax.ejb.EJBAccessException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.CleanupUsingScript;
import org.jboss.arquillian.persistence.ShouldMatchDataSet;
import org.jboss.arquillian.persistence.TestExecutionPhase;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import es.uvigo.esei.xcs.domain.entities.Owner;
import es.uvigo.esei.xcs.domain.entities.OwnersDataset;
import es.uvigo.esei.xcs.domain.entities.Pet;
import es.uvigo.esei.xcs.service.util.security.RoleCaller;
import es.uvigo.esei.xcs.service.util.security.TestPrincipal;

@RunWith(Arquillian.class)
@UsingDataSet("owners.xml")
@ShouldMatchDataSet("owners.xml")
@CleanupUsingScript(phase = TestExecutionPhase.BEFORE, value = { "cleanup.sql", "cleanup-autoincrement.sql" })
public class PetServiceIllegalAccessIntegrationTest {
	@Inject
	private PetService facade;
	
	@Inject
	private TestPrincipal principal;

	@EJB(beanName = "admin-caller")
	private RoleCaller asAdmin;

	@EJB(beanName = "owner-caller")
	private RoleCaller asOwner;
	
	@Deployment
	public static Archive<?> createDeployment() {
		final WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
			.addClasses(PetService.class, OwnersDataset.class)
			.addPackage(RoleCaller.class.getPackage())
			.addPackage(Pet.class.getPackage())
			.addAsResource("test-persistence.xml", "META-INF/persistence.xml")
			.addAsWebInfResource("jboss-web.xml")
            .addAsResource("arquillian.extension.persistence.properties")
            .addAsResource("arquillian.extension.persistence.dbunit.properties")
			.addAsWebInfResource("beans.xml", "beans.xml");

		return archive;
	}

	@Test(expected = EJBAccessException.class)
	public void testGetNoRole() {
		facade.get(existentPetId());
	}
	
	@Test(expected = EJBAccessException.class)
	public void testListNoRole() {
		facade.list();
	}
	
	@Test(expected = EJBAccessException.class)
	public void testCreateNoRole() {
		facade.create(newPet());
	}
	
	@Test(expected = EJBAccessException.class)
	public void testUpdateNoRole() {
		facade.update(anyPet());
	}
	
	@Test(expected = EJBAccessException.class)
	public void testRemoveNoRole() {
		facade.remove(existentPetId());
	}
	
	@Test(expected = EJBTransactionRolledbackException.class)
	public void testGetRoleAdmin() {
		asAdmin.run(this::testGetNoRole);
	}
	
	@Test(expected = EJBTransactionRolledbackException.class)
	public void testListRoleAdmin() {
		asAdmin.run(this::testListNoRole);
	}
	
	@Test(expected = EJBTransactionRolledbackException.class)
	public void testCreateRoleAdmin() {
		asAdmin.run(this::testCreateNoRole);
	}
	
	@Test(expected = EJBTransactionRolledbackException.class)
	public void testUpdateRoleAdmin() {
		asAdmin.run(this::testUpdateNoRole);
	}
	
	@Test(expected = EJBTransactionRolledbackException.class)
	public void testRemoveRoleAdmin() {
		asAdmin.run(this::testRemoveNoRole);
	}
	
	@Test(expected = EJBTransactionRolledbackException.class)
	public void testGetRoleOwner() {
		final Owner[] owners = owners();
		final Owner owner1 = owners[0];
		final Owner owner2 = owners[1];
		final Pet pet1 = owner1.getPets().iterator().next();

		principal.setName(owner2.getLogin());
		
		asOwner.run(() -> facade.get(pet1.getId()));
	}
	
	@Test(expected = EJBTransactionRolledbackException.class)
	public void testCreateRoleOwner() {
		final Owner[] owners = owners();
		final Owner owner1 = owners[0];
		final Owner owner2 = owners[1];
		final Pet pet = newPetWithOwner(owner1);

		principal.setName(owner2.getLogin());
		
		asOwner.run(() -> facade.create(pet));
	}
	
	@Test(expected = EJBTransactionRolledbackException.class)
	public void testUpdateRoleOwner() {
		final Owner[] owners = owners();
		final Owner owner1 = owners[0];
		final Owner owner2 = owners[1];
		final Pet pet1 = owner1.getPets().iterator().next();
		pet1.setName("Owner2 Pet");

		principal.setName(owner2.getLogin());
		
		asOwner.run(() -> facade.update(pet1));
	}
	
	@Test(expected = EJBTransactionRolledbackException.class)
	public void testRemoveRoleOwner() {
		final Owner[] owners = owners();
		final Owner owner1 = owners[0];
		final Owner owner2 = owners[1];
		final Pet pet1 = owner1.getPets().iterator().next();

		principal.setName(owner2.getLogin());
		
		asOwner.run(() -> facade.remove(pet1.getId()));
	}
}
