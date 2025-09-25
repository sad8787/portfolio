package es.uvigo.esei.xcs.service;

import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.anyOwner;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.existentLogin;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.existentPetName;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.newOwnerWithoutPets;

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
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import es.uvigo.esei.xcs.domain.entities.Owner;
import es.uvigo.esei.xcs.domain.entities.OwnersDataset;
import es.uvigo.esei.xcs.service.util.security.RoleCaller;

@RunWith(Arquillian.class)
@UsingDataSet("owners.xml")
@ShouldMatchDataSet("owners.xml")
@CleanupUsingScript(phase = TestExecutionPhase.BEFORE, value = { "cleanup.sql", "cleanup-autoincrement.sql" })
public class OwnerServiceIllegalAccessIntegrationTest {
	@Inject
	private OwnerService facade;

	@EJB(beanName = "owner-caller")
	private RoleCaller asOwner;
	
	@Deployment
	public static Archive<?> createDeployment() {
		final WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
			.addClasses(OwnerService.class, OwnersDataset.class)
			.addPackage(RoleCaller.class.getPackage())
			.addPackage(Owner.class.getPackage())
			.addAsResource("test-persistence.xml", "META-INF/persistence.xml")
			.addAsWebInfResource("jboss-web.xml")
            .addAsResource("arquillian.extension.persistence.properties")
            .addAsResource("arquillian.extension.persistence.dbunit.properties")
			.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
		
		return archive;
	}

	@Test(expected = EJBAccessException.class)
	public void testGetNoRole() {
		facade.get(existentLogin());
	}

	@Test(expected = EJBAccessException.class)
	public void testListNoRole() {
		facade.list();
	}

	@Test(expected = EJBAccessException.class)
	public void testFindByPetNameNoRole() {
		facade.findByPetName(existentPetName());
	}

	@Test(expected = EJBAccessException.class)
	public void testCreateNoRole() {
		facade.create(newOwnerWithoutPets());
	}

	@Test(expected = EJBAccessException.class)
	public void testUpdateNoRole() {
		facade.update(anyOwner());
	}

	@Test(expected = EJBAccessException.class)
	public void testRemoveNoRole() {
		facade.remove(existentLogin());
	}

	@Test(expected = EJBAccessException.class)
	public void testGetPetsNoRole() {
		facade.getPets(existentLogin());
	}

	@Test(expected = EJBTransactionRolledbackException.class)
	public void testGetRoleOwner() {
		asOwner.run(this::testGetNoRole);
	}

	@Test(expected = EJBTransactionRolledbackException.class)
	public void testListRoleOwner() {
		asOwner.run(this::testListNoRole);
	}

	@Test(expected = EJBTransactionRolledbackException.class)
	public void testFindByPetNameRoleOwner() {
		asOwner.run(this::testFindByPetNameNoRole);
	}

	@Test(expected = EJBTransactionRolledbackException.class)
	public void testCreateRoleOwner() {
		asOwner.run(this::testCreateNoRole);
	}

	@Test(expected = EJBTransactionRolledbackException.class)
	public void testUpdateRoleOwner() {
		asOwner.run(this::testUpdateNoRole);
	}

	@Test(expected = EJBTransactionRolledbackException.class)
	public void testRemoveRoleOwner() {
		asOwner.run(this::testRemoveNoRole);
	}

	@Test(expected = EJBTransactionRolledbackException.class)
	public void testGetPetsRoleOwner() {
		asOwner.run(this::testGetPetsNoRole);
	}
}
