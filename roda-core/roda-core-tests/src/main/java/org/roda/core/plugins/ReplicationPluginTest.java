/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.monitor.TransferredResourcesScanner;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.SolrType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.orchestrate.JobsHelper;
import org.roda.core.plugins.plugins.base.ReplicationPlugin;
import org.roda.core.plugins.plugins.ingest.TransferredResourceToAIPPlugin;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_TRAVIS})
public class ReplicationPluginTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReplicationPluginTest.class);
  private static final int GENERATED_FILE_SIZE = 100;
  private static final int NUMBER_OF_AIPS = 1000;

  private static Path basePath;
  private static Path testPath;

  private static StorageService storage;
  private static ModelService model;
  private static IndexService index;

  private static String parentResourceUUID = null;
  private static int resourceCounter = 0;

  @BeforeClass
  public void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(getClass(), true,
      PosixFilePermissions
        .asFileAttribute(new HashSet<>(Arrays.asList(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
          PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE))));

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = true;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = false;

    // embedded Apache Solr
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources, SolrType.EMBEDDED);

    // // HTTP Apache Solr
    // RodaCoreFactory.instantiateTest(deploySolr, deployLdap,
    // deployFolderMonitor, deployOrchestrator,
    // deployPluginManager, deployDefaultResources, SolrType.HTTP);

    // // Cloud Apache Solr
    // RodaCoreFactory.instantiateTest(deploySolr, deployLdap,
    // deployFolderMonitor, deployOrchestrator,
    // deployPluginManager, deployDefaultResources, SolrType.HTTP_CLOUD);

    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();
    storage = RodaCoreFactory.getStorageService();

    testPath = TestsHelper.createBaseTempDir(getClass(), true,
      PosixFilePermissions
        .asFileAttribute(new HashSet<>(Arrays.asList(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
          PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE))));

    Files.createDirectories(
      testPath.resolve(RodaConstants.CORE_STORAGE_FOLDER).resolve(RodaConstants.STORAGE_CONTAINER_AIP));
    Files.createDirectories(
      testPath.resolve(RodaConstants.CORE_STORAGE_FOLDER).resolve(RodaConstants.STORAGE_CONTAINER_PRESERVATION)
        .resolve(RodaConstants.STORAGE_CONTAINER_PRESERVATION_AGENTS));

    LOGGER.info("Running {} tests under storage {}", getClass().getSimpleName(), basePath);
  }

  @AfterClass
  public void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
    FSUtils.deletePath(testPath);
  }

  private ByteArrayInputStream generateContentData() {
    return new ByteArrayInputStream(RandomStringUtils.randomAscii(GENERATED_FILE_SIZE).getBytes());
  }

  private String createCorpora() throws InterruptedException, IOException, FileAlreadyExistsException,
    NotFoundException, GenericException, RequestNotValidException, AlreadyExistsException {
    TransferredResourcesScanner f = RodaCoreFactory.getTransferredResourcesScanner();

    String parentUUID = f.createFolder(null, "test").getUUID();
    index.commit(TransferredResource.class);

    index.retrieve(TransferredResource.class, parentUUID, new ArrayList<>());
    return parentUUID;
  }

  private TransferredResource createCorpora(String parentUUID, int i) throws InterruptedException, IOException,
    FileAlreadyExistsException, NotFoundException, GenericException, RequestNotValidException, AlreadyExistsException {
    TransferredResourcesScanner f = RodaCoreFactory.getTransferredResourcesScanner();

    String test1UUID = f.createFolder(parentUUID, "test" + i).getUUID();
    f.createFile(test1UUID, "test" + i + ".txt", generateContentData());
    index.commit(TransferredResource.class);

    return index.retrieve(TransferredResource.class, test1UUID, new ArrayList<>());
  }

  private List<String> ingestCorpora() throws RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, InvalidParameterException, InterruptedException, IOException,
    FileAlreadyExistsException, SolrServerException {

    if (parentResourceUUID == null) {
      parentResourceUUID = createCorpora();
    }

    List<String> resourceUUIDs = new ArrayList<>();

    for (int i = 0; i < NUMBER_OF_AIPS / 2; i++) {
      TransferredResource transferredResource = createCorpora(parentResourceUUID, resourceCounter);
      AssertJUnit.assertNotNull(transferredResource);
      resourceUUIDs.add(transferredResource.getUUID());
      resourceCounter++;
    }

    Job job = TestsHelper.executeJob(TransferredResourceToAIPPlugin.class, new HashMap<>(), PluginType.SIP_TO_AIP,
      SelectedItemsList.create(TransferredResource.class, resourceUUIDs));

    index.commitAIPs();
    return TestsHelper.getJobReports(index, job).stream().map(r -> r.getOutcomeObjectId()).collect(Collectors.toList());
  }

  @Test
  private void testRsyncViaListOfIds() throws FileAlreadyExistsException, RequestNotValidException, NotFoundException,
    GenericException, AlreadyExistsException, AuthorizationDeniedException, InvalidParameterException,
    InterruptedException, IOException, SolrServerException, JobAlreadyStartedException, CommandException {
    // increase number of job workers
    int originalNumberOfJobWorkers = JobsHelper.getNumberOfJobsWorkers();
    JobsHelper.setNumberOfJobsWorkers(originalNumberOfJobWorkers * 2);
    // increase sync job execution timeout in seconds
    JobsHelper.setSyncTimeout(1200);

    LOGGER.info("Ingesting first pack of corpora...");
    List<String> aips = ingestCorpora();
    LOGGER.info("Rsyncing first pack of corpora...");
    executeJobWithReplicationPlugin(aips);

    LOGGER.info("Ingesting second pack of corpora...");
    List<String> newAips = ingestCorpora();
    LOGGER.info("Rsyncing second pack of corpora...");
    executeJobWithReplicationPlugin(newAips);

    LOGGER.info("Testing if destination has the number of AIPs that was supposed");
    Assert.assertEquals(FSUtils
      .countPath(testPath.resolve(RodaConstants.CORE_STORAGE_FOLDER).resolve(RodaConstants.STORAGE_CONTAINER_AIP))
      .intValue(), NUMBER_OF_AIPS);

    AIP aip1 = model.retrieveAIP(aips.get(0));
    AIP aip2 = model.retrieveAIP(aips.get(1));
    Binary binary = null;

    Representation rep1 = aip1.getRepresentations().get(0);
    CloseableIterable<OptionalWithCause<File>> listFilesUnder1 = model.listFilesUnder(aip1.getId(), rep1.getId(),
      false);
    Iterator<OptionalWithCause<File>> iterator1 = listFilesUnder1.iterator();
    while (iterator1.hasNext()) {
      OptionalWithCause<File> next = iterator1.next();
      File file = next.get();
      binary = storage.getBinary(
        ModelUtils.getFileStoragePath(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId()));
    }

    Representation rep2 = aip2.getRepresentations().get(0);
    CloseableIterable<OptionalWithCause<File>> listFilesUnder2 = model.listFilesUnder(aip2.getId(), rep2.getId(),
      false);
    Iterator<OptionalWithCause<File>> iterator2 = listFilesUnder2.iterator();
    while (iterator2.hasNext()) {
      OptionalWithCause<File> next = iterator2.next();
      File file = next.get();
      if (binary != null) {
        model.updateFile(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(), binary.getContent(),
          true, true);
      }
    }

    executeJobWithReplicationPlugin(Arrays.asList(aip2.getId()));

    List<String> diffCommand = new ArrayList<>();
    diffCommand.add("diff");
    diffCommand.add("-rq");
    diffCommand.add(RodaCoreFactory.getDataPath().resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_AIP).toString());
    diffCommand
      .add(testPath.resolve(RodaConstants.CORE_STORAGE_FOLDER).resolve(RodaConstants.STORAGE_CONTAINER_AIP).toString());
    LOGGER.info("Executing diff to compare two folders: {}", diffCommand);
    CommandUtility.execute(diffCommand);
  }

  @Test(enabled = false)
  private void testRsyncViaFilter() throws FileAlreadyExistsException, RequestNotValidException, NotFoundException,
    GenericException, AlreadyExistsException, AuthorizationDeniedException, InvalidParameterException,
    InterruptedException, IOException, SolrServerException, JobAlreadyStartedException, CommandException {
    // increase sync job execution timeout in seconds
    JobsHelper.setSyncTimeout(1200);
    JobsHelper.setBlockSize(100);

    LOGGER.info("Ingesting first pack of corpora...");
    ingestCorpora();
    LOGGER.info("Ingesting second pack of corpora...");
    ingestCorpora();
    LOGGER.info("Rsyncing corpora via filter...");
    executeJobWithReplicationPlugin();

    LOGGER.info("Testing if destination has the number of AIPs that was supposed");
    Assert.assertEquals(FSUtils
      .countPath(testPath.resolve(RodaConstants.CORE_STORAGE_FOLDER).resolve(RodaConstants.STORAGE_CONTAINER_AIP))
      .intValue(), NUMBER_OF_AIPS);

    List<String> diffCommand = new ArrayList<>();
    diffCommand.add("diff");
    diffCommand.add("-rq");
    diffCommand.add(RodaCoreFactory.getDataPath().resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_AIP).toString());
    diffCommand
      .add(testPath.resolve(RodaConstants.CORE_STORAGE_FOLDER).resolve(RodaConstants.STORAGE_CONTAINER_AIP).toString());
    LOGGER.info("Executing diff to compare two folders: {}", diffCommand);
    CommandUtility.execute(diffCommand);
  }

  private void executeJobWithReplicationPlugin(List<String> aipIds)
    throws CommandException, IOException, JobAlreadyStartedException, GenericException, RequestNotValidException,
    NotFoundException, AuthorizationDeniedException {

    String targetUser = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target");
    RodaCoreFactory.getRodaConfiguration().setProperty("core.aip_rsync.target", testPath.toString() + "/");

    Job job = TestsHelper.executeJob(ReplicationPlugin.class, new HashMap<>(), PluginType.MISC,
      SelectedItemsList.create(AIP.class, aipIds));

    TestsHelper.getJobReports(index, job, true);

    RodaCoreFactory.getRodaConfiguration().setProperty("core.aip_rsync.target", targetUser);
  }

  private void executeJobWithReplicationPlugin() throws CommandException, IOException, JobAlreadyStartedException,
    GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    String targetUser = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target");
    RodaCoreFactory.getRodaConfiguration().setProperty("core.aip_rsync.target", testPath.toString());

    TestsHelper.executeJob(ReplicationPlugin.class, PluginType.MISC, SelectedItemsAll.create(AIP.class));
    RodaCoreFactory.getRodaConfiguration().setProperty("core.aip_rsync.target", targetUser);
  }

}
