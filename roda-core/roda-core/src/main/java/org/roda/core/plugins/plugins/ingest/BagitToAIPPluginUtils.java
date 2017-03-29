/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.util.IdUtils;
import org.roda_project.commons_ip.model.IPFile;
import org.roda_project.commons_ip.model.IPRepresentation;
import org.roda_project.commons_ip.model.SIP;
import org.roda_project.commons_ip.model.impl.bagit.BagitUtils;

public class BagitToAIPPluginUtils {
  private static final String METADATA_TYPE = "key-value";
  private static final String METADATA_VERSION = null;

  private BagitToAIPPluginUtils() {
    // do nothing
  }

  public static AIP bagitToAip(SIP bagit, ModelService model, String metadataFilename, List<String> ingestSIPIds,
    String ingestJobId, Optional<String> computedParentId, String createdBy) throws RequestNotValidException,
    NotFoundException, GenericException, AlreadyExistsException, AuthorizationDeniedException {

    Map<String, String> bagitInfo = BagitUtils
      .getBagitInfo(bagit.getDescriptiveMetadata().get(0).getMetadata().getPath());
    String metadataAsString = MetadataFileUtils.generateMetadataFile(bagitInfo);
    ContentPayload metadataAsPayload = new StringContentPayload(metadataAsString);

    AIPState state = AIPState.INGEST_PROCESSING;
    Permissions permissions = new Permissions();

    boolean notify = false;
    String aipType = RodaConstants.AIP_TYPE_MIXED;

    AIP aip = model.createAIP(state, computedParentId.orElse(null), aipType, permissions, ingestSIPIds, ingestJobId,
      notify, createdBy);

    model.createDescriptiveMetadata(aip.getId(), metadataFilename, metadataAsPayload, METADATA_TYPE, METADATA_VERSION,
      notify);

    String representationId = IdUtils.createUUID();
    boolean original = true;
    String representationType = RodaConstants.REPRESENTATION_TYPE_MIXED;

    model.createRepresentation(aip.getId(), representationId, original, representationType, notify);

    for (IPRepresentation rep : bagit.getRepresentations()) {
      for (IPFile bagFile : rep.getData()) {
        ContentPayload payload = new FSPathContentPayload(bagFile.getPath());
        model.createFile(aip.getId(), representationId, bagFile.getRelativeFolders(), bagFile.getFileName(), payload,
          notify);
      }
    }

    model.notifyAipCreated(aip.getId());
    return model.retrieveAIP(aip.getId());
  }

}
