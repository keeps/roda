/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.roda.core.common.MetadataFileUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.model.ModelService;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StringContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagInfoTxt;

public class BagitToAIPPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(BagitToAIPPluginUtils.class);

  private static final String DATA_FOLDER = "data";
  private static final String METADATA_TYPE = "key-value";
  private static final String METADATA_VERSION = null;
  private static final String BAGIT_FILE_PATH_SEPARATOR = "/";

  public static AIP bagitToAip(Bag bag, Path bagitPath, ModelService model, String metadataFilename,
    List<String> ingestSIPIds, String ingestJobId, Optional<String> computedParentId, String createdBy)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {

    BagInfoTxt bagInfoTxt = bag.getBagInfoTxt();
    String metadataAsString = MetadataFileUtils.generateMetadataFile(bagInfoTxt);
    ContentPayload metadataAsPayload = new StringContentPayload(metadataAsString);

    AIPState state = AIPState.INGEST_PROCESSING;
    Permissions permissions = new Permissions();

    boolean notify = false;

    String aipType = RodaConstants.AIP_TYPE_MIXED;

    AIP aip = model.createAIP(state, computedParentId.orElse(null), aipType, permissions, ingestSIPIds, ingestJobId,
      notify, createdBy);

    model.createDescriptiveMetadata(aip.getId(), metadataFilename, metadataAsPayload, METADATA_TYPE, METADATA_VERSION,
      notify);

    String representationId = UUID.randomUUID().toString();
    boolean original = true;

    String representationType = RodaConstants.REPRESENTATION_TYPE_MIXED;

    model.createRepresentation(aip.getId(), representationId, original, representationType, notify);

    if (bag.getPayload() != null) {
      for (BagFile bagFile : bag.getPayload()) {
        List<String> split = Arrays.asList(bagFile.getFilepath().split(BAGIT_FILE_PATH_SEPARATOR));
        if (!split.isEmpty() && split.get(0).equals(DATA_FOLDER)) {
          // skip 'data' folder
          List<String> directoryPath = split.subList(1, split.size() - 1);
          String fileId = split.get(split.size() - 1);

          ContentPayload payload = new BagFileContentPayload(bagFile);
          model.createFile(aip.getId(), representationId, directoryPath, fileId, payload, notify);
        }
      }
    }
    IOUtils.closeQuietly(bag);

    // FIXME 20160516 hsilva: put SIP inside the AIP

    model.notifyAIPCreated(aip.getId());

    return model.retrieveAIP(aip.getId());

  }

}
