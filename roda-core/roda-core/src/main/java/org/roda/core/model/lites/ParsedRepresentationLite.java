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
public class ParsedRepresentationLite extends ParsedLite {

  @Serial
  private static final long serialVersionUID = -3928822803239785780L;

  private final String id;
  private final String aipId;

  public ParsedRepresentationLite(LiteRODAObject representationLite, String[] split) throws GenericException {
    super(representationLite.getInfo());
    aipId = LiteRODAObjectFactory.decodeId(split[1]);
    id = LiteRODAObjectFactory.decodeId(split[2]);
  }

  public String getId() {
    return id;
  }

  public String getAipId() {
    return aipId;
  }

  @Override
  public IsRODAObject toRODAObject(ModelService model)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return model.retrieveRepresentation(aipId, id);
  }
}
