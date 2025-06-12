package org.roda.wui.api.v2.services;

import java.io.InputStream;

import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.model.ModelService;
import org.springframework.stereotype.Service;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

@Service
public class AuditLogService {
  public void importLogEntries(ModelService modelService, InputStream inputStream, String filename)
    throws AuthorizationDeniedException,
    GenericException, AlreadyExistsException, RequestNotValidException, NotFoundException {
    modelService.importLogEntries(inputStream, filename);
  }
}
