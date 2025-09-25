package es.uvigo.esei.xcs.service;

import static es.uvigo.esei.xcs.domain.entities.IsEqualToOwner.containsOwnersInAnyOrder;
import static es.uvigo.esei.xcs.domain.entities.IsEqualToOwner.equalToOwner;
import static es.uvigo.esei.xcs.domain.entities.IsEqualToPet.containsPetsInAnyOrder;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.existentOwner;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.newOwnerWithFreshPets;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.newOwnerWithPersistentPets;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.newOwnerWithoutPets;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.newPasswordForExistentOwner;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.nonExistentLogin;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.nonExistentPetName;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.ownerWithPets;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.ownerWithoutPets;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.owners;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.ownersOf;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.petNameWithMultipleOwners;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.petNameWithSingleOwner;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.CleanupUsingScript;
import org.jboss.arquillian.persistence.ShouldMatchDataSet;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import es.uvigo.esei.xcs.domain.entities.Owner;
import es.uvigo.esei.xcs.domain.entities.OwnersDataset;
import es.uvigo.esei.xcs.domain.entities.Pet;
import es.uvigo.esei.xcs.service.util.security.RoleCaller;

@RunWith(Arquillian.class)
@UsingDataSet("owners.xml")
@CleanupUsingScript({ "cleanup.sql", "cleanup-autoincrement.sql" })
public class OwnerServiceIntegrationTest {
	@Inject
	private OwnerService facade;
	
	@EJB(beanName = "admin-caller")
	private RoleCaller asAdmin;
	
	@Deployment
	public static Archive<?> createDeployment() {
		return ShrinkWrap.create(WebArchive.class, "test.war")
			.addClasses(OwnerService.class, OwnersDataset.class)
			.addPackage(RoleCaller.class.getPackage())
			.addPackage(Owner.class.getPackage())
			.addAsResource("test-persistence.xml", "META-INF/persistence.xml")
			.addAsWebInfResource("jboss-web.xml")
            .addAsResource("arquillian.extension.persistence.properties")
            .addAsResource("arquillian.extension.persistence.dbunit.properties")
			.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@Test
	@ShouldMatchDataSet("owners.xml")
	public void testGetOwner() {
		final Owner existentOwner = existentOwner();
		
		final Owner actualOwner = asAdmin.call(() -> facade.get(existentOwner.getLogin()));
		
		assertThat(actualOwner, is(equalToOwner(existentOwner)));
	}

	@Test
	@ShouldMatchDataSet("owners.xml")
	public void testGetOwnerNonExistent() {
		final Owner actualOwner = asAdmin.call(() -> facade.get(nonExistentLogin()));
		
		assertThat(actualOwner, is(nullValue()));
	}

	@Test(expected = EJBTransactionRolledbackException.class)
	@ShouldMatchDataSet("owners.xml")
	public void testGetOwnerNull() {
		asAdmin.call(() -> facade.get(null));
	}

	@Test
	@ShouldMatchDataSet("owners.xml")
	public void testList() {
		final List<Owner> actualOwners = asAdmin.call(() -> facade.list());
		
		assertThat(actualOwners, containsOwnersInAnyOrder(owners()));
	}

	@Test
	@ShouldMatchDataSet("owners.xml")
	public void testFindByPetName() {
		final String petName = petNameWithSingleOwner();
		final Owner owner = ownersOf(petName)[0];
		
		final List<Owner> actualOwners = asAdmin.call(() -> facade.findByPetName(petName));
		
		assertThat(actualOwners, hasSize(1));
		assertThat(actualOwners.get(0), is(equalToOwner(owner)));
	}

	@Test
	@ShouldMatchDataSet("owners.xml")
	public void testFindByPetNameMultipleOwners() {
		final String petName = petNameWithMultipleOwners();
		
		final List<Owner> actualOwners = asAdmin.call(() -> facade.findByPetName(petName));
		
		final Owner[] expectedOwners = ownersOf(petName);
		
		assertThat(actualOwners, containsOwnersInAnyOrder(expectedOwners));
	}

	@Test
	@ShouldMatchDataSet("owners.xml")
	public void testFindByPetNameNoPet() {
		final String nonExistentPet = nonExistentPetName();
		
		final List<Owner> actualOwners = asAdmin.call(() -> facade.findByPetName(nonExistentPet));
		
		assertThat(actualOwners, is(empty()));
	}

	@Test(expected = EJBTransactionRolledbackException.class)
	@ShouldMatchDataSet("owners.xml")
	public void testFindByPetNameNull() {
		asAdmin.run(() -> facade.findByPetName(null));
	}

	@Test
	@ShouldMatchDataSet({"owners.xml", "owners-create-without-pets.xml"})
	public void testCreateWithoutPets() {
		final Owner newOwner = newOwnerWithoutPets();
		
		final Owner actual = asAdmin.call(() -> facade.create(newOwner));
		
		assertThat(actual, is(equalToOwner(newOwner)));
	}

	@Test
	@ShouldMatchDataSet({"owners.xml", "owners-create-with-pets.xml"})
	public void testCreateWithPets() {
		final Owner actualOwner = asAdmin.call(() -> facade.create(newOwnerWithFreshPets()));
		
		assertThat(actualOwner, is(equalToOwner(newOwnerWithPersistentPets())));
	}

	@Test(expected = EJBTransactionRolledbackException.class)
	@ShouldMatchDataSet("owners.xml")
	public void testCreateExistentLogin() {
		asAdmin.run(() -> facade.create(existentOwner()));
	}

	@Test(expected = EJBTransactionRolledbackException.class)
	@ShouldMatchDataSet("owners.xml")
	public void testCreateNull() {
		asAdmin.call(() -> facade.create(null));
	}

	@Test(expected = EJBTransactionRolledbackException.class)
	@ShouldMatchDataSet("owners.xml")
	public void testUpdateNull() {
		asAdmin.run(() -> facade.update(null));
	}

	@Test
	@ShouldMatchDataSet("owners-update-password.xml")
	public void testUpdatePassword() {
		final Owner existentOwner = existentOwner();
		existentOwner.changePassword(newPasswordForExistentOwner());
		
		asAdmin.run(() -> facade.update(existentOwner));
	}

	@Test
	@ShouldMatchDataSet({"owners.xml", "owners-create-without-pets.xml"})
	public void testUpdateNewOwnerWithoutPets() {
		final Owner newOwner = newOwnerWithoutPets();
		
		final Owner actualOwner = asAdmin.call(() -> facade.update(newOwner));
		
		assertThat(actualOwner, is(equalToOwner(newOwner)));
	}

	@Test
	@ShouldMatchDataSet({"owners.xml", "owners-create-with-pets.xml"})
	public void testUpdateNewOwnerWithPets() {
		final Owner actualOwner = asAdmin.call(() -> facade.update(newOwnerWithFreshPets()));
		
		assertThat(actualOwner, is(equalToOwner(newOwnerWithPersistentPets())));
	}

	@Test
	@ShouldMatchDataSet("owners-remove-without-pets.xml")
	public void testRemoveWithoutPets() {
		asAdmin.run(() -> facade.remove(ownerWithoutPets().getLogin()));
	}

	@Test
	@ShouldMatchDataSet("owners-remove-with-pets.xml")
	public void testRemoveWithPets() {
		asAdmin.run(() -> facade.remove(ownerWithPets().getLogin()));
	}

	@Test(expected = EJBTransactionRolledbackException.class)
	@ShouldMatchDataSet("owners.xml")
	public void testRemoveNonExistentOwner() {
		asAdmin.run(() -> facade.remove(nonExistentLogin()));
	}

	@Test(expected = EJBTransactionRolledbackException.class)
	@ShouldMatchDataSet("owners.xml")
	public void testRemoveNull() {
		asAdmin.run(() -> facade.remove(null));
	}

	@Test
	@ShouldMatchDataSet("owners.xml")
	public void testGetPets() {
		final Owner owner = ownerWithPets();
		final Collection<Pet> ownedPets = owner.getPets();
		
		final List<Pet> actualPets = asAdmin.call(() -> facade.getPets(owner.getLogin()));
		
		assertThat(actualPets, containsPetsInAnyOrder(ownedPets));
	}

	@Test
	@ShouldMatchDataSet("owners.xml")
	public void testGetPetsNoPets() {
		final List<Pet> actualPets = asAdmin.call(() -> facade.getPets(ownerWithoutPets().getLogin()));
		
		assertThat(actualPets, is(empty()));
	}

	@Test(expected = EJBTransactionRolledbackException.class)
	@ShouldMatchDataSet("owners.xml")
	public void testGetPetsNonExistentOwner() {
		asAdmin.call(() -> facade.getPets(nonExistentLogin()));
	}

	@Test(expected = EJBTransactionRolledbackException.class)
	@ShouldMatchDataSet("owners.xml")
	public void testGetPetsNull() {
		asAdmin.call(() -> facade.getPets(null));
	}
}
