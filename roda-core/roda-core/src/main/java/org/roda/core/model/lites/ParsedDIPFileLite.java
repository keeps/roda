package org.roda.core.model.lites;

import java.io.Serial;
import java.util.ArrayList;

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
public class ParsedDIPFileLite extends ParsedLite {

  @Serial
  private static final long serialVersionUID = 3691381702070951853L;

  private final String id;
  private final ArrayList<String> directoryPath;
  private final String fileId;

  public ParsedDIPFileLite(LiteRODAObject dipLite, String[] split) throws GenericException {
    super(dipLite.getInfo());
    id = LiteRODAObjectFactory.decodeId(split[1]);
    String parsedFileId = null;
    directoryPath = new ArrayList<>();
    for (int i = 2; i < split.length; i++) {
      if (i + 1 == split.length) {
        parsedFileId = LiteRODAObjectFactory.decodeId(split[i]);
      } else {
        directoryPath.add(LiteRODAObjectFactory.decodeId(split[i]));
      }
    }
    fileId = parsedFileId;
  }

  public String getId() {
    return id;
  }

  public String getFileId() {
    return fileId;
  }

  public ArrayList<String> getDirectoryPath() {
    return directoryPath;
  }

  @Override
  public IsRODAObject toRODAObject(ModelService model)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return model.retrieveDIPFile(id, directoryPath, fileId);
  }
}
