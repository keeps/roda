/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.schema.collections;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.Permissions;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class FulltextCollectionInfoTest {

  private static final List<String> ANCESTORS = List.of("parent");
  private static final List<String> FULLTEXT = List.of("text 1", "text 2");

  @Test
  public void testAIPInfoWithFulltext() {
    Map<String, Object> fields = new AIPCollection.Info(ANCESTORS, false, null, null, false, FULLTEXT)
      .getPreCalculatedFields();

    Assert.assertEquals(fields.get(RodaConstants.AIP_FULLTEXT), FULLTEXT);
    Assert.assertEquals(fields.get(RodaConstants.AIP_ANCESTORS), ANCESTORS);
  }

  @Test
  public void testAIPInfoWithoutFulltext() {
    Assert.assertFalse(new AIPCollection.Info(ANCESTORS, false, null, null, false, Collections.emptyList())
      .getPreCalculatedFields().containsKey(RodaConstants.AIP_FULLTEXT));
    Assert.assertFalse(new AIPCollection.Info(ANCESTORS, false, null, null, false, null).getPreCalculatedFields()
      .containsKey(RodaConstants.AIP_FULLTEXT));
    Assert.assertFalse(
      new AIPCollection.Info(ANCESTORS, true).getPreCalculatedFields().containsKey(RodaConstants.AIP_FULLTEXT));
  }

  @Test
  public void testRepresentationInfoWithFulltext() {
    Map<String, Object> fields = new RepresentationCollection.Info(createAIP(), ANCESTORS, 10L, 2L, 0L, FULLTEXT,
      false).getPreCalculatedFields();

    Assert.assertEquals(fields.get(RodaConstants.REPRESENTATION_FULLTEXT), FULLTEXT);
    Assert.assertEquals(fields.get(RodaConstants.REPRESENTATION_SIZE_IN_BYTES), 10L);
    Assert.assertEquals(fields.get(RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FILES), 2L);
  }

  @Test
  public void testRepresentationInfoWithoutFulltext() {
    Assert.assertFalse(
      new RepresentationCollection.Info(createAIP(), ANCESTORS, 0L, 0L, 0L, Collections.emptyList(), false)
        .getPreCalculatedFields().containsKey(RodaConstants.REPRESENTATION_FULLTEXT));
    Assert.assertFalse(new RepresentationCollection.Info(createAIP(), ANCESTORS, 0L, 0L, 0L, null, false)
      .getPreCalculatedFields().containsKey(RodaConstants.REPRESENTATION_FULLTEXT));
  }

  private static AIP createAIP() {
    return new AIP("aip", null, "type", AIPState.ACTIVE, new Permissions());
  }
}
