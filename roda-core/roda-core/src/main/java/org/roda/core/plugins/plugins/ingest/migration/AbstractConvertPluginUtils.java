package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.tika.exception.TikaException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.roda.core.common.PremisUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.metadata.PremisMetadataException;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginOrchestrator;
import org.roda.core.plugins.orchestrate.AkkaEmbeddedPluginOrchestrator;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.ingest.characterization.PremisSkeletonPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.SiegfriedPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.SiegfriedPluginUtils;
import org.roda.core.plugins.plugins.ingest.characterization.TikaFullTextPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.TikaFullTextPluginUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ClosableIterable;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.xml.sax.SAXException;

public class AbstractConvertPluginUtils {

  public static void runReindexingPlugins(String aipId) throws InvalidParameterException {
    // TODO change to execute on the AIP with the new representation
    Plugin<AIP> psp = new PremisSkeletonPlugin();
    Plugin<AIP> sfp = new SiegfriedPlugin();
    Plugin<AIP> ttp = new TikaFullTextPlugin();

    Map<String, String> params = new HashMap<String, String>();
    params.put("createsPluginEvent", "false");
    psp.setParameterValues(params);
    sfp.setParameterValues(params);
    ttp.setParameterValues(params);

    PluginOrchestrator pluginOrchestrator = new AkkaEmbeddedPluginOrchestrator();
    pluginOrchestrator.runPluginOnAIPs(psp, Arrays.asList(aipId));
    pluginOrchestrator.runPluginOnAIPs(sfp, Arrays.asList(aipId));
    pluginOrchestrator.runPluginOnAIPs(ttp, Arrays.asList(aipId));
  }

  public static void reIndexingRepresentation(IndexService index, ModelService model, StorageService storage,
    String aipId, String representationId) throws IOException, PremisMetadataException, RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, PluginException, AlreadyExistsException,
    SAXException, TikaException {

    runPremisSkeleton(index, model, storage, aipId, representationId);
    runSiegfried(index, model, storage, aipId, representationId);
    runTIKA(index, model, storage, aipId, representationId);
  }

  private static void runPremisSkeleton(IndexService index, ModelService model, StorageService storage, String aipId,
    String representationId) throws IOException, PremisMetadataException, RequestNotValidException, GenericException,
    NotFoundException, AuthorizationDeniedException, PluginException, AlreadyExistsException, SAXException,
    TikaException {

    ContentPayload representationPremis = PremisUtils.createBaseRepresentation(representationId);
    model.createPreservationMetadata(PreservationMetadataType.OBJECT_REPRESENTATION, aipId, representationId,
      representationId, representationPremis);
    ClosableIterable<File> allFiles = model.listAllFiles(aipId, representationId);
    for (File file : allFiles) {
      ContentPayload filePreservation = PremisUtils.createBaseFile(file, model);
      model.createPreservationMetadata(PreservationMetadataType.OBJECT_FILE, aipId, representationId, file.getId(),
        filePreservation);
    }
    IOUtils.closeQuietly(allFiles);
  }

  private static void runSiegfried(IndexService index, ModelService model, StorageService storage, String aipId,
    String representationId) throws IOException, PremisMetadataException, RequestNotValidException, GenericException,
    NotFoundException, AuthorizationDeniedException, PluginException, AlreadyExistsException, SAXException,
    TikaException {

    IndexedPreservationAgent agent = PremisUtils.createPremisAgentBinary(new SiegfriedPlugin(),
      RodaConstants.PRESERVATION_AGENT_TYPE_CHARACTERIZATION_PLUGIN, model);

    Path data = Files.createTempDirectory("data");
    StorageService tempStorage = new FileStorageService(data);
    StoragePath representationPath = ModelUtils.getRepresentationPath(aipId, representationId);
    tempStorage.copy(storage, representationPath, representationPath);
    String siegfriedOutput = SiegfriedPluginUtils.runSiegfriedOnPath(data.resolve(representationPath.asString()));

    final JSONObject obj = new JSONObject(siegfriedOutput);
    JSONArray files = (JSONArray) obj.get("files");
    List<IndexedFile> updatedFiles = new ArrayList<IndexedFile>();
    for (int i = 0; i < files.length(); i++) {
      JSONObject fileObject = files.getJSONObject(i);

      String fileName = fileObject.getString("filename");
      fileName = fileName.substring(fileName.lastIndexOf(java.io.File.separatorChar) + 1);
      long fileSize = fileObject.getLong("filesize");

      Path p = Files.createTempFile("temp", ".temp");
      Files.write(p, fileObject.toString().getBytes());
      Binary resource = (Binary) FSUtils.convertPathToResource(p.getParent(), p);

      model.createOtherMetadata(aipId, representationId, fileName + ".json", "Siegfried", resource);

      p.toFile().delete();

      JSONArray matches = (JSONArray) fileObject.get("matches");
      if (matches.length() > 0) {
        for (int j = 0; j < matches.length(); j++) {
          JSONObject match = (JSONObject) matches.get(j);
          if (match.getString("id").equalsIgnoreCase("pronom")) {
            String format = match.getString("format");
            String pronom = match.getString("puid");
            String mime = match.getString("mime");
            String version = match.getString("version");
            String extension = "";
            if (fileName.contains(".")) {
              extension = fileName.substring(fileName.lastIndexOf('.'));
            }
            IndexedFile f = index.retrieve(IndexedFile.class, SolrUtils.getId(aipId, representationId, fileName));
            FileFormat ff = new FileFormat();
            ff.setFormatDesignationName(format);
            ff.setFormatDesignationVersion(version);
            ff.setPronom(pronom);
            ff.setMimeType(mime);
            ff.setExtension(extension);
            f.setFileFormat(ff);
            f.setSize(fileSize);
            f.setOriginalName(fileName);
            updatedFiles.add(f);
          }
        }
      }
    }
    model.updateFileFormats(updatedFiles);

    try {
      PluginHelper.createPluginEvent(aipId, representationId, null, model,
        RodaConstants.PRESERVATION_EVENT_TYPE_FORMAT_IDENTIFICATION,
        "The files of the representation were successfully identified.", Arrays.asList(representationId), null,
        "success", "", siegfriedOutput, agent);
    } catch (IOException | RODAException e) {
      throw new PluginException(e.getMessage(), e);
    }

    FSUtils.deletePath(data);
  }

  private static void runTIKA(IndexService index, ModelService model, StorageService storage, String aipId,
    String representationId) throws IOException, PremisMetadataException, RequestNotValidException, GenericException,
    NotFoundException, AuthorizationDeniedException, PluginException, AlreadyExistsException, SAXException,
    TikaException {

    ClosableIterable<File> allFiles = model.listAllFiles(aipId, representationId);
    List<IndexedFile> updatedFiles = new ArrayList<IndexedFile>();
    for (File file : allFiles) {

      if (!file.isDirectory()) {
        StoragePath storagePath = ModelUtils.getRepresentationFileStoragePath(file);
        Binary binary = storage.getBinary(storagePath);

        // FIXME file that doesn't get deleted afterwards
        Path tikaResult = TikaFullTextPluginUtils.extractMetadata(binary.getContent().createInputStream());

        Binary resource = (Binary) FSUtils.convertPathToResource(tikaResult.getParent(), tikaResult);
        model.createOtherMetadata(aipId, representationId, file.getId() + ".html", "ApacheTika", resource);
        try {
          IndexedFile f = index.retrieve(IndexedFile.class, SolrUtils.getId(aipId, representationId, file.getId()));

          Map<String, String> properties = TikaFullTextPluginUtils.extractPropertiesFromResult(tikaResult);
          if (properties.containsKey(RodaConstants.FILE_FULLTEXT)) {
            f.setFulltext(properties.get(RodaConstants.FILE_FULLTEXT));
          }
          if (properties.containsKey(RodaConstants.FILE_CREATING_APPLICATION_NAME)) {
            f.setCreatingApplicationName(properties.get(RodaConstants.FILE_CREATING_APPLICATION_NAME));
          }
          if (properties.containsKey(RodaConstants.FILE_CREATING_APPLICATION_VERSION)) {
            f.setCreatingApplicationVersion(properties.get(RodaConstants.FILE_CREATING_APPLICATION_VERSION));
          }
          if (properties.containsKey(RodaConstants.FILE_DATE_CREATED_BY_APPLICATION)) {
            f.setDateCreatedByApplication(properties.get(RodaConstants.FILE_DATE_CREATED_BY_APPLICATION));
          }

          updatedFiles.add(f);
        } catch (ParserConfigurationException pce) {

        }
      }
    }
    IOUtils.closeQuietly(allFiles);
    model.updateFileFormats(updatedFiles);
  }
}
