/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPPermissions;
import org.roda.core.model.ModelService;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StringContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagInfoTxt;

public class BagitToAIPPluginUtils {
  private static final String DATA_FOLDER = "data";
  private static final String METADATA_TYPE = "key-value";
  private static final String METADATA_VERSION = null;
  private static final String BAGIT_FILE_PATH_SEPARATOR = "/";
  private static final Logger LOGGER = LoggerFactory.getLogger(BagitToAIPPluginUtils.class);

  public static AIP bagitToAip(Bag bag, Path bagitPath, ModelService model, String metadataFilename, String parentId)
    throws BagitNotValidException, IOException, RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException {

    BagInfoTxt bagInfoTxt = bag.getBagInfoTxt();
    String metadataAsString = generateMetadataFile(bagInfoTxt);
    ContentPayload metadataAsPayload = new StringContentPayload(metadataAsString);

    boolean active = false;
    AIPPermissions permissions = new AIPPermissions();

    boolean notifyInSteps = false;

    AIP aip = model.createAIP(active, parentId, permissions, notifyInSteps);

    model.createDescriptiveMetadata(aip.getId(), metadataFilename, metadataAsPayload, METADATA_TYPE, METADATA_VERSION);

    String representationId = UUID.randomUUID().toString();
    boolean original = true;

    model.createRepresentation(aip.getId(), representationId, original, notifyInSteps);

    if (bag.getPayload() != null) {
      for (BagFile bagFile : bag.getPayload()) {
        List<String> split = Arrays.asList(bagFile.getFilepath().split(BAGIT_FILE_PATH_SEPARATOR));
        if (split.size() > 0 && split.get(0).equals(DATA_FOLDER)) {
          // skip 'data' folder
          List<String> directoryPath = split.subList(1, split.size() - 1);
          String fileId = split.get(split.size() - 1);

          ContentPayload payload = new BagFileContentPayload(bagFile);
          model.createFile(aip.getId(), representationId, directoryPath, fileId, payload, notifyInSteps);
        }
      }
    }
    bag.close();

    model.notifyAIPCreated(aip.getId());

    return model.retrieveAIP(aip.getId());

  }

  private static String generateMetadataFile(BagInfoTxt bagInfoTxt) throws IOException {
    StringBuilder b = new StringBuilder();
    b.append("<metadata>");
    for (Map.Entry<String, String> entry : bagInfoTxt.entrySet()) {
      if (!entry.getKey().equalsIgnoreCase("parent")) {
        b.append("<field name='" + entry.getKey() + "'>" + StringEscapeUtils.escapeXml(entry.getValue()) + "</field>");
      }
    }
    b.append("</metadata>");
    return b.toString();
  }
}
