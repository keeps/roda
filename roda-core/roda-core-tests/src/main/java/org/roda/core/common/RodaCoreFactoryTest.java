package org.roda.core.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.storage.fs.FSUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_TRAVIS})
public class RodaCoreFactoryTest {

  private static Path basePath;

  @BeforeClass
  public static void setUp() throws IOException {
    basePath = TestsHelper.createBaseTempDir(RodaCoreFactoryTest.class, true);
    RodaCoreFactory.instantiateTest(false, false, false, false, false, false);
  }

  @AfterClass
  public static void cleanup() throws IOException, NotFoundException, GenericException {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @Test
  public void testPathTransversalRelative() throws IOException {
    // set up
    Path configPath = RodaCoreFactory.getConfigPath();
    Files.createDirectories(configPath);
    String configurationFile = "../../../../../../../etc/passwd";

    Assert.assertNull(RodaCoreFactory.getConfigurationFileAsStream(configurationFile));
    Assert.assertNull(RodaCoreFactory.getConfigurationFile(configurationFile));
    Assert.assertNull(RodaCoreFactory.getConfigurationFileAsStream(configurationFile, configurationFile));
    Assert.assertNull(RodaCoreFactory.getDefaultFileAsStream(configurationFile));
    Assert.assertEquals(RodaCoreFactory.getRodaSchema(configurationFile, ""), Optional.empty());
  }

  @Test
  public void testPathTransversalAbsolute() throws IOException {
    // set up
    Path configPath = RodaCoreFactory.getConfigPath();
    Files.createDirectories(configPath);
    String configurationFile = "/etc/passwd";

    Assert.assertNull(RodaCoreFactory.getConfigurationFileAsStream(configurationFile));
    Assert.assertNull(RodaCoreFactory.getConfigurationFile(configurationFile));
    Assert.assertNull(RodaCoreFactory.getConfigurationFileAsStream(configurationFile, configurationFile));
    Assert.assertNull(RodaCoreFactory.getDefaultFileAsStream(configurationFile));
    Assert.assertEquals(RodaCoreFactory.getRodaSchema(configurationFile, ""), Optional.empty());
  }

  // TODO test symbolic link

  @Test
  public void testPathTransversalScoped() throws IOException, GenericException {

    String scope = "theme";
    String secret = "secret.txt";

    // set up
    Path configPath = RodaCoreFactory.getConfigPath();
    Files.createDirectories(configPath);
    Files.createDirectories(configPath.resolve(scope));
    Path secretFile = Files.createFile(configPath.resolve(secret));
    Path secretFileLink = Files.createSymbolicLink(configPath.resolve(Paths.get(scope, secret)),
      configPath.resolve(secret));

    try {
      RodaCoreFactory.getScopedConfigurationFileAsStream(Paths.get(scope), "../" + secret);
      Assert.fail("Should have got an exception");
    } catch (GenericException e) {
      // expected
    }

    try {
      RodaCoreFactory.getScopedConfigurationFileAsStream(Paths.get(scope), secret);
      Assert.fail("Should have got an exception");
    } catch (GenericException e) {
      // expected
    }

    try {
      RodaCoreFactory.getScopedConfigurationFileAsStream(Paths.get(scope), secretFile.toAbsolutePath().toString());
      Assert.fail("Should have got an exception");
    } catch (GenericException e) {
      // expected
    }

    // cleanup
    Files.deleteIfExists(secretFile);
    Files.deleteIfExists(secretFileLink);
  }

}
