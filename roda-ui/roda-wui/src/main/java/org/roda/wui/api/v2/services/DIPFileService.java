package org.roda.wui.api.v2.services;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.RangeConsumesOutputStream;
import org.roda.wui.common.model.RequestContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Eduardo Teixeira <eteixeira@keep.pt>
 */
@Service
public class DIPFileService {
  public RangeConsumesOutputStream retrieveDIPFileRangeStream(RequestContext requestContext, DIPFile dipfile)
    throws RequestNotValidException {
    ModelService model = requestContext.getModelService();
    if (!dipfile.isDirectory()) {
      final RangeConsumesOutputStream stream;
      try {
        DirectResourceAccess directDIPFileAccess = model.getDirectAccess(dipfile);
        stream = new RangeConsumesOutputStream(directDIPFileAccess.getPath());
        return stream;
      } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | NotFoundException e) {
        throw new RuntimeException(e);
      }

    } else
      throw new RequestNotValidException("Range stream for directory unsupported");
  }

  public StreamResponse retrieveDIPFileStreamResponse(RequestContext requestContext, DIPFile dipFile)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    ModelService model = requestContext.getModelService();

    final ConsumesOutputStream stream;
    List<String> idPaths = new ArrayList<>();
    idPaths.add(dipFile.getDipId());
    idPaths.addAll(dipFile.getPath());
    idPaths.add(dipFile.getId());

    Optional<LiteRODAObject> rodaDIPobj = LiteRODAObjectFactory.get(DIPFile.class, idPaths.toArray(String[]::new));
    stream = model.exportObjectToStream(rodaDIPobj.get());
    return new StreamResponse(stream);

  }
}
