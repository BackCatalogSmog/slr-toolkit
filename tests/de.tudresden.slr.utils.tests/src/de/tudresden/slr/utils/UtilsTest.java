package de.tudresden.slr.utils;

import static org.junit.Assert.assertNull;

import org.eclipse.emf.ecore.resource.Resource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class UtilsTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetIFilefromDocument() {
		// null should return null
		assertNull(Utils.getIFilefromEMFResource(null));
		
		//empty ressource set should return null
		Resource nullResourceSetResource = Mockito.mock(Resource.class);
		Mockito.when(nullResourceSetResource.getResourceSet()).thenReturn(null);
		assertNull(Utils.getIFilefromEMFResource(nullResourceSetResource));
	}
}
