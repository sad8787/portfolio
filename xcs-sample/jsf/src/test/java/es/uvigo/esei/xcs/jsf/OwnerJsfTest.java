package es.uvigo.esei.xcs.jsf;

import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.existentLogin;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.existentOwner;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.newOwnerLogin;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.newOwnerPassword;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.newOwnerWithoutPets;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.ownerWithPets;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.ownerWithoutPets;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.owners;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.ownersAnd;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.ownersWithout;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.InitialPage;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.persistence.Cleanup;
import org.jboss.arquillian.persistence.CleanupUsingScript;
import org.jboss.arquillian.persistence.ShouldMatchDataSet;
import org.jboss.arquillian.persistence.TestExecutionPhase;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import es.uvigo.esei.xcs.domain.entities.Owner;
import es.uvigo.esei.xcs.jsf.pages.LoginPage;
import es.uvigo.esei.xcs.jsf.pages.OwnersPage;
import es.uvigo.esei.xcs.service.OwnerService;

@RunWith(Arquillian.class)
public class OwnerJsfTest {
	private static final Path WEBAPP = Paths.get("src/main/webapp");

	@Drone
	private WebDriver browser;
	
	@Page
	private OwnersPage ownersPage;
	
	@Deployment
	public static Archive<?> createDeployment() {
		return ShrinkWrap.create(WebArchive.class, "test.war")
			.addAsLibraries(Maven.resolver()
				.loadPomFromFile("pom.xml")
				.importRuntimeAndTestDependencies()
				.resolve().withTransitivity()
				.asFile()
			)
			.addPackage(LoginManagedBean.class.getPackage())
			.addPackage(OwnerService.class.getPackage())
			.addPackage(Owner.class.getPackage())
            .addPackage(LoginPage.class.getPackage())
			.addAsWebResource(WEBAPP.resolve("index.xhtml").toFile())
            .addAsWebResource(WEBAPP.resolve("admin/owners.xhtml").toFile(), "admin/owners.xhtml")
			.addAsResource("test-persistence.xml", "META-INF/persistence.xml")
			.addAsWebInfResource(WEBAPP.resolve("WEB-INF/template.xhtml").toFile())
			.addAsWebInfResource(WEBAPP.resolve("WEB-INF/web.xml").toFile())
			.addAsWebInfResource(WEBAPP.resolve("WEB-INF/jboss-web.xml").toFile())
            .addAsResource("arquillian.extension.persistence.properties")
            .addAsResource("arquillian.extension.persistence.dbunit.properties")
			.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}
	
	@Before
	public void setUp() {
		if (this.browser != null)
			this.browser.manage().deleteAllCookies();
	}

	@Test @InSequence(1)
	@UsingDataSet("owners.xml")
	@Cleanup(phase = TestExecutionPhase.NONE)
	public void beforeLoginFail() {}
	
	@Test @InSequence(2)
	@RunAsClient
	public void testLoginFail(@InitialPage LoginPage loginPage) {
		loginPage.login("bad", "userpass");
		
		loginPage.assertOnLoginPage();
		
		loginAsAdmin(loginPage);
	}
	
	@Test @InSequence(3)
	@ShouldMatchDataSet("owners.xml")
	@CleanupUsingScript({ "cleanup.sql", "cleanup-autoincrement.sql" })
	public void afterLoginFail() {}

	
	
	@Test @InSequence(10)
	@UsingDataSet("owners.xml")
	@Cleanup(phase = TestExecutionPhase.NONE)
	public void beforeList() {}
	
	@Test @InSequence(11)
	@RunAsClient
	public void testList(@InitialPage LoginPage loginPage) {
		loginAsAdmin(loginPage);
		
		assertThat(ownersPage.areOwnersInTable(owners()), is(true));
	}
	
	@Test @InSequence(12)
	@ShouldMatchDataSet("owners.xml")
	@CleanupUsingScript({ "cleanup.sql", "cleanup-autoincrement.sql" })
	public void afterGet() {}

	
	
	@Test @InSequence(21)
	@UsingDataSet("owners.xml")
	@Cleanup(phase = TestExecutionPhase.NONE)
	public void beforeCreate() {}
	
	@Test @InSequence(22)
	@RunAsClient
	public void testCreate(@InitialPage LoginPage loginPage) {
		loginAsAdmin(loginPage);

		ownersPage.createOwner(newOwnerLogin(), newOwnerPassword());
		
		ownersPage.assertOnOwnersPage();
		
		final Owner[] expectedOwners = ownersAnd(newOwnerWithoutPets());
		
		assertThat(ownersPage.areOwnersInTable(expectedOwners), is(true));
	}
	
	@Test @InSequence(23)
	@ShouldMatchDataSet({ "owners.xml", "owners-create-without-pets.xml" })
	@CleanupUsingScript({ "cleanup.sql", "cleanup-autoincrement.sql" })
	public void afterCreate() {}

	
	
	@Test @InSequence(24)
	@UsingDataSet("owners.xml")
	@Cleanup(phase = TestExecutionPhase.NONE)
	public void beforeCreateExistingOwner() {}
	
	@Test @InSequence(25)
	@RunAsClient
	public void testCreateExistingOwner(@InitialPage LoginPage loginPage) {
		loginAsAdmin(loginPage);
		
		assertThat(ownersPage.isErrorMessageVisible(), is(false));

		final String existentLogin = existentLogin();
		ownersPage.createOwner(existentLogin, "anypassword");
		
		ownersPage.assertOnOwnersPage();
		
		assertThat(ownersPage.isErrorMessageVisible(), is(true));
	}
	
	@Test @InSequence(26)
	@ShouldMatchDataSet("owners.xml")
	@CleanupUsingScript({ "cleanup.sql", "cleanup-autoincrement.sql" })
	public void afterCreateExistingOwner() {}

	
	
	@Test @InSequence(27)
	@UsingDataSet("owners.xml")
	@Cleanup(phase = TestExecutionPhase.NONE)
	public void beforeCreateShortPassword() {}
	
	@Test @InSequence(28)
	@RunAsClient
	public void testCreateShortPassword(@InitialPage LoginPage loginPage) {
		loginAsAdmin(loginPage);
		
		assertThat(ownersPage.isErrorMessageVisible(), is(false));

		ownersPage.createOwner(newOwnerLogin(), "short");
		
		ownersPage.assertOnOwnersPage();
		
		assertThat(ownersPage.isErrorMessageVisible(), is(true));
	}
	
	@Test @InSequence(29)
	@ShouldMatchDataSet("owners.xml")
	@CleanupUsingScript({ "cleanup.sql", "cleanup-autoincrement.sql" })
	public void afterCreateShortPassword() {}

	
	
	@Test @InSequence(31)
	@UsingDataSet("owners.xml")
	@Cleanup(phase = TestExecutionPhase.NONE)
	public void beforeDeleteOwnerWithoutPets() {}
	
	@Test @InSequence(32)
	@RunAsClient
	public void testDeleteOwnerWithoutPets(@InitialPage LoginPage loginPage) {
		loginAsAdmin(loginPage);

		assertDeleteOwner(ownerWithoutPets());
	}
	
	@Test @InSequence(33)
	@ShouldMatchDataSet("owners-remove-without-pets.xml")
	@CleanupUsingScript({ "cleanup.sql", "cleanup-autoincrement.sql" })
	public void afterDeleteOwnerWithoutPets() {}

	
	
	@Test @InSequence(34)
	@UsingDataSet("owners.xml")
	@Cleanup(phase = TestExecutionPhase.NONE)
	public void beforeDeleteOwnerWithPets() {}
	
	@Test @InSequence(35)
	@RunAsClient
	public void testDeleteOwnerWithPets(@InitialPage LoginPage loginPage) {
		loginAsAdmin(loginPage);

		assertDeleteOwner(ownerWithPets());
	}
	
	@Test @InSequence(36)
	@ShouldMatchDataSet("owners-remove-with-pets.xml")
	@CleanupUsingScript({ "cleanup.sql", "cleanup-autoincrement.sql" })
	public void afterDeleteOwnerWithPets() {}

	private void assertDeleteOwner(Owner ownerToDelete) {
		ownersPage.removeOwner(ownerToDelete);
		
		ownersPage.assertOnOwnersPage();
		
		final Owner[] expectedOwners = ownersWithout(ownerToDelete);
		
		assertThat(ownersPage.areOwnersInTable(expectedOwners), is(true));
	}
	
	
	
	@Test @InSequence(41)
	@UsingDataSet("owners.xml")
	@Cleanup(phase = TestExecutionPhase.NONE)
	public void beforeEdit() {}
	
	@Test @InSequence(42)
	@RunAsClient
	public void testEdit(@InitialPage LoginPage loginPage) {
		loginAsAdmin(loginPage);

		final Owner owner = existentOwner();
		ownersPage.editOwner(owner);
		
		assertThat(ownersPage.isEditing(), is(true));
		
		ownersPage.changePassword("newpassword");
		
		ownersPage.assertOnOwnersPage();
		assertThat(ownersPage.isEditing(), is(false));
	}
	
	@Test @InSequence(43)
	@ShouldMatchDataSet(value = "owners-update-password.xml")
	@CleanupUsingScript({ "cleanup.sql", "cleanup-autoincrement.sql" })
	public void afterEdit() {}
	
	
	
	@Test @InSequence(44)
	@UsingDataSet("owners.xml")
	@Cleanup(phase = TestExecutionPhase.NONE)
	public void beforeEditShortPassword() {}
	
	@Test @InSequence(45)
	@RunAsClient
	public void testEditShortPassword(@InitialPage LoginPage loginPage) {
		loginAsAdmin(loginPage);

		assertThat(ownersPage.isErrorMessageVisible(), is(false));
		
		final Owner owner = existentOwner();
		ownersPage.editOwner(owner);
		
		assertThat(ownersPage.isEditing(), is(true));
		
		ownersPage.changePassword("short");
		
		ownersPage.assertOnOwnersPage();
		
		assertThat(ownersPage.isErrorMessageVisible(), is(true));
		assertThat(ownersPage.isEditing(), is(true));
	}
	
	@Test @InSequence(46)
	@UsingDataSet("owners.xml")
	@CleanupUsingScript({ "cleanup.sql", "cleanup-autoincrement.sql" })
	public void afterEditShortPassword() {}
	
	
	
	@Test @InSequence(47)
	@UsingDataSet("owners.xml")
	@Cleanup(phase = TestExecutionPhase.NONE)
	public void beforeCancelEdit() {}
	
	@Test @InSequence(48)
	@RunAsClient
	public void testCancelEdit(@InitialPage LoginPage loginPage) {
		loginAsAdmin(loginPage);
		
		final Owner owner = existentOwner();
		ownersPage.editOwner(owner);
		
		assertThat(ownersPage.isEditing(), is(true));
		
		ownersPage.cancelEdit();
		
		assertThat(ownersPage.isEditing(), is(false));
	}
	
	@Test @InSequence(49)
	@UsingDataSet("owners.xml")
	@CleanupUsingScript({ "cleanup.sql", "cleanup-autoincrement.sql" })
	public void afterCancelEdit() {}
	
	
	
	private void loginAsAdmin(LoginPage loginPage) {
		loginPage.login("jose", "josepass");
		
		ownersPage.assertOnOwnersPage();
	}
}
