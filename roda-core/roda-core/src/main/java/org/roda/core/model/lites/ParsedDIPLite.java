package org.roda.core.model.lites;

import java.io.Serial;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class ParsedDIPLite extends ParsedLite {

  @Serial
  private static final long serialVersionUID = 4130158720656955204L;

  private final String id;

  public ParsedDIPLite(LiteRODAObject dipLite, String[] split) throws GenericException {
    super(dipLite.getInfo());
    id = LiteRODAObjectFactory.decodeId(split[1]);
  }

  public String getId() {
    return id;
  }

  @Override
  public IsRODAObject toRODAObject(ModelService model)
    throws AuthorizationDeniedException, NotFoundException, GenericException {
    return model.retrieveDIP(id);
  }
}
