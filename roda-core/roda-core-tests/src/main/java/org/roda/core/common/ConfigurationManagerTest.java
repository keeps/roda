package org.roda.core.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.config.ConfigurationManager;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.storage.fs.FSUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_TRAVIS})
public class ConfigurationManagerTest {

  private static Path basePath;
  private static ConfigurationManager configurationManager;

  @BeforeClass
  public static void setUp() throws IOException, ConfigurationException {
    basePath = TestsHelper.createBaseTempDir(RodaCoreFactoryTest.class, true);
    RodaCoreFactory.instantiateTest(false, false, false, false, false, false, false);
    configurationManager = RodaCoreFactory.getConfigurationManager();
    configurationManager.addConfiguration("roda-wui.properties");
  }

  @AfterClass
  public static void cleanup() throws IOException, NotFoundException, GenericException {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @Test
  public void testPathTransversalRelative() throws IOException {
    // set up
    Path configPath = configurationManager.getConfigPath();
    Files.createDirectories(configPath);
    String configurationFile = "../../../../../../../etc/passwd";

    Assert.assertNull(configurationManager.getConfigurationFileAsStream(configurationFile));
    Assert.assertNull(configurationManager.getConfigurationFile(configurationFile));
    Assert.assertNull(configurationManager.getConfigurationFileAsStream(configurationFile, configurationFile));
    Assert.assertNull(RodaCoreFactory.getDefaultFileAsStream(configurationFile));
    Assert.assertEquals(RodaCoreFactory.getRodaSchema(configurationFile, ""), Optional.empty());
  }

  @Test
  public void testPathTransversalAbsolute() throws IOException {
    // set up
    Path configPath = configurationManager.getConfigPath();
    Files.createDirectories(configPath);
    String configurationFile = "/etc/passwd";

    Assert.assertNull(configurationManager.getConfigurationFileAsStream(configurationFile));
    Assert.assertNull(configurationManager.getConfigurationFile(configurationFile));
    Assert.assertNull(configurationManager.getConfigurationFileAsStream(configurationFile, configurationFile));
    Assert.assertNull(RodaCoreFactory.getDefaultFileAsStream(configurationFile));
    Assert.assertEquals(RodaCoreFactory.getRodaSchema(configurationFile, ""), Optional.empty());
  }

  @Test
  public void testPathTransversalScoped() throws IOException, GenericException {

    String scope = "theme";
    String secret = "secret.txt";
    String item = "item.txt";

    // set up
    Path configPath = configurationManager.getConfigPath();
    Files.createDirectories(configPath);
    Files.createDirectories(configPath.resolve(scope));
    Path secretFile = Files.createFile(configPath.resolve(secret));
    Path secretFileLink = Files.createSymbolicLink(configPath.resolve(Paths.get(scope, secret)),
      configPath.resolve(secret));
    Path publicFile = configPath.resolve(Paths.get(scope, item));
    Files.copy(new ByteArrayInputStream(item.getBytes()), publicFile);

    try {
      configurationManager.getScopedConfigurationFileAsStream(Paths.get(scope), "../" + secret);
      Assert.fail("Should have got an exception");
    } catch (GenericException e) {
      // expected
    }

    // test link
    configurationManager.setConfigSymbolicLinksAllowed(false);
    try {
      configurationManager.getScopedConfigurationFileAsStream(Paths.get(scope), secret);
      Assert.fail("Should have got an exception");
    } catch (GenericException e) {
      // expected
    }

    configurationManager.setConfigSymbolicLinksAllowed(true);
    InputStream secretByLink = configurationManager.getScopedConfigurationFileAsStream(Paths.get(scope), secret);
    Assert.assertNotNull(secretByLink, "Secret by link should be allowed here");

    try {
      configurationManager.getScopedConfigurationFileAsStream(Paths.get(scope), secretFile.toAbsolutePath().toString());
      Assert.fail("Should have got an exception");
    } catch (GenericException e) {
      // expected
    }

    InputStream itemStream = configurationManager.getScopedConfigurationFileAsStream(Paths.get(scope), item);
    Assert.assertEquals(new String(IOUtils.toByteArray(itemStream)), item);

    // cleanup
    Files.deleteIfExists(secretFile);
    Files.deleteIfExists(secretFileLink);
    Files.deleteIfExists(publicFile);
  }

  @Test(invocationCount = 2)
  public void testSharedProperties() throws GenericException {
    // invocationCount of 2 is needed to make the tests fail when the cached
    // properties map is being shared between locales (resulting in the next
    // un-cached locale overwriting the previous values)
    Locale enLocale = Locale.ENGLISH;
    Locale ptLocale = new Locale("pt", "PT");

    // shared properties expected to be present in roda-wui
    final Map<String, List<String>> baseProperties = ImmutableMap.<String, List<String>> builder()
      .put("testing.prefix.string", Collections.singletonList("string"))
      .put("testing.prefix.array", Arrays.asList("first", "second"))
      .put("testing.property.thisOne", Collections.singletonList("value"))
      .put(RodaConstants.RODA_NODE_TYPE_KEY, Collections.singletonList(configurationManager.getNodeType().toString()))
      .put(RodaConstants.DISTRIBUTED_MODE_TYPE_PROPERTY,
        Collections.singletonList(configurationManager.getDistributedModeType().toString()))
      .put(RodaConstants.CORE_SYNCHRONIZATION_FOLDER,
        Collections.singletonList(configurationManager.getSynchronizationDirectoryPath().toString()))
      .build();

    // add shared properties expected to be present in ServerMessages_locale
    Map<String, List<String>> enProperties = new HashMap<>(baseProperties);
    enProperties.put("i18n.testing.language", Collections.singletonList("english"));

    Map<String, List<String>> ptProperties = new HashMap<>(baseProperties);
    ptProperties.put("i18n.testing.language", Collections.singletonList("portuguese"));

    // check english properties
    Map<String, List<String>> rodaEnProperties = configurationManager.getRodaSharedProperties(enLocale);
    assert enProperties.equals(rodaEnProperties) : "shared properties for locale '" + enLocale + "' are "
      + rodaEnProperties + " when they should be " + enProperties;

    // check portuguese properties
    Map<String, List<String>> rodaPtProperties = configurationManager.getRodaSharedProperties(ptLocale);
    assert ptProperties.equals(rodaPtProperties) : "shared properties for locale '" + ptLocale + "' are "
      + rodaPtProperties + " when they should be " + ptProperties;
  }

  @Test
  public void testGetConfigurationFile() throws IOException {
    // set up
    Path configPath = configurationManager.getConfigPath();
    Files.createDirectories(configPath);
    String configurationFile = "roda-wui.properties";
    Path configurationFilePath = Files.createFile(configPath.resolve(configurationFile));

    // check if file exists
    Assert.assertEquals(configurationManager.getConfigurationFile(configurationFile).getPath(),
      configurationFilePath.toString());
  }

  @Test
  public void testGetConfigurationFileAsStream() throws IOException {
    // set up
    Path configPath = configurationManager.getConfigPath();
    Files.createDirectories(configPath);
    String configurationFile = "roda-wui.properties";
    Path configurationFilePath = Files.createFile(configPath.resolve(configurationFile));

    // check if input stream is not null
    InputStream configurationFileAsStream = configurationManager
      .getConfigurationFileAsStream(configurationFilePath.toString());
    Assert.assertNotNull(configurationFileAsStream);
  }

}
