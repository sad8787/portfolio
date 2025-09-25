package es.uvigo.esei.xcs.rest;

import static es.uvigo.esei.xcs.domain.entities.IsEqualToOwner.containsOwnersInAnyOrder;
import static es.uvigo.esei.xcs.domain.entities.IsEqualToOwner.equalToOwner;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.anyLogin;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.existentLogin;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.existentOwner;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.existentPassword;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.newOwnerLogin;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.newOwnerPassword;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.newPasswordForExistentOwner;
import static es.uvigo.esei.xcs.domain.entities.OwnersDataset.owners;
import static es.uvigo.esei.xcs.http.util.HasHttpStatus.hasCreatedStatus;
import static es.uvigo.esei.xcs.http.util.HasHttpStatus.hasOkStatus;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URI;
import java.util.List;

import javax.persistence.EntityExistsException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import es.uvigo.esei.xcs.domain.entities.Owner;
import es.uvigo.esei.xcs.domain.entities.OwnersDataset;
import es.uvigo.esei.xcs.rest.entity.OwnerCreationData;
import es.uvigo.esei.xcs.rest.entity.OwnerEditionData;
import es.uvigo.esei.xcs.service.OwnerService;

@RunWith(EasyMockRunner.class)
public class OwnerResourceUnitTest extends EasyMockSupport {
  @TestSubject
  private OwnerResource resource = new OwnerResource();

  @Mock
  private OwnerService facade;

  @Mock
  private UriInfo uriInfo;

  @Mock
  private UriBuilder uriBuilder;

  @After
  public void tearDown() throws Exception {
    verifyAll();
  }

  @Test
  public void testGet() {
    final Owner owner = OwnersDataset.anyOwner();

    expect(facade.get(owner.getLogin()))
      .andReturn(owner);

    replayAll();

    final Response response = resource.get(owner.getLogin());

    assertThat(response, hasOkStatus());
    assertThat(response.getEntity(), is(instanceOf(Owner.class)));
    assertThat((Owner) response.getEntity(), is(equalToOwner(owner)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetNull() {
    replayAll();

    resource.get(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetMissing() {
    final String login = anyLogin();

    expect(facade.get(login))
      .andReturn(null);

    replayAll();

    resource.get(login);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testList() {
    final Owner[] owners = owners();

    expect(facade.list())
      .andReturn(asList(owners));

    replayAll();

    final Response response = resource.list();

    assertThat(response, hasOkStatus());
    assertThat(response.getEntity(), is(instanceOf(List.class)));
    assertThat((List<Owner>) response.getEntity(), containsOwnersInAnyOrder(owners));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testListEmpty() {
    expect(facade.list())
      .andReturn(emptyList());

    replayAll();

    final Response response = resource.list();

    assertThat(response, hasOkStatus());
    assertThat(response.getEntity(), is(instanceOf(List.class)));
    assertThat((List<Owner>) response.getEntity(), is(empty()));
  }

  @Test
  public void testCreate() throws Exception {
    final OwnerCreationData newOwner = new OwnerCreationData(newOwnerLogin(), newOwnerPassword());
    final Owner createdOwner = OwnersDataset.newOwnerWithoutPets();

    final URI mockUri = new URI("http://host/api/owner/" + newOwner.getLogin());

    expect(facade.create(anyObject(Owner.class)))
      .andReturn(createdOwner);

    expect(uriInfo.getAbsolutePathBuilder())
      .andReturn(uriBuilder);
    expect(uriBuilder.path(newOwner.getLogin()))
      .andReturn(uriBuilder);
    expect(uriBuilder.build())
      .andReturn(mockUri);

    replayAll();

    final Response response = resource.create(newOwner);

    assertThat(response, hasCreatedStatus());
    assertThat(response.getHeaderString("Location"), is(equalTo(mockUri.toString())));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateNull() {
    replayAll();

    resource.create(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateExistentOwner() {
    final OwnerCreationData existentOwner = new OwnerCreationData(existentLogin(), existentPassword());

    expect(facade.create(anyObject(Owner.class)))
      .andThrow(new EntityExistsException());

    replayAll();

    resource.create(existentOwner);
  }

  @Test
  public void testUpdate() {
    final Owner owner = existentOwner();
    final Owner ownerWithChangedPassword = existentOwner();
    ownerWithChangedPassword.changePassword(newPasswordForExistentOwner());

    final OwnerEditionData ownerData = new OwnerEditionData(newPasswordForExistentOwner());

    expect(facade.get(owner.getLogin()))
      .andReturn(owner);

    expect(facade.update(anyObject(Owner.class)))
      .andReturn(owner);

    replayAll();

    final Response response = resource.update(owner.getLogin(), ownerData);

    assertThat(response, hasOkStatus());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateNull() {
    replayAll();

    resource.update(null, null);
  }

  @Test
  public void testDelete() {
    final String login = anyLogin();

    facade.remove(login);

    replayAll();

    final Response response = resource.delete(login);

    assertThat(response, hasOkStatus());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDeleteNull() {
    replayAll();

    resource.delete(null);
  }
}
