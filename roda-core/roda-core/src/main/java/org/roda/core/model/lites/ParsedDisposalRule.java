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
public class ParsedDisposalRule extends ParsedLite {

  @Serial
  private static final long serialVersionUID = -177619704403990777L;

  private final String id;

  public ParsedDisposalRule(LiteRODAObject disposalRuleLite, String[] split) throws GenericException {
    super(disposalRuleLite.getInfo());
    id = LiteRODAObjectFactory.decodeId(split[1]);
  }

  public String getId() {
    return id;
  }

  @Override
  public IsRODAObject toRODAObject(ModelService model)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return model.retrieveDisposalRule(id);
  }
}
