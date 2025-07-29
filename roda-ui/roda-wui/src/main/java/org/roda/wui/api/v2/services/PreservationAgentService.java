/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v2.services;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.model.ModelService;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryConsumesOutputStream;
import org.springframework.stereotype.Service;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
public class PreservationAgentService {

  public StreamResponse retrievePreservationAgentFile(ModelService model, String id)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    final Binary binary = model.retrievePreservationAgent(id);

    final ConsumesOutputStream stream = new BinaryConsumesOutputStream(binary,
      RodaConstants.MEDIA_TYPE_APPLICATION_XML);
    return new StreamResponse(stream);
  }
}
