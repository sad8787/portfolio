package es.uvigo.esei.xcs.service;

import javax.ejb.EJBAccessException;
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

import es.uvigo.esei.xcs.domain.entities.Pet;
import es.uvigo.esei.xcs.service.util.security.RoleCaller;

@RunWith(Arquillian.class)
@UsingDataSet("owners.xml")
@CleanupUsingScript({ "cleanup.sql", "cleanup-autoincrement.sql" })
public class UserServiceIntegrationTest {

  @Inject
  private UserService facade;
  
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

  @Test(expected = EJBAccessException.class)
  @ShouldMatchDataSet("owners.xml")
  public void testGetCredentialsNoUser() {
    facade.getCurrentUser();
  }
}
