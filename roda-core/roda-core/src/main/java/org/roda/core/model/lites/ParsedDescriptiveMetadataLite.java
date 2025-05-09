package org.roda.core.model.lites;

import java.io.Serial;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class ParsedDescriptiveMetadataLite extends ParsedLite {

  @Serial
  private static final long serialVersionUID = -9191200333537240968L;

  private final String id;
  private final String aipId;
  private final String representationId;

  public ParsedDescriptiveMetadataLite(LiteRODAObject dmLite, String[] split) throws GenericException {
    super(dmLite.getInfo());
    aipId = LiteRODAObjectFactory.decodeId(split[1]);
    if (split.length > 3) {
      representationId = LiteRODAObjectFactory.decodeId(split[2]);
      id = LiteRODAObjectFactory.decodeId(split[3]);
    } else {
      representationId = null;
      id = LiteRODAObjectFactory.decodeId(split[2]);
    }
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

  @Override
  public IsRODAObject toRODAObject(ModelService model)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    if (representationId == null) {
      return model.retrieveDescriptiveMetadata(aipId, id);
    } else {
      return model.retrieveDescriptiveMetadata(aipId, representationId, id);
    }
  }
}
