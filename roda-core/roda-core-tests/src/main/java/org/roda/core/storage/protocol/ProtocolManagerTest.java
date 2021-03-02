package org.roda.core.storage.protocol;

import static org.testng.AssertJUnit.assertEquals;

import java.net.URI;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.protocols.Protocol;
import org.roda.core.protocols.ProtocolManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class ProtocolManagerTest {
  private static ProtocolManager protocolManager;

  @BeforeMethod
  public static void setUp() throws Exception {
    System.out.println("setup");

    boolean deploySolr = false;
    boolean deployLdap = false;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = false;
    boolean deployPluginManager = false;
    boolean deployDefaultResources = false;
    boolean deployProtocolManager = true;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources, deployProtocolManager);
  }

  @AfterMethod
  public static void tearDown() throws RODAException {
    RodaCoreFactory.shutdown();
  }

  @Test
  public void testProtocolManagerFactory() throws GenericException {
    URI httpUri = URI.create("http://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf");
    Protocol httpProtocol = RodaCoreFactory.getProtocol(httpUri);

    assertEquals(httpProtocol.getSchema(), httpUri.getScheme());

    URI fileUri = URI.create("file://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf");
    Protocol fileProtocol = RodaCoreFactory.getProtocol(fileUri);

    assertEquals(fileProtocol.getSchema(), fileUri.getScheme());

    URI httpsUri = URI.create("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf");
    Protocol httpsProtocol = RodaCoreFactory.getProtocol(httpsUri);

    assertEquals(httpsProtocol.getSchema(), httpsUri.getScheme());
  }
}
