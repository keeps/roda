package org.roda.core.index;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.mockito.Mockito;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.ReturnWithExceptions;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmation;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationState;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelService;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

// @PrepareForTest(SolrUtils.class)
public class SolrRetryTest {

  private static Path basePath;
  private static IndexService index;
  private static ModelService model;
  private static StorageService corporaService;

  @BeforeClass
  public static void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(IndexServiceTest.class, true);

    boolean deploySolr = true;
    boolean deployLdap = false;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = false;
    boolean deployPluginManager = false;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources);

    URL corporaURL = IndexServiceTest.class.getResource("/corpora");
    corporaService = new FileStorageService(Paths.get(corporaURL.toURI()));

    index = RodaCoreFactory.getIndexService();
    model = RodaCoreFactory.getModelService();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    IndexTestUtils.resetIndex();
    RodaCoreFactory.shutdown();
  }

  @Test
  public void testSolrRetryCommit() throws SolrServerException, IOException, GenericException {
    SolrClient solrClient = index.getSolrClient();
    SolrClient spy = Mockito.spy(solrClient);

    Mockito.doThrow(new SolrServerException("test")).doCallRealMethod().when(spy).commit(anyString(), anyBoolean(),
      anyBoolean(), anyBoolean());

    SolrUtils.commit(spy, IndexedAIP.class);

    Mockito.verify(spy, times(2)).commit(anyString(), anyBoolean(), anyBoolean(), anyBoolean());
  }

  @Test
  public void testSolrRetryGetObjectLabel() throws SolrServerException, IOException, RequestNotValidException,
    AuthorizationDeniedException, ValidationException, AlreadyExistsException, NotFoundException, GenericException {
    SolrClient solrClient = index.getSolrClient();
    SolrClient spy = Mockito.spy(solrClient);

    Mockito.doThrow(new SolrServerException("test")).doCallRealMethod().when(spy).getById(anyString(), anyString());

    String aipId = IdUtils.createUUID();

    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    Assert.assertEquals("My example", SolrUtils.getObjectLabel(spy, AIP.class.getName(), aipId));

    Mockito.verify(spy, times(2)).getById(anyString(), anyString());
  }

  @Test
  public void testSolrRetryCreate2() throws IOException, SolrServerException, GenericException,
    AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException, NotFoundException {
    SolrClient solrClient = index.getSolrClient();
    SolrClient spy = Mockito.spy(solrClient);

    Mockito.doThrow(new SolrServerException("test")).doThrow(new SolrServerException("test")).doCallRealMethod()
      .when(spy).add(anyString(), any(SolrInputDocument.class));

    DisposalConfirmation confirmation = new DisposalConfirmation();
    confirmation.setTitle("Confirmation");
    confirmation.setNumberOfAIPs(100L);
    confirmation.setState(DisposalConfirmationState.PENDING);
    confirmation.setSize(12346234L);

    DisposalConfirmation disposalConfirmation = model.createDisposalConfirmation(confirmation, "admin");

    SolrUtils.create2(spy, this, DisposalConfirmation.class, disposalConfirmation);

    Mockito.verify(spy, times(3)).add(anyString(), any(SolrInputDocument.class));

    DisposalConfirmation retrieve = index.retrieve(DisposalConfirmation.class, disposalConfirmation.getId(),
      new ArrayList<>());

    Assert.assertEquals(retrieve, disposalConfirmation);
  }

  @Test
  public void testSolrRetryDelete() throws SolrServerException, IOException, RequestNotValidException,
    AuthorizationDeniedException, ValidationException, AlreadyExistsException, GenericException, NotFoundException {
    SolrClient solrClient = index.getSolrClient();
    SolrClient spy = Mockito.spy(solrClient);

    Mockito
      .doThrow(new SolrServerException("test"), new SolrServerException("test"), new SolrServerException("test"),
        new SolrServerException("test"), new SolrServerException("test"), new SolrServerException("test"),
        new SolrServerException("test"), new SolrServerException("test"), new SolrServerException("test"))
      .doCallRealMethod().when(spy).deleteById(anyString(), anyList());

    String aipId = IdUtils.createUUID();

    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    SolrUtils.delete(spy, IndexedAIP.class, Collections.singletonList(aipId), this, false);

    Mockito.verify(spy, times(10)).deleteById(anyString(), anyList());

    Assert.assertThrows(NotFoundException.class, () -> index.retrieve(IndexedAIP.class, aipId, new ArrayList<>()));
  }
}
