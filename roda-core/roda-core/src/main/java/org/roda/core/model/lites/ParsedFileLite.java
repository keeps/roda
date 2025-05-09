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
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class ParsedFileLite extends ParsedLite {

  @Serial
  private static final long serialVersionUID = -8884934720086713462L;

  private final String aipId;
  private final String representationId;
  private final List<String> directoryPath;
  private final String id;

  public ParsedFileLite(LiteRODAObject fileLite, String[] split) throws GenericException {
    super(fileLite.getInfo());
    aipId = LiteRODAObjectFactory.decodeId(split[1]);
    representationId = LiteRODAObjectFactory.decodeId(split[2]);
    directoryPath = new ArrayList<>();
    String fileId = null;
    for (int i = 3; i < split.length; i++) {
      if (i + 1 == split.length) {
        fileId = LiteRODAObjectFactory.decodeId(split[i]);
      } else {
        directoryPath.add(LiteRODAObjectFactory.decodeId(split[i]));
      }
    }
    id = fileId;
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

  public List<String> getDirectoryPath() {
    return directoryPath;
  }

  @Override
  public IsRODAObject toRODAObject(ModelService model)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return model.retrieveFile(aipId, representationId, directoryPath, id);
  }
}
