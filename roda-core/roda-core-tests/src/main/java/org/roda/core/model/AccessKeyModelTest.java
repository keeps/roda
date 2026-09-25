/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;

import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.CryptographyUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.core.data.v2.accessKey.AccessKeys;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.IdUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class AccessKeyModelTest {
  private static final String UPDATED_BY = "admin";
  private static final long ONE_DAY = 24 * 60 * 60 * 1000L;

  private static Path basePath;
  private static ModelService model;

  @BeforeClass
  public void init() throws IOException {
    basePath = TestsHelper.createBaseTempDir(getClass(), true);
    RodaCoreFactory.instantiateTest(false, false, false, false, false, false, false);
    model = RodaCoreFactory.getModelService();
  }

  @AfterClass
  public void cleanup() throws NotFoundException, GenericException, IOException {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  private static Date daysFromNow(int days) {
    long nowInSeconds = System.currentTimeMillis() / 1000L * 1000L;
    return new Date(nowInSeconds + days * ONE_DAY);
  }

  @Test
  public void regenerateAccessKeyStoresHashOfNewTokenAndNewExpiration() throws RODAException {
    AccessKey accessKey = new AccessKey();
    accessKey.setName("regenerate");
    accessKey.setUserName(IdUtils.createUUID());
    accessKey.setExpirationDate(daysFromNow(1));
    AccessKey created = model.createAccessKey(accessKey, UPDATED_BY);
    String oldToken = created.getKey();

    Date newExpirationDate = daysFromNow(2);
    AccessKey regenerated = model.regenerateAccessKey(model.retrieveAccessKey(created.getId()), newExpirationDate,
      UPDATED_BY);
    String newToken = regenerated.getKey();

    assertNotEquals(newToken, oldToken);
    AccessKey stored = model.retrieveAccessKey(created.getId());
    assertEquals(stored.getKey(), CryptographyUtils.hashTokenSHA256(newToken));
    assertNotEquals(stored.getKey(), newToken);
    assertEquals(stored.getExpirationDate().getTime(), newExpirationDate.getTime());

    AccessKeys accessKeys = model.listAccessKeys();
    AccessKey foundByNewToken = accessKeys.getAccessKeyByKey(CryptographyUtils.hashTokenSHA256(newToken));
    assertNotNull(foundByNewToken);
    assertEquals(foundByNewToken.getId(), created.getId());
    assertNull(accessKeys.getAccessKeyByKey(CryptographyUtils.hashTokenSHA256(oldToken)));
  }
}
