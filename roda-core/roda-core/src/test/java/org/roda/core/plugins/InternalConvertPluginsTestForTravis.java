/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.IdUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.monitor.FolderMonitorNIO;
import org.roda.core.common.monitor.FolderObserver;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.plugins.ingest.TransferredResourceToAIPPlugin;
import org.roda.core.plugins.plugins.ingest.migration.AvconvConvertPlugin;
import org.roda.core.plugins.plugins.ingest.migration.GeneralCommandConvertPlugin;
import org.roda.core.plugins.plugins.ingest.migration.GhostScriptConvertPlugin;
import org.roda.core.plugins.plugins.ingest.migration.ImageMagickConvertPlugin;
import org.roda.core.plugins.plugins.ingest.migration.PdfToPdfaPlugin;
import org.roda.core.plugins.plugins.ingest.migration.SoxConvertPlugin;
import org.roda.core.plugins.plugins.ingest.migration.UnoconvConvertPlugin;
import org.roda.core.plugins.plugins.ingest.validation.DigitalSignatureDIPPlugin;
import org.roda.core.plugins.plugins.ingest.validation.DigitalSignatureDIPPluginUtils;
import org.roda.core.plugins.plugins.ingest.validation.DigitalSignaturePlugin;
import org.roda.core.plugins.plugins.ingest.validation.DigitalSignaturePluginUtils;
import org.roda.core.plugins.plugins.ingest.validation.VeraPDFPlugin;
import org.roda.core.storage.Binary;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalConvertPluginsTestForTravis {
  private static final Logger LOGGER = LoggerFactory.getLogger(InternalConvertPluginsTestForTravis.class);

  private static final int AUTO_COMMIT_TIMEOUT = 2000;
  private static Path basePath;
  private static ModelService model;
  private static IndexService index;
  private static int numberOfConvertableFiles = 17;
  private static Path corporaPath;

  @Before
  public void setUp() throws Exception {

    basePath = Files.createTempDirectory("indexTests");
    System.setProperty("roda.home", basePath.toString());

    boolean deploySolr = true;
    boolean deployLdap = false;
    boolean deployFolderMonitor = true;
    boolean deployOrchestrator = true;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator);
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    URL corporaURL = InternalConvertPluginsTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());

    LOGGER.info("Running internal convert plugins tests under storage {}", basePath);
  }

  @After
  public void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  private List<TransferredResource> createCorpora(int corporaId) throws InterruptedException, IOException,
    FileAlreadyExistsException, NotFoundException, GenericException, AlreadyExistsException {
    FolderMonitorNIO f = RodaCoreFactory.getFolderMonitor();

    FolderObserver observer = Mockito.mock(FolderObserver.class);
    f.addFolderObserver(observer);

    while (!f.isFullyInitialized()) {
      LOGGER.info("Waiting for folder monitor to initialize...");
      Thread.sleep(1000);
    }

    Assert.assertTrue(f.isFullyInitialized());

    List<TransferredResource> resources = new ArrayList<TransferredResource>();

    String[] aips = {CorporaConstants.SOURCE_AIP_CONVERTER_1, CorporaConstants.SOURCE_AIP_CONVERTER_2,
      CorporaConstants.SOURCE_AIP_CONVERTER_3};

    String[] reps = {CorporaConstants.REPRESENTATION_CONVERTER_ID_1, CorporaConstants.REPRESENTATION_CONVERTER_ID_2,
      CorporaConstants.REPRESENTATION_CONVERTER_ID_3};

    Path corpora = corporaPath.resolve(RodaConstants.STORAGE_CONTAINER_AIP).resolve(aips[corporaId])
      .resolve(RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS).resolve(reps[corporaId])
      .resolve(RodaConstants.STORAGE_DIRECTORY_DATA);

    FSUtils.copy(corpora, f.getBasePath().resolve("testt"), true);

    LOGGER.info("Waiting for soft-commit");
    Thread.sleep(AUTO_COMMIT_TIMEOUT);

    resources.add(index.retrieve(TransferredResource.class, "testt"));
    return resources;
  }

  private AIP ingestCorpora(int corporaId) throws RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, InvalidParameterException, InterruptedException, IOException,
    FileAlreadyExistsException {
    AIP root = model.createAIP(null);

    Plugin<TransferredResource> plugin = new TransferredResourceToAIPPlugin();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, root.getId());
    plugin.setParameterValues(parameters);

    List<TransferredResource> transferredResources = new ArrayList<TransferredResource>();
    transferredResources = createCorpora(corporaId);

    Assert.assertEquals(1, transferredResources.size());
    RodaCoreFactory.getPluginOrchestrator().runPluginOnTransferredResources(plugin, transferredResources);

    IndexResult<IndexedAIP> find = index.find(IndexedAIP.class, new Filter(new SimpleFilterParameter(
      RodaConstants.AIP_PARENT_ID, root.getId())), null, new Sublist(0, 10));

    Assert.assertEquals(1L, find.getTotalCount());
    IndexedAIP indexedAIP = find.getResults().get(0);

    AIP aip = model.retrieveAIP(indexedAIP.getId());
    return aip;
  }

  @Test
  public void testIngestTransferredResource() throws IOException, InterruptedException, RODAException {
    AIP aip = ingestCorpora(0);
    Assert.assertEquals(1, aip.getRepresentations().size());

    CloseableIterable<File> allFiles = model.listFilesUnder(aip.getId(), aip.getRepresentations().get(0).getId(), true);
    List<File> reusableAllFiles = new ArrayList<>();
    Iterables.addAll(reusableAllFiles, allFiles);

    Assert.assertEquals(numberOfConvertableFiles, reusableAllFiles.size());
  }

  @Test
  public void testImageMagickPlugin() throws RODAException, FileAlreadyExistsException, InterruptedException,
    IOException {
    AIP aip = ingestCorpora(0);

    CloseableIterable<File> allFiles = model.listFilesUnder(aip.getId(), aip.getRepresentations().get(0).getId(), true);
    List<File> reusableAllFiles = new ArrayList<>();
    Iterables.addAll(reusableAllFiles, allFiles);

    Plugin<Representation> plugin = new ImageMagickConvertPlugin<Representation>();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, "NONE");
    parameters.put("outputFormat", "tiff");
    plugin.setParameterValues(parameters);

    RodaCoreFactory.getPluginOrchestrator().runPluginOnAllRepresentations(plugin);

    aip = model.retrieveAIP(aip.getId());
    Assert.assertEquals(2, aip.getRepresentations().size());

    CloseableIterable<File> newAllFiles = model.listFilesUnder(aip.getId(), aip.getRepresentations().get(1).getId(),
      true);
    List<File> newReusableAllFiles = new ArrayList<>();
    Iterables.addAll(newReusableAllFiles, newAllFiles);

    Assert.assertEquals(numberOfConvertableFiles, newReusableAllFiles.size());

    int changedCounter = 0;

    for (File f : reusableAllFiles) {
      if (f.getId().matches(".*[.](jpg|png)$")) {
        changedCounter++;
        String filename = f.getId().substring(0, f.getId().lastIndexOf('.'));
        Assert.assertEquals(1, newReusableAllFiles.stream().filter(o -> o.getId().equals(filename + ".tiff")).count());
      }
    }

    List<File> changedFiles = newReusableAllFiles.stream().filter(o -> o.getId().matches(".*[.]tiff$"))
      .collect(Collectors.toList());

    Assert.assertEquals(changedCounter, changedFiles.size());

    for (File file : changedFiles) {
      IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
      String fileMimetype = ifile.getFileFormat().getMimeType();
      Assert.assertTrue(ifile.getSize() > 0);
      Assert.assertEquals("image/tiff", fileMimetype);
    }
  }

  @Test
  public void testSoxPlugin() throws RODAException, FileAlreadyExistsException, InterruptedException, IOException {
    AIP aip = ingestCorpora(0);

    CloseableIterable<File> allFiles = model.listFilesUnder(aip.getId(), aip.getRepresentations().get(0).getId(), true);
    List<File> reusableAllFiles = new ArrayList<>();
    Iterables.addAll(reusableAllFiles, allFiles);

    Plugin<Representation> plugin = new SoxConvertPlugin<Representation>();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, "NONE");
    parameters.put("outputFormat", "ogg");
    plugin.setParameterValues(parameters);

    RodaCoreFactory.getPluginOrchestrator().runPluginOnAllRepresentations(plugin);

    aip = model.retrieveAIP(aip.getId());
    Assert.assertEquals(2, aip.getRepresentations().size());

    CloseableIterable<File> newAllFiles = model.listFilesUnder(aip.getId(), aip.getRepresentations().get(1).getId(),
      true);
    List<File> newReusableAllFiles = new ArrayList<>();
    Iterables.addAll(newReusableAllFiles, newAllFiles);

    Assert.assertEquals(numberOfConvertableFiles, newReusableAllFiles.size());

    int changedCounter = 0;

    for (File f : reusableAllFiles) {
      if (f.getId().matches(".*[.](mp3)$")) {
        changedCounter++;
        String filename = f.getId().substring(0, f.getId().lastIndexOf('.'));
        Assert.assertEquals(1, newReusableAllFiles.stream().filter(o -> o.getId().equals(filename + ".ogg")).count());
      }
    }

    List<File> changedFiles = newReusableAllFiles.stream().filter(o -> o.getId().matches(".*[.]ogg$"))
      .collect(Collectors.toList());

    Assert.assertEquals(changedCounter, changedFiles.size());

    for (File file : changedFiles) {
      IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
      String fileMimetype = ifile.getFileFormat().getMimeType();
      Assert.assertTrue(ifile.getSize() > 0);
      Assert.assertEquals("audio/ogg", fileMimetype);
    }
  }

  @Test
  public void testAvconvPlugin() throws RODAException, FileAlreadyExistsException, InterruptedException, IOException {
    AIP aip = ingestCorpora(0);

    CloseableIterable<File> allFiles = model.listFilesUnder(aip.getId(), aip.getRepresentations().get(0).getId(), true);
    List<File> reusableAllFiles = new ArrayList<>();
    Iterables.addAll(reusableAllFiles, allFiles);

    Plugin<Representation> plugin = new AvconvConvertPlugin<Representation>();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, "NONE");
    parameters.put("outputFormat", "gif");
    parameters.put("outputArguments", "-pix_fmt rgb24");
    plugin.setParameterValues(parameters);

    RodaCoreFactory.getPluginOrchestrator().runPluginOnAllRepresentations(plugin);

    aip = model.retrieveAIP(aip.getId());
    Assert.assertEquals(2, aip.getRepresentations().size());

    CloseableIterable<File> newAllFiles = model.listFilesUnder(aip.getId(), aip.getRepresentations().get(1).getId(),
      true);
    List<File> newReusableAllFiles = new ArrayList<>();
    Iterables.addAll(newReusableAllFiles, newAllFiles);

    Assert.assertEquals(numberOfConvertableFiles, newReusableAllFiles.size());

    int changedCounter = 0;

    for (File f : reusableAllFiles) {
      if (f.getId().matches(".*[.](3g2|avi)$")) {
        changedCounter++;
        String filename = f.getId().substring(0, f.getId().lastIndexOf('.'));
        Assert.assertEquals(1, newReusableAllFiles.stream().filter(o -> o.getId().equals(filename + ".gif")).count());
      }
    }

    List<File> changedFiles = newReusableAllFiles.stream().filter(o -> o.getId().matches(".*[.]gif$"))
      .collect(Collectors.toList());

    Assert.assertEquals(changedCounter, changedFiles.size());

    for (File file : changedFiles) {
      IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
      String fileMimetype = ifile.getFileFormat().getMimeType();
      Assert.assertTrue(ifile.getSize() > 0);
      Assert.assertEquals("image/gif", fileMimetype);
    }
  }

  @Ignore
  @Test
  public void testUnoconvPlugin() throws RODAException, FileAlreadyExistsException, InterruptedException, IOException {
    AIP aip = ingestCorpora(0);

    CloseableIterable<File> allFiles = model.listFilesUnder(aip.getId(), aip.getRepresentations().get(0).getId(), true);
    List<File> reusableAllFiles = new ArrayList<>();
    Iterables.addAll(reusableAllFiles, allFiles);

    Plugin<Representation> plugin = new UnoconvConvertPlugin<Representation>();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, "NONE");
    parameters.put("outputFormat", "pdf");
    plugin.setParameterValues(parameters);

    RodaCoreFactory.getPluginOrchestrator().runPluginOnAllRepresentations(plugin);

    aip = model.retrieveAIP(aip.getId());
    Assert.assertEquals(2, aip.getRepresentations().size());

    CloseableIterable<File> newAllFiles = model.listFilesUnder(aip.getId(), aip.getRepresentations().get(1).getId(),
      true);
    List<File> newReusableAllFiles = new ArrayList<>();
    Iterables.addAll(newReusableAllFiles, newAllFiles);

    Assert.assertEquals(numberOfConvertableFiles, newReusableAllFiles.size());

    int changedCounter = 0;

    for (File f : reusableAllFiles) {
      if (f.getId().matches(".*[.](pdf|docx|txt|xls|odp|ppt|pptx|doc|rtf|xlsx|ods|odt|xml)$")) {
        changedCounter++;
        String filename = f.getId().substring(0, f.getId().lastIndexOf('.'));
        Assert.assertEquals(1, newReusableAllFiles.stream().filter(o -> o.getId().equals(filename + ".pdf")).count());
      }
    }

    List<File> changedFiles = newReusableAllFiles.stream().filter(o -> o.getId().matches(".*[.]pdf$"))
      .collect(Collectors.toList());

    Assert.assertEquals(changedCounter, changedFiles.size());

    for (File file : changedFiles) {
      IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
      String fileMimetype = ifile.getFileFormat().getMimeType();
      Assert.assertEquals("application/pdf", fileMimetype);
    }
  }

  @Test
  public void testGhostScriptPlugin() throws RODAException, FileAlreadyExistsException, InterruptedException,
    IOException {
    AIP aip = ingestCorpora(0);

    CloseableIterable<File> allFiles = model.listFilesUnder(aip.getId(), aip.getRepresentations().get(0).getId(), true);
    List<File> reusableAllFiles = new ArrayList<>();
    Iterables.addAll(reusableAllFiles, allFiles);

    Plugin<Representation> plugin = new GhostScriptConvertPlugin<Representation>();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, "NONE");
    parameters.put("outputFormat", "pdf");
    parameters.put("commandArguments", "-sDEVICE=pdfwrite");
    plugin.setParameterValues(parameters);

    RodaCoreFactory.getPluginOrchestrator().runPluginOnAllRepresentations(plugin);

    aip = model.retrieveAIP(aip.getId());
    Assert.assertEquals(2, aip.getRepresentations().size());

    CloseableIterable<File> newAllFiles = model.listFilesUnder(aip.getId(), aip.getRepresentations().get(1).getId(),
      true);
    List<File> newReusableAllFiles = new ArrayList<>();
    Iterables.addAll(newReusableAllFiles, newAllFiles);

    Assert.assertEquals(numberOfConvertableFiles, newReusableAllFiles.size());

    int changedCounter = 0;

    for (File f : reusableAllFiles) {
      if (f.getId().matches(".*[.](pdf)$")) {
        changedCounter++;
        String filename = f.getId().substring(0, f.getId().lastIndexOf('.'));
        Assert.assertEquals(1, newReusableAllFiles.stream().filter(o -> o.getId().equals(filename + ".pdf")).count());
      }
    }

    List<File> changedFiles = newReusableAllFiles.stream().filter(o -> o.getId().matches(".*[.]pdf$"))
      .collect(Collectors.toList());

    Assert.assertEquals(changedCounter, changedFiles.size());

    for (File file : changedFiles) {
      IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
      String fileMimetype = ifile.getFileFormat().getMimeType();
      Assert.assertTrue(ifile.getSize() > 0);
      Assert.assertEquals("application/pdf", fileMimetype);
    }
  }

  @Test
  public void testPdfToPdfaPlugin() throws RODAException, FileAlreadyExistsException, InterruptedException, IOException {
    AIP aip = ingestCorpora(0);

    CloseableIterable<File> allFiles = model.listFilesUnder(aip.getId(), aip.getRepresentations().get(0).getId(), true);
    List<File> reusableAllFiles = new ArrayList<>();
    Iterables.addAll(reusableAllFiles, allFiles);

    Plugin<Representation> plugin = new PdfToPdfaPlugin<Representation>();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, "NONE");
    parameters.put("outputFormat", "pdf");
    plugin.setParameterValues(parameters);

    RodaCoreFactory.getPluginOrchestrator().runPluginOnAllRepresentations(plugin);
    aip = model.retrieveAIP(aip.getId());
    Assert.assertEquals(2, aip.getRepresentations().size());

    CloseableIterable<File> newAllFiles = model.listFilesUnder(aip.getId(), aip.getRepresentations().get(1).getId(),
      true);
    List<File> newReusableAllFiles = new ArrayList<>();
    Iterables.addAll(newReusableAllFiles, newAllFiles);
    Assert.assertEquals(numberOfConvertableFiles, newReusableAllFiles.size());

    for (File f : reusableAllFiles) {
      if (f.getId().matches(".*[.](pdf)$")) {
        String filename = f.getId().substring(0, f.getId().lastIndexOf('.'));
        Assert.assertEquals(1, newReusableAllFiles.stream().filter(o -> o.getId().equals(filename + ".pdf")).count());
      }
    }

    List<File> changedFiles = newReusableAllFiles.stream().filter(o -> o.getId().matches(".*[.]pdf$"))
      .collect(Collectors.toList());
    for (File file : changedFiles) {
      IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
      String filePronom = ifile.getFileFormat().getPronom();
      Assert.assertTrue(ifile.getSize() > 0);
      Assert.assertEquals("fmt/354", filePronom);
    }

  }

  @Test
  public void testMultipleRepresentations() throws FileAlreadyExistsException, RequestNotValidException,
    NotFoundException, GenericException, AlreadyExistsException, AuthorizationDeniedException,
    InvalidParameterException, InterruptedException, IOException {

    AIP aip = ingestCorpora(0);

    CloseableIterable<File> allFiles = model.listFilesUnder(aip.getId(), aip.getRepresentations().get(0).getId(), true);
    List<File> reusableAllFiles = new ArrayList<>();
    Iterables.addAll(reusableAllFiles, allFiles);

    Plugin<Representation> plugin = new SoxConvertPlugin<Representation>();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, "NONE");
    parameters.put("outputFormat", "ogg");
    plugin.setParameterValues(parameters);

    RodaCoreFactory.getPluginOrchestrator().runPluginOnAllRepresentations(plugin);
    aip = model.retrieveAIP(aip.getId());
    Assert.assertEquals(2, aip.getRepresentations().size());
    String editedRepresentationId = aip.getRepresentations().get(1).getId();

    Plugin<Representation> plugin2 = new ImageMagickConvertPlugin<Representation>();
    Map<String, String> parameters2 = new HashMap<>();
    parameters2.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, "NONE");
    parameters2.put("outputFormat", "tiff");
    plugin2.setParameterValues(parameters2);

    RodaCoreFactory.getPluginOrchestrator().runPluginOnAllRepresentations(plugin2);
    aip = model.retrieveAIP(aip.getId());
    Assert.assertEquals(4, aip.getRepresentations().size());

    Assert.assertEquals(1, aip.getRepresentations().stream().filter(o -> o.getId().equals(editedRepresentationId))
      .count());

    CloseableIterable<File> newAllFiles = model.listFilesUnder(aip.getId(), aip.getRepresentations().get(3).getId(),
      true);
    List<File> newReusableAllFiles = new ArrayList<>();
    Iterables.addAll(newReusableAllFiles, newAllFiles);

    Assert.assertEquals(numberOfConvertableFiles, newReusableAllFiles.size());

    int changedCounter = 0;

    for (File f : reusableAllFiles) {
      if (f.getId().matches(".*[.](jpg|png|mp3)$")) {
        changedCounter++;
        String filename = f.getId().substring(0, f.getId().lastIndexOf('.'));
        Assert.assertEquals(
          1,
          newReusableAllFiles.stream()
            .filter(o -> o.getId().equals(filename + ".tiff") || o.getId().equals(filename + ".ogg")).count());
      }
    }

    Assert.assertEquals(changedCounter, newReusableAllFiles.stream().filter(o -> o.getId().matches(".*[.](tiff|ogg)$"))
      .count());

  }

  @Test
  public void testGeneralCommandPlugin() throws RODAException, FileAlreadyExistsException, InterruptedException,
    IOException {
    AIP aip = ingestCorpora(0);

    CloseableIterable<File> allFiles = model.listFilesUnder(aip.getId(), aip.getRepresentations().get(0).getId(), true);
    List<File> reusableAllFiles = new ArrayList<>();
    Iterables.addAll(reusableAllFiles, allFiles);

    Plugin<Representation> plugin = new GeneralCommandConvertPlugin<Representation>();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, "NONE");
    parameters.put("inputFormat", "png");
    parameters.put("outputFormat", "tiff");
    parameters.put("commandArguments", "/usr/bin/convert -regard-warnings {input_file} {output_file}");
    plugin.setParameterValues(parameters);

    RodaCoreFactory.getPluginOrchestrator().runPluginOnAllRepresentations(plugin);
    aip = model.retrieveAIP(aip.getId());
    Assert.assertEquals(2, aip.getRepresentations().size());

    CloseableIterable<File> newAllFiles = model.listFilesUnder(aip.getId(), aip.getRepresentations().get(1).getId(),
      true);
    List<File> newReusableAllFiles = new ArrayList<>();
    Iterables.addAll(newReusableAllFiles, newAllFiles);

    Assert.assertEquals(numberOfConvertableFiles, newReusableAllFiles.size());

    int changedCounter = 0;

    for (File f : reusableAllFiles) {
      if (f.getId().matches(".*[.](png)$")) {
        changedCounter++;
        String filename = f.getId().substring(0, f.getId().lastIndexOf('.'));
        Assert.assertEquals(1, newReusableAllFiles.stream().filter(o -> o.getId().equals(filename + ".tiff")).count());
      }
    }

    Assert.assertEquals(changedCounter, newReusableAllFiles.stream().filter(o -> o.getId().matches(".*[.]tiff$"))
      .count());
  }

  @Test
  public void testDigitalSignaturePlugin() throws RODAException, FileAlreadyExistsException, InterruptedException,
    IOException, NoSuchAlgorithmException {
    AIP aip = ingestCorpora(1);

    CloseableIterable<File> allFiles = model.listFilesUnder(aip.getId(), aip.getRepresentations().get(0).getId(), true);
    List<File> reusableAllFiles = new ArrayList<>();
    Iterables.addAll(reusableAllFiles, allFiles);

    Plugin<Representation> plugin = new DigitalSignaturePlugin();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, "NONE");
    parameters.put("doVerify", "True");
    parameters.put("doExtract", "True");
    parameters.put("doStrip", "True");
    plugin.setParameterValues(parameters);

    RodaCoreFactory.getPluginOrchestrator().runPluginOnAllRepresentations(plugin);

    aip = model.retrieveAIP(aip.getId());
    Assert.assertEquals(2, aip.getRepresentations().size());

    CloseableIterable<File> newFiles = model.listFilesUnder(aip.getId(), aip.getRepresentations().get(1).getId(), true);
    List<File> newReusableFiles = new ArrayList<>();
    Iterables.addAll(newReusableFiles, newFiles);

    for (File f : reusableAllFiles) {
      if (f.getId().matches(".*[.](pdf)$")) {
        String filename = f.getId().substring(0, f.getId().lastIndexOf('.'));
        Assert.assertEquals(1, newReusableFiles.stream().filter(o -> o.getId().equals(f.getId())).count());

        Binary binary = model.retrieveOtherMetadataBinary(aip.getId(), f.getRepresentationId(), f.getPath(), filename,
          ".txt", "DigitalSignature");

        Assert.assertTrue(binary.getSizeInBytes() > 0);

      }
    }

    for (File file : newReusableFiles) {
      IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
      String fileMimetype = ifile.getFileFormat().getMimeType();
      Assert.assertTrue(ifile.getSize() > 0);
      Assert.assertTrue(fileMimetype.equals("application/pdf"));

      StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
      String intermediatePath = "/data/storage/";
      Assert.assertEquals(0,
        DigitalSignaturePluginUtils.countSignaturesPDF(basePath, fileStoragePath, intermediatePath));
    }
  }

  @Ignore
  @Test
  public void testDigitalSignatureDIPPlugin() throws RODAException, FileAlreadyExistsException, InterruptedException,
    IOException {
    AIP aip = ingestCorpora(2);
    CloseableIterable<File> allFiles = model.listFilesUnder(aip.getId(), aip.getRepresentations().get(0).getId(), true);
    File file = allFiles.iterator().next();

    StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
    String intermediatePath = "/data/storage/";
    Assert.assertEquals(0, DigitalSignaturePluginUtils.countSignaturesPDF(basePath, fileStoragePath, intermediatePath));

    Plugin<Representation> plugin = new DigitalSignatureDIPPlugin();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, "NONE");
    plugin.setParameterValues(parameters);

    DigitalSignatureDIPPluginUtils.setKeystorePath(corporaPath.toString() + "/Certification/keystore.jks");
    RodaCoreFactory.getPluginOrchestrator().runPluginOnAllRepresentations(plugin);

    aip = model.retrieveAIP(aip.getId());
    CloseableIterable<File> allNewFiles = model.listFilesUnder(aip.getId(), aip.getRepresentations().get(1).getId(),
      true);
    File newFile = allNewFiles.iterator().next();

    StoragePath newFileStoragePath = ModelUtils.getFileStoragePath(newFile);
    Assert.assertEquals(1,
      DigitalSignaturePluginUtils.countSignaturesPDF(basePath, newFileStoragePath, intermediatePath));
  }

  @Ignore
  @Test
  public void testVeraPDFPlugin() throws RODAException, FileAlreadyExistsException, InterruptedException, IOException {
    ingestCorpora(2);

    Plugin<AIP> plugin = new VeraPDFPlugin();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, "NONE");
    parameters.put("profile", "1b");
    parameters.put("hasFeatures", "False");
    plugin.setParameterValues(parameters);

    List<Report> reports = RodaCoreFactory.getPluginOrchestrator().runPluginOnAllAIPs(plugin);
    Assert.assertEquals("PARTIAL_SUCCESS", reports.get(0).getReports().get(0).getPluginState().toString());

    Plugin<AIP> plugin2 = new PdfToPdfaPlugin<AIP>();
    RodaCoreFactory.getPluginOrchestrator().runPluginOnAllAIPs(plugin2);

    Plugin<AIP> plugin3 = new VeraPDFPlugin();
    plugin3.setParameterValues(parameters);
    reports = RodaCoreFactory.getPluginOrchestrator().runPluginOnAllAIPs(plugin3);
    Assert.assertEquals("SUCCESS", reports.get(0).getReports().get(1).getPluginState().toString());

  }
}
