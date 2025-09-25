package es.uvigo.esei.xcs.service;

import static es.uvigo.esei.xcs.domain.entities.IsEqualToUser.equalToUser;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.existentOwner;
import static es.uvigo.esei.xcs.domain.entities.UsersDataset.existentAdmin;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.ejb.EJB;
import javax.inject.Inject;

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

import es.uvigo.esei.xcs.domain.entities.Owner;
import es.uvigo.esei.xcs.domain.entities.Pet;
import es.uvigo.esei.xcs.domain.entities.User;
import es.uvigo.esei.xcs.service.util.security.RoleCaller;
import es.uvigo.esei.xcs.service.util.security.TestPrincipal;

@RunWith(Arquillian.class)
@UsingDataSet("owners.xml")
@CleanupUsingScript({ "cleanup.sql", "cleanup-autoincrement.sql" })
public class UserServiceIllegalAccessIntegrationTest {

  @Inject
  private UserService facade;

  @EJB(beanName = "owner-caller")
  private RoleCaller asOwner;

  @EJB(beanName = "admin-caller")
  private RoleCaller asAdmin;
  
  @Inject
  private TestPrincipal principal;
  
  @Deployment
  public static Archive<?> createDeployment() {
    return ShrinkWrap.create(WebArchive.class, "test.war")
      .addClasses(UserService.class)
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
  public void testGetOwnerCredentials() {
    final Owner existentOwner = existentOwner();
    
    principal.setName(existentOwner.getLogin());
    
    final User actualUser = asOwner.call(() -> facade.getCurrentUser());
    
    assertThat(actualUser, is(equalToUser(existentOwner)));
  }

  @Test
  @ShouldMatchDataSet("owners.xml")
  public void testGetAdminCredentials() {
    final User existentAdmin = existentAdmin();
    
    principal.setName(existentAdmin.getLogin());
    
    final User actualUser = asAdmin.call(() -> facade.getCurrentUser());
    
    assertThat(actualUser, is(equalToUser(existentAdmin)));
  }

}
