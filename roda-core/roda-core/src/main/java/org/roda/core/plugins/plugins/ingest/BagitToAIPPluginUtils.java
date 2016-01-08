/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.model.AIP;
import org.roda.core.model.ModelService;
import org.roda.core.storage.Binary;
import org.roda.core.storage.Resource;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagInfoTxt;

public class BagitToAIPPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(BagitToAIPPluginUtils.class);

  public static AIP bagitToAip(Bag bag, Path bagitPath, ModelService model, String metadataFilename)
    throws BagitNotValidException, IOException, RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException {
    AIP aip = null;

    BagInfoTxt bagInfoTxt = bag.getBagInfoTxt();
    Path metadataFile = Files.createTempFile("metadata", ".xml");
    generateMetadataFile(metadataFile, bagInfoTxt);
    Resource descriptiveMetadataResource = FSUtils.convertPathToResource(metadataFile.getParent(), metadataFile);

    Map<String, Set<String>> metadata = new HashMap<String, Set<String>>();
    if (bag.getBagInfoTxt().get("parent") != null) {
      try {
        model.retrieveAIP(bag.getBagInfoTxt().get("parent"));
        metadata.put(RodaConstants.STORAGE_META_PARENT_ID,
          new HashSet<String>(Arrays.asList(bag.getBagInfoTxt().get("parent"))));
      } catch (RODAException mse) {
        LOGGER.error("Error retrieving AIP", mse);
      }
    }

    aip = model.createAIP(metadata, false, true);

    String representationID = "representation";
    IngestUtils.createDirectories(model, aip.getId(), representationID);

    model.createDescriptiveMetadata(aip.getId(), metadataFilename, (Binary) descriptiveMetadataResource, "metadata");

    Path tempFolder = Files.createTempDirectory("temp");
    if (bag.getPayload() != null) {
      for (BagFile bagFile : bag.getPayload()) {
        // FIXME this is being done because we don't support folders in a
        // representation
        String fileName = bagFile.getFilepath().replace("/", "_");
        File f = new File(tempFolder.toFile(), fileName);
        Files.copy(bagFile.newInputStream(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Binary resource = (Binary) FSUtils.convertPathToResource(f.toPath().getParent(), f.toPath());
        model.createFile(aip.getId(), representationID, fileName, resource);
      }
    }
    bag.close();
    FSUtils.deletePath(tempFolder);
    FSUtils.deletePath(metadataFile);
    return model.retrieveAIP(aip.getId());

  }

  private static void generateMetadataFile(Path metadataFile, BagInfoTxt bagInfoTxt) throws IOException {
    StringWriter sw = new StringWriter();
    sw.append("<metadata>");
    for (Map.Entry<String, String> entry : bagInfoTxt.entrySet()) {
      if (!entry.getKey().equalsIgnoreCase("parent")) {
        sw.append("<field name='" + entry.getKey() + "'>" + StringEscapeUtils.escapeXml(entry.getValue()) + "</field>");
      }
    }
    sw.append("</metadata>");
    Files.write(metadataFile, sw.toString().getBytes("UTF-8"));
  }
}
