package org.roda.core.storage.protocol;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URI;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class ProtocolManagerTest {
  @BeforeMethod
  public static void setUp() throws Exception {
    System.out.println("setup");
  }
  @AfterMethod
  public static void tearDown() throws RODAException {
    System.out.println("teardown");
  }

  @Test
  public void testConnection() throws MalformedURLException, GenericException {
    System.out.println("testConnection");

    ProtocolManager protocol = new ProtocolManagerFactory().createProtocolManager(URI.create("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"));

  }
}
