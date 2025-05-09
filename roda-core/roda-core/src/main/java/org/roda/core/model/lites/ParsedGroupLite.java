package org.roda.core.model.lites;

import java.io.Serial;

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
public class ParsedGroupLite extends ParsedLite {

  @Serial
  private static final long serialVersionUID = 2732547488842527018L;

  private final String id;

  public ParsedGroupLite(LiteRODAObject aipLite, String[] split) throws GenericException {
    super(aipLite.getInfo());
    id = LiteRODAObjectFactory.decodeId(split[1]);
  }

  public String getId() {
    return id;
  }

  @Override
  public IsRODAObject toRODAObject(ModelService model) throws NotFoundException, GenericException {
    return model.retrieveGroup(id);
  }
}
