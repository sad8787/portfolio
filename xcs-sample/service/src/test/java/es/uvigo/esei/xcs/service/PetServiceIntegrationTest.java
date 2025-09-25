package es.uvigo.esei.xcs.service;

import static es.uvigo.esei.xcs.domain.entities.IsEqualToPet.containsPetsInAnyOrder;
import static es.uvigo.esei.xcs.domain.entities.IsEqualToPet.equalToPet;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.anyPetOf;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.existentPet;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.existentPetId;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.newPet;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.newPetWithOwner;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.nonExistentPetId;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.ownerWithPets;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.ownerWithoutPets;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.petWithId;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.inject.Inject;
import javax.security.auth.login.LoginException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.CleanupUsingScript;
import org.jboss.arquillian.persistence.ShouldMatchDataSet;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import es.uvigo.esei.xcs.domain.entities.AnimalType;
import es.uvigo.esei.xcs.domain.entities.Owner;
import es.uvigo.esei.xcs.domain.entities.OwnersDataset;
import es.uvigo.esei.xcs.domain.entities.Pet;
import es.uvigo.esei.xcs.service.util.security.RoleCaller;
import es.uvigo.esei.xcs.service.util.security.TestPrincipal;

@RunWith(Arquillian.class)
@UsingDataSet("owners.xml")
@CleanupUsingScript({ "cleanup.sql", "cleanup-autoincrement.sql" })
public class PetServiceIntegrationTest {
	@Inject
	private PetService facade;

	@EJB(beanName = "owner-caller")
	private RoleCaller asOwner;
	
	@Inject
	private TestPrincipal principal;
	
	@Deployment
	public static Archive<?> createDeployment() {
		return ShrinkWrap.create(WebArchive.class, "test.war")
			.addClasses(PetService.class, OwnersDataset.class)
			.addPackage(RoleCaller.class.getPackage())
			.addPackage(Pet.class.getPackage())
			.addAsResource("test-persistence.xml", "META-INF/persistence.xml")
			.addAsWebInfResource("jboss-web.xml")
      .addAsResource("arquillian.extension.persistence.properties")
      .addAsResource("arquillian.extension.persistence.dbunit.properties")
			.addAsWebInfResource("beans.xml", "beans.xml");
	}
	
	@Test
	@ShouldMatchDataSet("owners.xml")
	public void testGet() throws LoginException {
		final Pet existentPet = existentPet();
		
		principal.setName(existentPet.getOwner().getLogin());
		
		final Pet actualPet = asOwner.call(() -> facade.get(existentPet.getId()));
		
		assertThat(actualPet, equalToPet(existentPet));
	}

	@Test
	@ShouldMatchDataSet("owners.xml")
	public void testGetBadId() throws LoginException {
		principal.setName(ownerWithoutPets().getLogin());
		
		final Pet actual = asOwner.call(() -> facade.get(nonExistentPetId()));
		
		assertThat(actual, is(nullValue()));
	}

	@Test(expected = EJBTransactionRolledbackException.class)
	@ShouldMatchDataSet("owners.xml")
	public void testGetOthersPetId() throws LoginException {
		final Owner ownerWithoutPets = ownerWithoutPets();
		final int petId = anyPetOf(ownerWithPets()).getId();
		
		principal.setName(ownerWithoutPets.getLogin());
		
		asOwner.run(() -> facade.get(petId));
	}

	@Test
	@ShouldMatchDataSet("owners.xml")
	public void testList() throws LoginException {
		final Owner ownerWithPets = ownerWithPets();
		
		principal.setName(ownerWithPets.getLogin());
		
		final List<Pet> actualPets = asOwner.call(() -> facade.list());
		
		assertThat(actualPets, containsPetsInAnyOrder(ownerWithPets.getPets()));
	}

	@Test
	@ShouldMatchDataSet("owners.xml")
	public void testListNoPets() throws LoginException {
		principal.setName(ownerWithoutPets().getLogin());
		
		final List<Pet> pets = asOwner.call(() -> facade.list());
		
		assertThat(pets, is(empty()));
	}

	@Test
	@ShouldMatchDataSet({ "owners.xml", "owners-create-pet.xml" })
	public void testCreate() {
		final Owner ownerWithoutPets = ownerWithoutPets();
		
		principal.setName(ownerWithoutPets.getLogin());
		
		final Pet pet = newPetWithOwner(ownerWithoutPets);
		
		asOwner.call(() -> facade.create(pet));
	}

	@Test
	@ShouldMatchDataSet({ "owners.xml", "owners-create-pet.xml" })
	public void testCreateNullOwner() {
		principal.setName(ownerWithoutPets().getLogin());
		
		final Pet pet = newPet();
		
		asOwner.run(() -> facade.create(pet));
	}
	
	@Test(expected = EJBTransactionRolledbackException.class)
	@ShouldMatchDataSet({ "owners.xml" })
	public void testCreateNullPet() {
		principal.setName(ownerWithoutPets().getLogin());
		
		asOwner.run(() -> facade.create(null));
	}
	
	@Test(expected = EJBTransactionRolledbackException.class)
	@ShouldMatchDataSet({ "owners.xml" })
	public void testCreateWrongOwner() {
		principal.setName(ownerWithoutPets().getLogin());
		
		final Pet pet = newPetWithOwner(ownerWithPets());
		
		asOwner.run(() -> facade.create(pet));
	}
	
	@Test
	@ShouldMatchDataSet("owners-update-pet.xml")
	public void testUpdate() throws LoginException {
		final Pet existentPet = existentPet();
		
		principal.setName(existentPet.getOwner().getLogin());
		
		existentPet.setName("UpdateName");
		existentPet.setAnimal(AnimalType.BIRD);
		existentPet.setBirth(new Date(946771261000L));
		
		asOwner.run(() -> facade.update(existentPet));
	}

	@Test
	@ShouldMatchDataSet({ "owners.xml", "owners-create-pet.xml" })
	public void testUpdateNewPetWithOwner() {
		final Owner ownerWithoutPets = ownerWithoutPets();
		
		principal.setName(ownerWithoutPets.getLogin());
		
		final Pet pet = newPetWithOwner(ownerWithoutPets);
		
		asOwner.call(() -> facade.update(pet));
	}
	
	@Test(expected = EJBTransactionRolledbackException.class)
	@ShouldMatchDataSet("owners.xml")
	public void testUpdateNull() throws LoginException {
		asOwner.run(() -> facade.update(null));
	}
	
	@Test(expected = EJBTransactionRolledbackException.class)
	@ShouldMatchDataSet({ "owners.xml" })
	public void testUpdateWrongOwner() {
		principal.setName(ownerWithoutPets().getLogin());
		
		final Pet pet = anyPetOf(ownerWithPets());
		
		asOwner.run(() -> facade.update(pet));
	}

	@Test(expected = EJBTransactionRolledbackException.class)
	@ShouldMatchDataSet({ "owners.xml", "owners-create-pet.xml" })
	public void testUpdatePetNoOwner() {
		final int id = existentPetId();
		final Pet pet = petWithId(id);
		
		principal.setName(pet.getOwner().getLogin());
		pet.setOwner(null);
		
		
		asOwner.run(() -> facade.update(pet));
	}

	@Test
	@ShouldMatchDataSet("owners-remove-pet.xml")
	public void testRemove() throws LoginException {
		final Pet existentPet = existentPet();
		
		principal.setName(existentPet.getOwner().getLogin());
		
		asOwner.run(() -> facade.remove(existentPet.getId()));
	}

	@Test(expected = EJBTransactionRolledbackException.class)
	@ShouldMatchDataSet("owners.xml")
	public void testRemoveBadId() throws LoginException {
		principal.setName(ownerWithoutPets().getLogin());
		
		asOwner.run(() -> facade.remove(nonExistentPetId()));
	}

	@Test(expected = EJBTransactionRolledbackException.class)
	@ShouldMatchDataSet("owners.xml")
	public void testRemoveOthersPetId() throws LoginException {
		final Owner ownerWithoutPets = ownerWithoutPets();
		final int petId = anyPetOf(ownerWithPets()).getId();
		
		principal.setName(ownerWithoutPets.getLogin());
		
		asOwner.run(() -> facade.remove(petId));
	}
}
