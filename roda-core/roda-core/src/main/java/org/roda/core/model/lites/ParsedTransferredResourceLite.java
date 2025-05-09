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
public class ParsedTransferredResourceLite extends ParsedLite {

  @Serial
  private static final long serialVersionUID = -9172060277258379663L;

  private final String fullPath;

  public ParsedTransferredResourceLite(LiteRODAObject aipLite, String[] split) throws GenericException {
    super(aipLite.getInfo());
    fullPath = LiteRODAObjectFactory.decodeId(split[1]);
  }

  public String getFullPath() {
    return fullPath;
  }

  @Override
  public IsRODAObject toRODAObject(ModelService model)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return model.retrieveTransferredResource(fullPath);
  }
}
