package org.roda.core.model.lites;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;
import org.roda.core.util.IdUtils;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class ParsedPreservationMetadataLite extends ParsedLite {

  @Serial
  private static final long serialVersionUID = -3155921076653152011L;

  private final PreservationMetadata.PreservationMetadataType type;
  private final String id;
  private final String aipId;
  private final String representationId;
  private final String fileId;
  private final List<String> fileDirectoryPath;

  public ParsedPreservationMetadataLite(LiteRODAObject dmLite, String[] split) throws GenericException {
    super(dmLite.getInfo());
    type = IdUtils.getPreservationTypeFromId(LiteRODAObjectFactory.decodeId(split[split.length - 1]));
    id = LiteRODAObjectFactory.decodeId(split[split.length - 1]);

    if (split.length == 2) {
      aipId = null;
      representationId = null;
      fileId = null;
      fileDirectoryPath = null;
    } else if (split.length == 3) {
      aipId = LiteRODAObjectFactory.decodeId(split[1]);
      representationId = null;
      fileId = null;
      fileDirectoryPath = null;
    } else if (split.length == 4) {
      aipId = LiteRODAObjectFactory.decodeId(split[1]);
      representationId = LiteRODAObjectFactory.decodeId(split[2]);
      fileId = null;
      fileDirectoryPath = null;
    } else {
      aipId = LiteRODAObjectFactory.decodeId(split[1]);
      representationId = LiteRODAObjectFactory.decodeId(split[2]);
      fileDirectoryPath = new ArrayList<>();
      String parsedFileId = null;
      for (int i = 2; i < split.length - 1; i++) {
        if (i + 1 == split.length) {
          parsedFileId = LiteRODAObjectFactory.decodeId(split[i]);
        } else {
          fileDirectoryPath.add(LiteRODAObjectFactory.decodeId(split[i]));
        }
      }
      fileId = parsedFileId;
    }
  }

  public PreservationMetadata.PreservationMetadataType getType() {
    return type;
  }

  public String getId() {
    return id;
  }

  public String getAipId() {
    return aipId;
  }

  public String getRepresentationId() {
    return representationId;
  }

  public String getFileId() {
    return fileId;
  }

  public List<String> getFileDirectoryPath() {
    return fileDirectoryPath;
  }

  @Override
  public IsRODAObject toRODAObject(ModelService model)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    if (aipId != null && representationId != null && fileId != null) {
      return model.retrievePreservationMetadata(aipId, representationId, fileDirectoryPath, fileId, type);
    } else if (aipId != null && representationId != null) {
      return model.retrievePreservationMetadata(aipId, representationId, null, null, type);
    } else if (aipId != null) {
      return model.retrievePreservationMetadata(aipId, null, null, null, type);
    } else {
      return model.retrievePreservationMetadata(id, type);
    }
  }
}
