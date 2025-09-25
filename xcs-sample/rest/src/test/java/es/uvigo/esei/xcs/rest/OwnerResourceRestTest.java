package es.uvigo.esei.xcs.rest;

import static es.uvigo.esei.xcs.domain.entities.IsEqualToOwner.containsOwnersInAnyOrder;
import static es.uvigo.esei.xcs.domain.entities.IsEqualToOwner.equalToOwner;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.EXISTENT_LOGIN;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.NON_EXISTENT_LOGIN;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.OWNER_WITHOUT_PETS_LOGIN;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.OWNER_WITH_PETS_LOGIN;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.existentOwner;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.newOwnerLogin;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.newOwnerPassword;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.newOwnerWithoutPets;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.newPasswordForExistentOwner;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.owners;
import static es.uvigo.esei.xcs.http.util.HasHttpStatus.hasBadRequestStatus;
import static es.uvigo.esei.xcs.http.util.HasHttpStatus.hasCreatedStatus;
import static es.uvigo.esei.xcs.http.util.HasHttpStatus.hasMethodNotAllowedStatus;
import static es.uvigo.esei.xcs.http.util.HasHttpStatus.hasOkStatus;
import static javax.ws.rs.client.Entity.json;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.extension.rest.client.ArquillianResteasyResource;
import org.jboss.arquillian.extension.rest.client.Header;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.persistence.Cleanup;
import org.jboss.arquillian.persistence.CleanupUsingScript;
import org.jboss.arquillian.persistence.ShouldMatchDataSet;
import org.jboss.arquillian.persistence.TestExecutionPhase;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import es.uvigo.esei.xcs.domain.entities.Owner;
import es.uvigo.esei.xcs.rest.GenericTypes.ListOwnerType;
import es.uvigo.esei.xcs.rest.entity.OwnerCreationData;
import es.uvigo.esei.xcs.rest.entity.OwnerEditionData;
import es.uvigo.esei.xcs.service.OwnerService;

@RunWith(Arquillian.class)
public class OwnerResourceRestTest {
  private final static String BASE_PATH = "api/owner/";
  private static final String BASIC_AUTHORIZATION = "Basic am9zZTpqb3NlcGFzcw=";

  @Deployment
  public static Archive<?> createDeployment() {
    return ShrinkWrap.create(WebArchive.class, "test.war")
      .addClass(OwnerResource.class)
      .addClasses(OwnerCreationData.class, OwnerEditionData.class)
      .addClasses(CORSFilter.class, IllegalArgumentExceptionMapper.class, SecurityExceptionMapper.class)
      .addPackage(OwnerService.class.getPackage())
      .addPackage(Owner.class.getPackage())
      .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
      .addAsWebInfResource("jboss-web.xml")
      .addAsWebInfResource("web.xml")
      .addAsResource("arquillian.extension.persistence.properties")
      .addAsResource("arquillian.extension.persistence.dbunit.properties")
      .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
  }

  @Test
  @InSequence(1)
  @UsingDataSet("owners.xml")
  @Cleanup(phase = TestExecutionPhase.NONE)
  public void beforeGet() {}

  @Test
  @InSequence(2)
  @RunAsClient
  @Header(name = "Authorization", value = BASIC_AUTHORIZATION)
  public void testGet(
    @ArquillianResteasyResource(BASE_PATH + EXISTENT_LOGIN)
    ResteasyWebTarget webTarget
  ) throws Exception {
    final Response response = webTarget.request().get();

    assertThat(response, hasOkStatus());

    final Owner owner = response.readEntity(Owner.class);
    final Owner expected = existentOwner();

    assertThat(owner, is(equalToOwner(expected)));
  }

  @Test
  @InSequence(3)
  @ShouldMatchDataSet("owners.xml")
  @CleanupUsingScript({
    "cleanup.sql", "cleanup-autoincrement.sql"
  })
  public void afterGet() {}

  @Test
  @InSequence(4)
  @UsingDataSet("owners.xml")
  @Cleanup(phase = TestExecutionPhase.NONE)
  public void beforeGetNonExistent() {}

  @Test
  @InSequence(5)
  @RunAsClient
  @Header(name = "Authorization", value = BASIC_AUTHORIZATION)
  public void testGetNonExistent(
    @ArquillianResteasyResource(BASE_PATH + NON_EXISTENT_LOGIN)
    ResteasyWebTarget webTarget
  ) throws Exception {
    final Response response = webTarget.request().get();

    assertThat(response, hasBadRequestStatus());
  }

  @Test
  @InSequence(6)
  @ShouldMatchDataSet("owners.xml")
  @CleanupUsingScript({
    "cleanup.sql", "cleanup-autoincrement.sql"
  })
  public void afterGetNonExistent() {}

  @Test
  @InSequence(10)
  @UsingDataSet("owners.xml")
  @Cleanup(phase = TestExecutionPhase.NONE)
  public void beforeList() {}

  @Test
  @InSequence(11)
  @RunAsClient
  @Header(name = "Authorization", value = BASIC_AUTHORIZATION)
  public void testList(
    @ArquillianResteasyResource(BASE_PATH)
    ResteasyWebTarget webTarget
  ) throws Exception {
    final Response response = webTarget.request().get();

    assertThat(response, hasOkStatus());

    final List<Owner> list = ListOwnerType.readEntity(response);
    assertThat(list, containsOwnersInAnyOrder(owners()));
  }

  @Test
  @InSequence(12)
  @ShouldMatchDataSet("owners.xml")
  @CleanupUsingScript({
    "cleanup.sql", "cleanup-autoincrement.sql"
  })
  public void afterList() {}

  @Test
  @InSequence(20)
  @UsingDataSet("owners.xml")
  @Cleanup(phase = TestExecutionPhase.NONE)
  public void beforeCreate() {}

  @Test
  @InSequence(21)
  @RunAsClient
  @Header(name = "Authorization", value = BASIC_AUTHORIZATION)
  public void testCreate(
    @ArquillianResteasyResource(BASE_PATH)
    ResteasyWebTarget webTarget
  ) throws Exception {
    final Owner newOwner = newOwnerWithoutPets();
    final OwnerCreationData ownerData = new OwnerCreationData(newOwnerLogin(), newOwnerPassword());

    final Response response = webTarget.request().post(json(ownerData));

    assertThat(response, hasCreatedStatus());

    final String location = response.getHeaderString("Location");

    final Response responseGet = authorizedJsonRequestGet(location);
    final Owner owner = responseGet.readEntity(Owner.class);
    assertThat(owner, is(equalToOwner(newOwner)));
  }

  @Test
  @InSequence(22)
  @ShouldMatchDataSet({
    "owners.xml", "owners-create-without-pets.xml"
  })
  @CleanupUsingScript({
    "cleanup.sql", "cleanup-autoincrement.sql"
  })
  public void afterCreate() {}

  @Test
  @InSequence(30)
  @UsingDataSet("owners.xml")
  @Cleanup(phase = TestExecutionPhase.NONE)
  public void beforeUpdatePassword() {}

  @Test
  @InSequence(31)
  @RunAsClient
  @Header(name = "Authorization", value = BASIC_AUTHORIZATION)
  public void testUpdatePassword(
    @ArquillianResteasyResource(BASE_PATH + EXISTENT_LOGIN)
    ResteasyWebTarget webTarget
  ) throws Exception {
    final OwnerEditionData ownerData = new OwnerEditionData(newPasswordForExistentOwner());

    final Response response = webTarget.request().put(json(ownerData));

    assertThat(response, hasOkStatus());
  }

  @Test
  @InSequence(32)
  @ShouldMatchDataSet("owners-update-password.xml")
  @CleanupUsingScript({
    "cleanup.sql", "cleanup-autoincrement.sql"
  })
  public void afterUpdatePassword() {}

  @Test
  @InSequence(40)
  @UsingDataSet("owners.xml")
  @Cleanup(phase = TestExecutionPhase.NONE)
  public void beforeDeleteWithoutPets() {}

  @Test
  @InSequence(41)
  @RunAsClient
  @Header(name = "Authorization", value = BASIC_AUTHORIZATION)
  public void testDeleteWithoutPets(
    @ArquillianResteasyResource(BASE_PATH + OWNER_WITHOUT_PETS_LOGIN)
    ResteasyWebTarget webTarget
  ) throws Exception {
    final Response response = webTarget.request().delete();

    assertThat(response, hasOkStatus());
  }

  @Test
  @InSequence(42)
  @ShouldMatchDataSet("owners-remove-without-pets.xml")
  @CleanupUsingScript({
    "cleanup.sql", "cleanup-autoincrement.sql"
  })
  public void afterDeleteWithoutPets() {}

  @Test
  @InSequence(43)
  @UsingDataSet("owners.xml")
  @Cleanup(phase = TestExecutionPhase.NONE)
  public void beforeDeleteWithPets() {}

  @Test
  @InSequence(44)
  @RunAsClient
  @Header(name = "Authorization", value = BASIC_AUTHORIZATION)
  public void testDeleteWithPets(
    @ArquillianResteasyResource(BASE_PATH + OWNER_WITH_PETS_LOGIN)
    ResteasyWebTarget webTarget
  ) throws Exception {
    final Response response = webTarget.request().delete();

    assertThat(response, hasOkStatus());
  }

  @Test
  @InSequence(45)
  @ShouldMatchDataSet("owners-remove-with-pets.xml")
  @CleanupUsingScript({
    "cleanup.sql", "cleanup-autoincrement.sql"
  })
  public void afterDeleteWithPets() {}

  @Test
  @InSequence(46)
  @UsingDataSet("owners.xml")
  @Cleanup(phase = TestExecutionPhase.NONE)
  public void beforeDeleteNoLogin() {}

  @Test
  @InSequence(47)
  @RunAsClient
  @Header(name = "Authorization", value = BASIC_AUTHORIZATION)
  public void testDeleteNoLogin(
    @ArquillianResteasyResource(BASE_PATH)
    ResteasyWebTarget webTarget
  ) throws Exception {
    final Response response = webTarget.request().delete();

    assertThat(response, hasMethodNotAllowedStatus());
  }

  @Test
  @InSequence(48)
  @ShouldMatchDataSet("owners.xml")
  @CleanupUsingScript({
    "cleanup.sql", "cleanup-autoincrement.sql"
  })
  public void afterDeleteNoLogin() {}

  private static Response authorizedJsonRequestGet(String uri) {
    return ClientBuilder.newClient().target(uri)
      .request(MediaType.APPLICATION_JSON_TYPE)
      .header("Authorization", BASIC_AUTHORIZATION)
      .get();
  }
}
