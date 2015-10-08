package org.roda.action.ingest.bagit.utils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.roda.core.common.RodaConstants;
import org.roda.model.AIP;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.model.utils.ModelUtils;
import org.roda.storage.Binary;
import org.roda.storage.Resource;
import org.roda.storage.StorageServiceException;
import org.roda.storage.fs.FSUtils;

import com.google.common.collect.Sets;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.utilities.SimpleResult;

public class BagitUtils {
  private static final Logger LOGGER = Logger.getLogger(BagitUtils.class);

  public static AIP bagitToAip(Path bagitPath, ModelService model)
    throws BagitNotValidException, IOException, StorageServiceException, ModelServiceException {
    AIP aip = null;
    BagFactory bagFactory = new BagFactory();
    Bag bag = bagFactory.createBag(bagitPath.toFile());
    SimpleResult result = bag.verifyPayloadManifests();
    if (!result.isSuccess()) {
      throw new BagitNotValidException();
    }
    BagInfoTxt bagInfoTxt = bag.getBagInfoTxt();

    Path metadataFile = Files.createTempFile("metadata", ".xml");
    generateMetadataFile(metadataFile, bagInfoTxt);
    Resource descriptiveMetadataResource = FSUtils.convertPathToResource(metadataFile.getParent(), metadataFile);

    aip = model.createAIP(new HashMap<String, Set<String>>());

    model.createDescriptiveMetadata(aip.getId(), "metadata.xml", (Binary) descriptiveMetadataResource, "metadata");

    String representationID = "representation";
    createDirectories(model, aip.getId(), representationID);

    Path tempFolder = Files.createTempDirectory("temp");
    if (bag.getPayload() != null) {
      for (BagFile bagFile : bag.getPayload()) {
        String fileName = bagFile.getFilepath().replace("/", "_");
        File f = new File(tempFolder.toFile(), fileName);
        Files.copy(bagFile.newInputStream(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Binary resource = (Binary) FSUtils.convertPathToResource(f.toPath().getParent(), f.toPath());
        model.createFile(aip.getId(), representationID, fileName, resource);
      }
    }
    FSUtils.deletePath(tempFolder);
    FSUtils.deletePath(metadataFile);
    return model.retrieveAIP(aip.getId());

  }

  private static void createDirectories(ModelService model, String aipId, String representationID)
    throws StorageServiceException {
    model.getStorage().createDirectory(ModelUtils.getRepresentationsPath(aipId), new HashMap<String, Set<String>>());
    model.getStorage().createDirectory(ModelUtils.getRepresentationPath(aipId, representationID),
      getRepresentationMetadata(representationID));
    model.getStorage().createDirectory(ModelUtils.getPreservationPath(aipId), new HashMap<String, Set<String>>());
    model.getStorage().createDirectory(ModelUtils.getPreservationPath(aipId, representationID),
      new HashMap<String, Set<String>>());
  }

  private static Map<String, Set<String>> getRepresentationMetadata(String representationId) {
    SimpleDateFormat iso8601DateFormat = new SimpleDateFormat(RodaConstants.ISO8601);
    String dateString = iso8601DateFormat.format(new Date());
    Map<String, Set<String>> data = new HashMap<String, Set<String>>();
    data.put("active", Sets.newHashSet("true"));
    data.put("date.created", Sets.newHashSet(dateString));
    data.put("date.modified", Sets.newHashSet(dateString));
    data.put("representation.type", Sets.newHashSet(""));
    data.put("representation.content.model", Sets.newHashSet(""));
    data.put("representation.dObject.pid", Sets.newHashSet(""));
    data.put("representation.id", Sets.newHashSet(representationId));
    data.put("representation.label", Sets.newHashSet(""));
    data.put("representation.pid", Sets.newHashSet(""));
    data.put("representation.state", Sets.newHashSet(""));
    data.put("representation.subtype", Sets.newHashSet(""));
    data.put("representation.type", Sets.newHashSet(""));
    data.put("representation.statuses", Sets.newHashSet("original"));
    return data;
  }

  private static void generateMetadataFile(Path metadataFile, BagInfoTxt bagInfoTxt) throws IOException {
    StringWriter sw = new StringWriter();
    sw.append("<metadata>");
    for (Map.Entry<String, String> entry : bagInfoTxt.entrySet()) {
      sw.append("<field name='" + entry.getKey() + "'>" + entry.getValue() + "</field>");
    }
    sw.append("</metadata>");
    Files.write(metadataFile, sw.toString().getBytes("UTF-8"));
  }
}
