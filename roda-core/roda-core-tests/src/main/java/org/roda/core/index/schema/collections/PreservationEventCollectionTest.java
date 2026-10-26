/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.schema.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.solr.common.SolrInputDocument;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.RODA_TYPE;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LinkingObjectUtils;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent.PreservationMetadataEventClass;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.index.schema.Field;
import org.roda.core.model.ModelService;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class PreservationEventCollectionTest {

  private static final String EVENT_ID = "event-id";
  private static final String INSTANCE_ID = "instance-id";

  private MockedStatic<RodaCoreFactory> rodaCoreFactory;
  private ModelService model;
  private PreservationEventCollection collection;

  @BeforeMethod
  public void setUp() {
    rodaCoreFactory = Mockito.mockStatic(RodaCoreFactory.class);
    rodaCoreFactory.when(RodaCoreFactory::getDistributedModeType).thenReturn(RodaConstants.DistributedModeType.BASE);
    rodaCoreFactory.when(() -> RodaCoreFactory.getRodaConfigurationAsList(Mockito.<String> any()))
      .thenReturn(Collections.emptyList());

    model = Mockito.mock(ModelService.class);
    collection = new PreservationEventCollection();
  }

  @AfterMethod
  public void tearDown() {
    rodaCoreFactory.close();
  }

  @Test
  public void testLinkingSourceObjectIdentifierFieldIsDeclared() {
    Optional<Field> field = collection.getFields().stream()
      .filter(f -> RodaConstants.PRESERVATION_EVENT_LINKING_SOURCE_OBJECT_IDENTIFIER.equals(f.getName())).findFirst();

    Assert.assertTrue(field.isPresent());
    Assert.assertEquals(field.get().getType(), Field.TYPE_STRING);
    Assert.assertEquals(field.get().getMultiValued(), Optional.of(true));
  }

  @Test
  public void testRepositoryEventIndexesSourceUUID() throws Exception {
    String aipId = "1a2b3c4d-aaaa-bbbb-cccc-1234567890ab";
    SolrInputDocument doc = indexRepositoryEvent(
      Collections.singletonList(source(urn(RODA_TYPE.AIP, aipId), RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE)),
      null);

    Assert.assertEquals(doc.getFieldValue(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS),
      PreservationMetadataEventClass.REPOSITORY.toString());
    Assert.assertEquals(sourceValues(doc), Collections.singletonList(aipId));
  }
  
  @Test
  public void testRepositoryEventSkipsBlankSourceValue() throws Exception {
    SolrInputDocument doc = indexRepositoryEvent(
      Collections.singletonList(source(" ", RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE)), null);

    Assert.assertNull(doc.getField(RodaConstants.PRESERVATION_EVENT_LINKING_SOURCE_OBJECT_IDENTIFIER));
  }

  @Test
  public void testAIPEventDoesNotIndexSources() throws Exception {
    String aipId = "aip-id";
    PreservationMetadata pm = preservationMetadata();
    pm.setAipId(aipId);
    mockEventBinary(aipId,
      Collections.singletonList(source(urn(RODA_TYPE.AIP, aipId), RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE)),
      null);

    SolrInputDocument doc = collection.toSolrDocument(model, pm, new PreservationEventCollection.Info(null));

    Assert.assertEquals(doc.getFieldValue(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS),
      PreservationMetadataEventClass.AIP.toString());
    Assert.assertNull(doc.getField(RodaConstants.PRESERVATION_EVENT_LINKING_SOURCE_OBJECT_IDENTIFIER));
  }

  private SolrInputDocument indexRepositoryEvent(List<LinkingIdentifier> sources, List<LinkingIdentifier> outcomes)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    mockEventBinary(null, sources, outcomes);
    return collection.toSolrDocument(model, preservationMetadata(), new PreservationEventCollection.Info(null));
  }

  private void mockEventBinary(String aipId, List<LinkingIdentifier> sources, List<LinkingIdentifier> outcomes)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    ContentPayload payload = PremisV3Utils.createPremisEventBinary(EVENT_ID, new Date(), "deletion", "details",
      sources, outcomes, "success", "", "", null);
    Binary binary = Mockito.mock(Binary.class);
    Mockito.when(binary.getContent()).thenReturn(payload);
    Mockito.when(model.retrievePreservationEvent(aipId, null, null, null, EVENT_ID)).thenReturn(binary);
  }

  private static PreservationMetadata preservationMetadata() {
    PreservationMetadata pm = new PreservationMetadata();
    pm.setId(EVENT_ID);
    pm.setType(PreservationMetadataType.EVENT);
    return pm;
  }

  private static String urn(RODA_TYPE type, String uuid) {
    return LinkingObjectUtils.getLinkingIdentifierId(type, uuid, INSTANCE_ID);
  }

  private static LinkingIdentifier source(String value, String role) {
    return new LinkingIdentifier(RodaConstants.PREMIS_IDENTIFIER_TYPE_URN, value, Collections.singletonList(role));
  }

  private static Collection<Object> sourceValues(SolrInputDocument doc) {
    return doc.getFieldValues(RodaConstants.PRESERVATION_EVENT_LINKING_SOURCE_OBJECT_IDENTIFIER);
  }
}
