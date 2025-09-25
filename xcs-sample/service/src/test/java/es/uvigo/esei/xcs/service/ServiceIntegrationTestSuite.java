package es.uvigo.esei.xcs.service;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	OwnerServiceIntegrationTest.class,
	OwnerServiceIllegalAccessIntegrationTest.class,
	PetServiceIntegrationTest.class,
	PetServiceIllegalAccessIntegrationTest.class,
	UserServiceIntegrationTest.class,
	UserServiceIllegalAccessIntegrationTest.class
})
public class ServiceIntegrationTestSuite {
}
